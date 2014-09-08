package org.qbit.message;


import java.util.Map;

/**
 * Created by Richard on 7/21/14.
 */
public interface Request <T> extends Message<T> {


    String address();
    String returnAddress();
    Map<String, Object> params();
    long timestamp();

}
