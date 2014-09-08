package org.qbit.mapping;

import org.qbit.Input;
import org.qbit.queue.ReceiveQueue;
import org.qbit.Output;
import org.qbit.queue.SendQueue;

/**
 * Interface used to set and put values into a mapping.
 * The send types are set, which is pure send and expects no return, and put.
 *
 * This interface is for put operations.
 * @author Richard Hightower
 *
 */
public interface InputOutputMap<K, V> extends Output, Input {



    SendQueue<Entry<K, V>> output();

    ReceiveQueue<Entry<K, V>> input();

}
