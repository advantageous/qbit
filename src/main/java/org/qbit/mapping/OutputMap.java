package org.qbit.mapping;

import org.qbit.Output;
import org.qbit.queue.SendQueue;

/**
 * Created by Richard on 7/18/14.
 * @author Richard Hightower
 * Represents an asynchronous interface to a mapping object or service.
 */
public interface OutputMap<K, V> extends Output {

    SendQueue<Entry<K, V>> setOutput();
}
