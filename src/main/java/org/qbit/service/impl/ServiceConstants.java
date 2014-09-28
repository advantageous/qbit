package org.qbit.service.impl;

import org.qbit.message.Response;
import org.qbit.transforms.NoOpRequestTransform;

/**
 * Created by Richard on 9/8/14.
 */
public class ServiceConstants {

    public static final Response<Object> VOID = new Response<Object>() {
        @Override
        public boolean wasErrors() {
            return false;
        }

        @Override
        public void body(Object body) {

        }

        @Override
        public String returnAddress() {
            return "";
        }

        @Override
        public String address() {
            return "";
        }

        @Override
        public long timestamp() {
            return 0;
        }

        @Override
        public long id() {
            return 0;
        }

        @Override
        public Object body() {
            return "VOID";
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    };


    public static final NoOpBeforeMethodCall NO_OP_BEFORE_METHOD_CALL = new NoOpBeforeMethodCall();
    public static final NoOpAfterMethodCall NO_OP_AFTER_METHOD_CALL = new NoOpAfterMethodCall();

    public static final NoOpRequestTransform NO_OP_ARG_TRANSFORM = new NoOpRequestTransform();
}
