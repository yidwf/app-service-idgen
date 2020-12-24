package com.yesido.idgen.snowflake;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yesido.zookeeper.service.ZkService;

public class SnowFlakeWorkerIdHolder {
    private static Logger LOGGER = LoggerFactory.getLogger(SnowFlakeIDGen.class);
    private final String ipNodePath;
    private static final String Zk_Path_Root = "/id-gen";
    private static final String Zk_Path_Worker_Id_List = Zk_Path_Root + "/worker-ids";
    private static final String Zk_Path_Server_Ip_List = Zk_Path_Root + "/gens";
    private static final String PROP_PATH = System.getProperty("java.io.tmpdir") + "/leafconf/{port}/workerId.properties";
    private ZkService zkService;
    private String ip;
    private String port;
    private long workerId;
    private final int minWorkerId;
    private final int maxWorkerId;

    public SnowFlakeWorkerIdHolder(ZkService zkService, String ip, String port) {
        this.zkService = zkService;
        this.ip = ip;
        this.port = port;
        this.ipNodePath = createIpNodePath(ip);
        this.minWorkerId = 1;
        this.maxWorkerId = (1 << 10) - 1;
        initZkNode();
    }

    private void initZkNode() {
        createZkNode(Zk_Path_Root);
        createZkNode(Zk_Path_Worker_Id_List);
        createZkNode(Zk_Path_Server_Ip_List);
    }

    private void createZkNode(String path) {
        if (zkService.isExistNode(path)) {
            return;
        }
        zkService.createPersistentNode(path);
    }

    public long getWorkerId() {
        if (workerId != 0) {
            return workerId;
        }
        return initWorkerId();
    }

    private long initWorkerId() {
        if (workerId != 0) {
            return workerId;
        }
        if (zkService.isExistNode(ipNodePath)) {
            // 优先获取已经存在的workerId节点
            String wid = zkService.getNodeData(ipNodePath);
            if (wid != null) {
                workerId = Long.parseLong(wid);
                return workerId;
            }
        }
        CuratorFramework curator = zkService.getZkClient();
        for (int workerId = minWorkerId; workerId <= maxWorkerId; workerId++) {
            try {
                String workerIdNodePath = createWorkerIdNodePath(workerId);
                CuratorOp workerIdNodeOp = curator.transactionOp()
                        .create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(workerIdNodePath, ip.getBytes());
                CuratorOp iNodeOp = curator.transactionOp()
                        .create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(ipNodePath, String.valueOf(workerId).getBytes());
                curator.transaction().forOperations(workerIdNodeOp, iNodeOp);
                this.workerId = workerId;
                updateLocalWorkerID(workerId);
                return workerId;
            } catch (Exception e) {
                // ingore
            }
        }
        return getLocalWorkerId();
    }

    private long getLocalWorkerId() {
        // 从文件中获取workerId
        long workerID = 0;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(PROP_PATH.replace("{port}", port + ""))));
            workerID = Integer.valueOf(properties.getProperty("workerId"));
            LOGGER.warn("START FAILED ,use local node file properties workerID-{}", workerID);
        } catch (Exception e1) {
            LOGGER.error("Read file error ", e1);
        }
        return workerID;
    }

    private void updateLocalWorkerID(long workerId) {
        // 将workerId写入本地文件，即使zk失效了，也可以使用本地文件的workerId
        File leafConfFile = new File(PROP_PATH.replace("{port}", port));
        boolean exists = leafConfFile.exists();
        LOGGER.info("file exists status is {}", exists);
        if (exists) {
            try {
                FileUtils.writeStringToFile(leafConfFile, "workerId=" + workerId, false);
                LOGGER.info("update file cache workerID is {}", workerId);
            } catch (IOException e) {
                LOGGER.error("update file cache error ", e);
            }
        } else {
            try {
                boolean mkdirs = leafConfFile.getParentFile().mkdirs();
                LOGGER.info("init local file cache create parent dis status is {}, worker id is {}", mkdirs, workerId);
                if (mkdirs) {
                    if (leafConfFile.createNewFile()) {
                        FileUtils.writeStringToFile(leafConfFile, "workerId=" + workerId, false);
                        LOGGER.info("local file cache workerID is {}", workerId);
                    }
                } else {
                    LOGGER.warn("create parent dir error===");
                }
            } catch (IOException e) {
                LOGGER.warn("craete workerID conf file error", e);
            }
        }
    }

    private String createWorkerIdNodePath(int workerId) {
        return Zk_Path_Worker_Id_List + "/" + workerId;
    }

    private String createIpNodePath(String ip) {
        return Zk_Path_Server_Ip_List + "/" + ip;
    }
}
