package com.yesido.idgen.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yesido.idgen.snowflake.SnowFlakeIDGen;

public class SnowFlakeIDGenTest {

    static Map<String, String> ids = new ConcurrentHashMap<String, String>();
    static SnowFlakeIDGen idGen = new SnowFlakeIDGen(1);

    public static void main(String[] args) {
        //test();
        test2();
    }

    public static void test() {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        String id = String.valueOf(idGen.nextId());
                        if (ids.containsKey(id)) {
                            System.out.println(id);
                        } else {
                            ids.put(id, id);
                        }
                    }

                }
            }).start();
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        System.out.println(ids.size());
    }

    static boolean b = true;

    private static void test2() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (b) {
                    String id = String.valueOf(idGen.nextId());
                    ids.put(id, id);
                }
                System.out.println("end");
            }
        }).start();
        try {
            Thread.sleep(2000);
            b = false;
        } catch (InterruptedException e) {
        }
        System.out.println(ids.size());
    }
}
