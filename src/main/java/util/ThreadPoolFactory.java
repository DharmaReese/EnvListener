package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolFactory {
    private static final int THREADS = 10;

    private static ThreadPoolFactory factory = null;
    private static ExecutorService pool = null;
    public static Lock lock = new ReentrantLock();

    private ThreadPoolFactory() {
        if (pool == null) {
            pool = Executors.newFixedThreadPool(THREADS);
        }
    }

    // 获取线程池工厂单例
    public static ThreadPoolFactory newInstance() {
        if (factory == null) {
            synchronized (ThreadPoolFactory.class) {
                if (factory == null) {
                    factory = new ThreadPoolFactory();
                }
            }
        }

        return factory;
    }

    // 获取连接池单例
    public ExecutorService getPool() {
        return pool;
    }
}
