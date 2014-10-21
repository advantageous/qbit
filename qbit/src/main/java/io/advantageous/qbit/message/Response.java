package io.advantageous.qbit.message;



/**
 * Created by Richard on 7/21/14.
 * @author rhightower
 */
public interface Response <T> extends Message<T> {

    boolean wasErrors();

    void body(T body);

    String returnAddress();

    String address();


    long timestamp();
}

