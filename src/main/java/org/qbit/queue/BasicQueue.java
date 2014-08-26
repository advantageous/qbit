package org.qbit.queue;

import org.boon.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.boon.Exceptions.die;

/**
 * Created by Richard on 8/4/14.
 */
public class BasicQueue <T> implements Queue<T> {


    private final LinkedTransferQueue<T> queue = new LinkedTransferQueue<T>();

    private final int waitTime;

    private final TimeUnit timeUnit;

    private ScheduledExecutorService monitor;

    private ScheduledFuture<?> future;

    private volatile boolean stop;


    private final int batchSize;

    public BasicQueue(int waitTime, TimeUnit timeUnit, int batchSize) {
        this.waitTime = waitTime;
        this.timeUnit = timeUnit;
        this.batchSize = batchSize;
    }

    @Override
    public InputQueue<T> input() {

        return new InputQueue<T>() {

            @Override
            public T pollWait() {
                try {
                 return  queue.poll(waitTime, timeUnit);
                } catch (InterruptedException e) {
                   Thread.interrupted();
                    return null;
                }
            }

            @Override
            public T poll() {
                return  queue.poll();

            }

            @Override
            public T take() {
                try {
                    return queue.take();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return null;
                }
            }

            @Override
            public Iterable<T> readBatch(int max) {

                T item = queue.poll();
                if (item==null) {
                    return Collections.EMPTY_LIST;
                } else {
                    List<T> batch = new ArrayList<T>();
                    batch.add(item);
                    while ((item = queue.poll()) != null) {
                        batch.add(item);
                    }
                    return batch;
                }
            }
        };
    }

    @Override
    public OutputQueue<T> output() {
        return new OutputQueue<T>() {
            @Override
            public boolean offer(T item) {
                return queue.offer(item);
            }

            @Override
            public List<T> offerMany(T... items) {


                List<T> returnList = Lists.linkedList(items);

                for (T item : items) {

                    if (queue.offer(item)) {
                        returnList.remove(item);
                    } else {
                        break;
                    }
                }
                return returnList;
            }

            @Override
            public List<T> offerBatch(Iterable<T> items) {


                List<T> returnList = Lists.list(items);

                for (T item : items) {

                    if (queue.offer(item)) {
                        returnList.remove(item);
                    } else {
                        break;
                    }
                }
                return returnList;
            }
        };
    }



    @Override
    public void startListener(final InputQueueListener<T> listener) {


        if (monitor == null) {
            monitor = Executors.newScheduledThreadPool(1,
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Thread thread = new Thread(runnable);
                            thread.setName("BasicQueueListener");
                            return thread;
                        }
                    }
            );
        } else {
            die("Only one BasicQueue listener allowed at a time");
        }


        future = monitor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              manageQueue(listener);
            }

        }, 50, 50, TimeUnit.MILLISECONDS);



    }

    @Override
    public void stop() {
        if (future!=null) {


            future.cancel(true);
        }

        if (monitor!=null) {
            monitor.shutdownNow();
        }


        stop = true;
    }


    private void manageQueue(InputQueueListener<T> listener) {

        T item = queue.poll(); //Initialize things.
        int count = 0;
        long longCount = 0;

        /* Continues forever or until someone calls stop. */
        while (true) {


            if (item!=null) {
                count++;
            }

            /* Collect a batch of items as long as no item is null. */
            while (item!=null) {

                /* Notify listener that we have an item. */
                listener.receive(item);


                /* If the batch size has hit the max then we need to break. */
                if (count >= batchSize) {
                    listener.limit();
                    break;
                }
                /* Grab the next item from the queue. */
                item = queue.poll();
                count++;

            }

            /* Notify listener that the queue is empty. */
            if (item ==null) {
                listener.empty();
            }
            count = 0;


            item = queue.poll();

            /* See if a yield helps. Try to keep the thread alive. */
            if (item!=null) {
                continue;
            } else {
                Thread.yield();
            }


            /* Get the next item, but wait this time since the queue was empty. */
            try {
                item = queue.poll(waitTime, timeUnit);
            } catch (InterruptedException e) {
                Thread.interrupted();
                if (stop) {

                    listener.shutdown();
                    return;
                }
            }

            if (item==null ) {
                if (longCount % 10 == 0 && stop) {
                    listener.shutdown();
                    return;
                }
                /* Idle means we yielded and then waited a full wait time, so idle might be a good time to do clean up
                or timed tasks.
                 */
                listener.idle();

            }


            longCount++;

        }
    }

}
