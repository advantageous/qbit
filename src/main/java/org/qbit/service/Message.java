package org.qbit.service;

import java.util.Map;

/**
 * Created by Richard on 7/21/14.
 */
public interface Message <T> {
    long id();

    String address();
    Map<String, Object> params();
    T body(); //Body could be a Map for parameters for forms or JSON or bytes[]

}
