package com.cc.graph.util;

import java.util.concurrent.locks.Lock;

public class LockUtil {

    public static void withLock(Lock lock, Runnable f) {
        lock.lock();
        try {
            f.run();
        } finally {
            lock.unlock();
        }
    }
}