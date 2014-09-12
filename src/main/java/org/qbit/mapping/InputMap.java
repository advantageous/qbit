package org.qbit.mapping;

import org.qbit.Input;
import org.qbit.queue.ReceiveQueue;
import org.qbit.queue.SendQueue;

/**
 * Thread safe receiveQueue from a mapping.
 * The get method is async.
 * You call get, and the poll the queue for the value.
 * Get can be called on one thread while inputForGet can be polled on another thread.
 * <code>
 *     <pre>
 *         InputMap<String, String> map = Factory.tcpIpMap("localhost", 9999, string, string, JSON);
 *         map.sendQueue().send("foo");
 *
 *         ...
 *
 *         String value = map.receiveQueue().poll(1000, TimeUnit.MILLISECONDS);
 *
 *     </pre>
 * </code>

 */
public interface InputMap <K, V> extends Input {


    /**
     * Represents a series of get operations.
     *
     * @return sendQueue queue
     */
    SendQueue<K> output();


    /**
     * Represents a series of get return values.
     * @return an receiveQueue queue
     */
    ReceiveQueue<Entry<K, V>> input();
}
