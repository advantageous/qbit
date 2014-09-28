package org.qbit.message;


import org.boon.collections.MultiMap;


/**
 * Created by Richard on 7/21/14.
 */
public interface Request <T> extends Message<T> {


    String address();
    String returnAddress();
    MultiMap<String, String> params();

    MultiMap<String, String> headers();
    long timestamp();

}
