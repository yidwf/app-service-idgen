package com.yesido.idgen.snowflake.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yesido.idgen.core.IDGen;
import com.yesido.idgen.core.IDResult;
import com.yesido.idgen.snowflake.SnowFlakeIDGen;
import com.yesido.idgen.snowflake.SnowFlakeWorkerIdHolder;
import com.yesido.idgen.snowflake.exception.SnowflakeException;
import com.yesido.lib.utils.BizUtil;
import com.yesido.zookeeper.service.ZkService;

@Service
public class SnowFlakeIDService {
    private IDGen idGen;
    private SnowFlakeWorkerIdHolder holder;
    @Autowired
    private ZkService zkService;
    @Value("${server.port:80}")
    private int port;

    @PostConstruct
    public void init() {
        String ip = BizUtil.getLocalIp();
        holder = new SnowFlakeWorkerIdHolder(zkService, ip, String.valueOf(port));
        idGen = new SnowFlakeIDGen(holder.getWorkerId());
        idGen.init();
    }

    public IDResult nextId() {
        try {
            long id = idGen.nextId();
            return IDResult.ok(id);
        } catch (SnowflakeException e) {
            return IDResult.error(e);
        } catch (Exception e) {
            return IDResult.error();
        }
    }
}
