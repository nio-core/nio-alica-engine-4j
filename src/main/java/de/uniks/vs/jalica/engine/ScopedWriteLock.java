package de.uniks.vs.jalica.engine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class ScopedWriteLock extends ReentrantReadWriteLock {

    private Lock mutex;

    public ScopedWriteLock() {}

    public ScopedWriteLock(Lock mutex) {
        this.mutex = mutex;
    }
}
