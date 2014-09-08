package org.qbit.message.impl;

import org.boon.concurrent.Timer;
import org.boon.json.annotations.JsonIgnore;
import org.qbit.message.CompositeMessage;
import org.qbit.message.Message;
import org.qbit.message.MethodCall;
import org.qbit.message.Response;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.boon.Exceptions.die;


/**
 * Created by Richard on 9/8/14.
 */
public class CompositeMessageImpl<M extends Message<T>, T>  implements CompositeMessage<M,T> {


    private final long id;
    private final long timestamp;
    private List<M> messages;


    private static volatile long idSequence;


    @JsonIgnore
    private static transient Timer timer = Timer.timer();


    public CompositeMessageImpl(List<M> messages) {

        this.messages = messages;
        this.id = idSequence++;
        this.timestamp = timer.time();

    }

    public static <M extends Message<T> & MethodCall<T> & Response<T>, T> CompositeMessage<M, T> messages(List<M> messages) {

        return new CompositeMessageImpl<>(messages);
    }

    @Override
    public Iterator<M> iterator() {
        return   messages.iterator();
    }

    @Override
    public long id() {
        return id;
    }


    @Override
    public T body() {
         die("This is a composite message");
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }


}
