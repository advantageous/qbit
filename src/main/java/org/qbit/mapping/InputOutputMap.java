package org.qbit.mapping;

import org.qbit.Input;
import org.qbit.queue.InputQueue;
import org.qbit.Output;
import org.qbit.queue.OutputQueue;

/**
 * Interface used to set and put values into a mapping.
 * The output types are set, which is pure output and expects no return, and put.
 *
 * This interface is for put operations.
 * @author Richard Hightower
 *
 */
public interface InputOutputMap<K, V> extends Output, Input {



    OutputQueue<Entry<K, V>> output();

    InputQueue<Entry<K, V>> input();

}
