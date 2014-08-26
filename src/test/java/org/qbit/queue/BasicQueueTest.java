package org.qbit.queue;

import org.junit.Test;

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
    public void test() {
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


       sleep(1000);
       synchronized (counter) {
           ok = counter[0] == 115 || die("Crap not 115", counter[0]);
       }

    }
}
