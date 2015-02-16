/*******************************************************************************

  * Copyright (c) 2015. Rick Hightower, Geoff Chandler
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  		http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.queue.ReceiveQueueManager;
import org.boon.core.Sys;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Boon.puts;


/**
 * Created by Richard on 9/8/14.
 *
 * @author rhightower
 */
public class BasicReceiveQueueManager<T> implements ReceiveQueueManager<T> {

    private final boolean debug = false || GlobalConstants.DEBUG;


    //boolean sleepWait = false;

    @Override
    public void manageQueue(ReceiveQueue<T> inputQueue, ReceiveQueueListener<T> listener, int batchSize, AtomicBoolean stop) {


        T item = inputQueue.poll(); //Initialize things.

        int count = 0;
        long longCount = 0;

        /* Continues forever or until someone calls stop. */
        while (true) {


            /* Collect a batch of items as long as no item is null. */
            while (item != null) {

                listener.startBatch();

                count++;

                /* Notify listener that we have an item. */
                listener.receive(item);


                /* If the batch size has hit the max then we need to break. */
                if (count >= batchSize) {

                    if (debug) {
                        puts("BasicReceiveQueueManager limit reached", batchSize);
                    }
                    listener.limit();
                    break;
                }
                /* Grab the next item from the queue. */
                item = inputQueue.poll();
                count++;

            }

            /* Notify listener that the queue is empty. */
            listener.empty();

            if (debug) {
                puts("BasicReceiveQueueManager empty queue count was", count, Thread.currentThread().getName());
                Sys.sleep(1_000);
            }

            count = 0;



            /* Get the next item, but wait this time since the queue was empty. */

            item = inputQueue.pollWait();


            if (item == null) {
                if (longCount % 100 == 0) {
                    if (Thread.currentThread().isInterrupted()) {
                        if (stop.get()) {
                            listener.shutdown();
                            return;
                        }
                    }
                } else if (longCount % 1000 == 0 && stop.get()) {
                    listener.shutdown();
                }
                /* Idle means we yielded and then waited a full wait time, so idle might be a good time to do clean up
                or timed tasks.
                 */
                listener.idle();

                if (stop.get()) {
                    listener.shutdown();
                    return;
                }


                if (debug) {
                    puts("BasicReceiveQueueManager idle");
                }


            }


            longCount++;


        }


    }

//    /*
//
//            if (sleepWait) {
//
//                item = inputQueue.poll();
//
//            /* See if a yield helps. Try to keep the thread alive. */
//    if (item != null) {
//        continue;
//    } else {
//        Thread.yield();
//    }
//
//
//    item = inputQueue.poll();
//
//            /* See if a yield helps. Try to keep the thread alive. */
//    if (item != null) {
//        continue;
//    } else {
//        LockSupport.parkNanos(1_000_000);
//    }
//
//
//    item = inputQueue.poll();
//
//            /* See if a yield helps. Try to keep the thread alive. */
//    if (item != null) {
//        continue;
//    } else {
//
//        LockSupport.parkNanos(2_000_000);
//    }
//
//
//    item = inputQueue.poll();
//
//            /* See if a yield helps. Try to keep the thread alive. */
//    if (item != null) {
//        continue;
//    } else {
//
//        LockSupport.parkNanos(4_000_000);
//    }
//
//
//    item = inputQueue.poll();
//
//            /* See if a yield helps. Try to keep the thread alive. */
//    if (item != null) {
//        continue;
//    } else {
//        LockSupport.parkNanos(8_000_000);
//
//    }
//}
//
//*/
}
