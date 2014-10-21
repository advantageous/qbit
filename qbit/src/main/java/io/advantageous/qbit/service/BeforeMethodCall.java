package io.advantageous.qbit.service;

import io.advantageous.qbit.message.MethodCall;

/**
 * Use this to register for before method calls for services.
 *
 * Created by Richard on 8/26/14.
 * @author rhightower
 */
public interface BeforeMethodCall {

    boolean before(MethodCall call);
}
