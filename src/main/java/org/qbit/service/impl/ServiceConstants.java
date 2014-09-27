package org.qbit.service.impl;

import org.qbit.message.Response;

/**
 * Created by Richard on 9/8/14.
 */
public class ServiceConstants {

    public static final Response<Object> VOID = new Response<Object>() {
        @Override
        public void body(Object body) {

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
}
