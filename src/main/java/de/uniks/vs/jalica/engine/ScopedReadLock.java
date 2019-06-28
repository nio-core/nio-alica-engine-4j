package de.uniks.vs.jalica.engine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

public class ScopedReadLock extends ReentrantReadWriteLock {

    private Lock mutex;
    private ScopedReadLock readLock;

    public ScopedReadLock() {
    }

    public ScopedReadLock(Lock mutex) {
        this.mutex = mutex;
    }

    public ScopedReadLock(ScopedReadLock readLock) {
        this.readLock = readLock;
    }

    public void unlock() {
        if (readLock != null) readLock.unlock();
        if (mutex != null) mutex.unlock();
    }
}
