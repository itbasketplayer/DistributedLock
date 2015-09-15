package com.ooopen.lock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

class LockWatcher implements Watcher{
    @Override
    public void process(WatchedEvent watchedEvent) {
        synchronized (this){
            this.notifyAll();
        }
    }
}
