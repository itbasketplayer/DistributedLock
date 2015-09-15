package com.ooopen.lock.test;

import com.ooopen.lock.DistributedLock;
import com.ooopen.lock.LockManager;

import java.util.Scanner;

public class TestLockMain {
    public static void main(String[] s) throws Exception{
        DistributedLock lock = LockManager.createMutexLock("192.168.20.31:2181", "fp");
        lock.lock();
        System.out.println("请输入命令：\n");
        Scanner in = new Scanner(System.in);
        String line = null;
        while(null != (line = in.nextLine())) {
            if(line.startsWith("bye")){
                break;
            }
        }
        System.out.println("unlock");
        lock.unlock();
    }
}
