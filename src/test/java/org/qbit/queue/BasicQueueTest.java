package org.qbit.queue;

import org.boon.Lists;
import org.boon.core.Sys;
import org.junit.Test;
import org.qbit.queue.impl.BasicQueue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.boon.core.Sys.sleep;

/**
 * Created by Richard on 8/11/14.
 */
public class BasicQueueTest {

    boolean ok;


    @Test
    public void testUsingListener() {
       BasicQueue<String> queue = new BasicQueue<>("test", 1000, TimeUnit.MILLISECONDS, 10);

       final int []counter = new int[1];

       queue.startListener(new ReceiveQueueListener<String>() {
           @Override
           public void receive(String item) {
               puts (item);
               synchronized (counter) {
                   counter[0]++;
               }
           }

           @Override
           public void empty() {
                puts ("Queue is empty");

           }

           @Override
           public void limit() {

               puts ("Batch size limit is reached");
           }

           @Override
           public void shutdown() {

               puts("Queue is shut down");
           }

           @Override
           public void idle() {

               puts("Queue is idle");

           }
       });

        final SendQueue<String> sendQueue = queue.sendQueue();
        for (int index = 0; index < 10; index++) {
            sendQueue.send("item" + index);
       }


        sendQueue.flushSends();

        sleep(100);
        synchronized (counter) {
            puts("1", counter[0]);
        }


       for (int index = 0; index < 100; index++) {
            sendQueue.send("item2nd" + index);
       }

        sendQueue.flushSends();


        sleep(100);
        synchronized (counter) {
            puts("2", counter[0]);
        }

        for (int index = 0; index < 5; index++) {
            sleep(100);
            sendQueue.send("item3rd" + index);
       }
        sendQueue.flushSends();

        sleep(100);
        synchronized (counter) {
            puts("3", counter[0]);
        }


       sendQueue.sendMany("hello", "how", "are", "you");


        sleep(100);
        synchronized (counter) {
            puts("4", counter[0]);
        }

       List<String> list = Lists.linkedList("Good", "Thanks");

       sendQueue.sendBatch(list);


        sleep(100);
        synchronized (counter) {
            puts("1", counter[0]);
        }



       sleep(100);
       synchronized (counter) {
           ok = counter[0] == 121 || die("Crap not 121", counter[0]);
       }


      queue.stop();

    }



    @Test
    public void testUsingInput() throws Exception {

        final BasicQueue<String> queue = new BasicQueue<>("test", 1000, TimeUnit.MILLISECONDS, 10);

        final int count[] = new int[1];



        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {


                final SendQueue<String> sendQueue = queue.sendQueue();

                for (int index = 0; index < 1000; index++) {
                    sendQueue.send("item" + index);
                }
                sendQueue.flushSends();
            }
        });



        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiveQueue<String> receiveQueue = queue.receiveQueue();

                while (receiveQueue.poll()!=null) {
                    count[0]++;
                }
            }
        });

        writer.start();

        Sys.sleep(100);

        reader.start();

        writer.join();
        reader.join();

        puts(count[0]);

        ok = count[0] == 1000 || die("count should be 1000", count[0]);

    }

    @Test
    public void testUsingInputTake() throws Exception {

        final BasicQueue<String> queue = new BasicQueue<>("test", 1000, TimeUnit.MILLISECONDS, 1000);

        final AtomicLong count = new AtomicLong();

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {

                long cnt = 0;
                final ReceiveQueue<String> receiveQueue = queue.receiveQueue();
                String item = receiveQueue.take();

                while (item !=null) {
                    cnt++;
                    puts(item);
                    item = receiveQueue.take();

                    if (cnt>=900) {
                        count.set(cnt);
                        break;
                    }
                }
            }
        });


        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {

                final SendQueue<String> sendQueue = queue.sendQueue();

                for (int index = 0; index < 1000; index++) {
                    sendQueue.send("this item " + index);
                }
                sendQueue.flushSends();
            }
        });




        writer.start();


        reader.start();

        writer.join();
        reader.join();

        puts(count.get());

        ok = count.get() == 900 || die("count should be 1000", count.get());

    }


    @Test
    public void testUsingInputPollWait() throws Exception {

        final BasicQueue<String> queue = new BasicQueue<>("test", 1000, TimeUnit.MILLISECONDS, 10);

        final int count[] = new int[1];



        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {


                SendQueue<String> sendQueue = queue.sendQueue();
                for (int index = 0; index < 1000; index++) {
                    sendQueue.send("item" + index);
                }
                sendQueue.flushSends();
            }
        });



        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiveQueue<String> receiveQueue = queue.receiveQueue();

                String item = receiveQueue.pollWait();

                while (item !=null) {
                    count[0]++;
                    puts(item);
                    item = receiveQueue.pollWait();

                }
            }
        });

        writer.start();


        reader.start();

        writer.join();
        reader.join();

        puts(count[0]);

        ok = count[0] == 1000 || die("count should be 1000",  count[0]);

    }

}
