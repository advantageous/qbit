package io.advantageous.boon.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Richard on 9/10/14.
 */
public class SimpleExecutors {

    public static ExecutorService threadPool(int size, final String poolName) {

        final int [] threadId = new int[1];
        threadId[0] = 0;
        return Executors.newFixedThreadPool(size,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        threadId[0] = threadId[0]++;
                        Thread thread = new Thread(runnable);
                        thread.setName(poolName + " " + threadId[0]);
                        return thread;
                    }
                }
        );
    }


    public static ExecutorService threadPool(final String poolName) {

        return Executors.newCachedThreadPool(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setName(poolName );
                        return thread;
                    }
                }
        );
    }
}
