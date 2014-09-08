package org.qbit.queue.impl;

import org.boon.Lists;
import org.qbit.queue.SendQueue;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by Richard on 9/8/14.
 */
public class BasicSendQueue<T> implements SendQueue<T> {


    private final LinkedTransferQueue<Object> queue;

    public BasicSendQueue(LinkedTransferQueue<Object> queue) {
        this.queue = queue;
    }

    @Override
    public boolean offer(T item) {
        return queue.offer(item);
    }

    @Override
    public boolean offerMany(T... items) {


        List<T> returnList = Lists.linkedList(items);
        return queue.offer(returnList);

    }

    @Override
    public boolean offerBatch(Iterable<T> items) {


        List<T> returnList = Lists.linkedList(items);
        return queue.offer(returnList);

    }
}
