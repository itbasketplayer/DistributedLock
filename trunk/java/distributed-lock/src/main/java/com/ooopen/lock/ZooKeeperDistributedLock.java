package com.ooopen.lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.Arrays;
import java.util.List;

class ZooKeeperDistributedLock implements DistributedLock {
    protected ZooKeeper zk = null;
    protected String lockSid;
    protected String curLock;
    protected int maxCount = 0;
    protected final LockWatcher lockWatcher = new LockWatcher();

    ZooKeeperDistributedLock(String hostList, String lockName, int sessionTimeout, int maxCount) throws Exception {
        try{
            zk = new ZooKeeper(hostList, sessionTimeout, lockWatcher);
            String root = "/locks";
            lockSid = root + "/" + lockName;
            if(null == zk.exists(root,false)) {
                zk.create(root, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if(null == zk.exists(lockSid,false)){
                zk.create(lockSid, String.valueOf(maxCount).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }else {
                byte[] maxCountData = zk.getData(lockSid,false,null);
                int oldMaxCount = Integer.valueOf(new String(maxCountData));
                if(oldMaxCount != maxCount){
                    throw new IllegalArgumentException("锁已存在，且新指定的maxCount与原有maxCount不相等");
                }
            }
        }catch (Exception e){
            throw new Exception(e);
        }

        this.maxCount = maxCount;
    }

    @Override
    public synchronized void lock() throws Exception {
        try {
            if(null == curLock){
                curLock = zk.create(lockSid + "/lock", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }

            while(true){
                synchronized (lockWatcher){
                    List<String> listChi = zk.getChildren(lockSid, true);
                    String[] nodes = listChi.toArray(new String[listChi.size()]);
                    Arrays.sort(nodes);

                    for(int i=0; i<nodes.length; i++){
                        String lockElemSid = lockSid + "/" + nodes[i];
                        if(lockElemSid.equals(curLock)){
                            if(i < maxCount){
                                return ;
                            }else{
                                lockWatcher.wait();
                                break;
                            }
                        }
                    }
                }
             }
        } catch (KeeperException e) {
            throw new Exception(e);
        } catch (InterruptedException e) {
            throw new Exception(e);
        }
    }

    @Override
    public synchronized void unlock() throws Exception {
        if(null != curLock){
            try {
                zk.delete(curLock,-1);
            } catch (InterruptedException e) {
                throw new Exception(e);
            } catch (KeeperException e) {
                throw new Exception(e);
            }finally {
                curLock = null;
            }
        }
    }
}
