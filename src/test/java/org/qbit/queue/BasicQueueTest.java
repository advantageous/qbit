package org.qbit.queue;

import org.boon.Lists;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
       BasicQueue<String> queue = new BasicQueue<String>(1000, TimeUnit.MILLISECONDS, 10);

       final int []counter = new int[1];

       queue.startListener(new InputQueueListener<String>() {
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
           public void shutdown() {

               puts("Queue is shut down");
           }

           @Override
           public void idle() {

               puts("Queue is idle");

           }
       });


       for (int index = 0; index < 10; index++) {
           queue.output().offer("item" + index);
       }


       for (int index = 0; index < 100; index++) {
            queue.output().offer("item2nd" + index);
       }

       for (int index = 0; index < 5; index++) {
            sleep(1000);
            queue.output().offer("item3rd" + index);
       }


       queue.output().offerMany("hello", "how", "are", "you");


       List<String> list = Lists.linkedList("Good", "Thanks");

       queue.output().offerBatch(list);




       sleep(1000);
       synchronized (counter) {
           ok = counter[0] == 121 || die("Crap not 121", counter[0]);
       }


      queue.stop();

    }



    @Test
    public void testUsingInput() throws Exception {

        final BasicQueue<String> queue = new BasicQueue<String>(1000, TimeUnit.MILLISECONDS, 10);

        final int count[] = new int[1];


        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {

                for (int index = 0; index < 1000; index++) {
                    queue.output().offer("item" + index);
                }
            }
        });



        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {

                while (queue.input().poll()!=null) {
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

        ok = count[0] == 1000 || die("count should be 1000");

    }

    @Test
    public void testUsingInputTake() throws Exception {

        final BasicQueue<String> queue = new BasicQueue<String>(1000, TimeUnit.MILLISECONDS, 10);

        final int count[] = new int[1];


        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {

                for (int index = 0; index < 1000; index++) {
                    queue.output().offer("item" + index);
                }
            }
        });



        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {

                String item = queue.input().take();

                while (item !=null) {
                    count[0]++;
                    puts(item);
                    item = queue.input().take();

                    if (count[0]==900) {
                        break;
                    }
                }
            }
        });

        writer.start();


        reader.start();

        writer.join();
        reader.join();

        puts(count[0]);

        ok = count[0] == 900 || die("count should be 1000");

    }


    @Test
    public void testUsingInputPollWait() throws Exception {

        final BasicQueue<String> queue = new BasicQueue<String>(1000, TimeUnit.MILLISECONDS, 10);

        final int count[] = new int[1];


        Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {

                for (int index = 0; index < 1000; index++) {
                    queue.output().offer("item" + index);
                }
            }
        });



        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {

                String item = queue.input().pollWait();

                while (item !=null) {
                    count[0]++;
                    puts(item);
                    item = queue.input().pollWait();

                }
            }
        });

        writer.start();


        reader.start();

        writer.join();
        reader.join();

        puts(count[0]);

        ok = count[0] == 1000 || die("count should be 1000");

    }

}
