package com.ooopen.lock;

import java.util.HashMap;
import java.util.Map;

public class LockManager {
    protected static Map<String,DistributedLock> locks = new HashMap<String, DistributedLock>();

    public static DistributedLock createSemaphoreLock(String hostList,String lockName, int maxCount) throws Exception {
        synchronized (locks){
            if(locks.containsKey(lockName)){
                return locks.get(lockName);
            }else{
                DistributedLock distributedLock = new ZooKeeperDistributedLock(hostList,lockName,30000,maxCount);
                locks.put(lockName,distributedLock);
                return distributedLock;
            }
        }
    }

    public static DistributedLock createMutexLock(String hostList, String lockName) throws Exception {
        return createSemaphoreLock(hostList,lockName,1);
    }
}
