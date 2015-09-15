package com.ooopen.lock;

public interface DistributedLock{

    public void lock() throws Exception;

    public void unlock() throws Exception;
}
