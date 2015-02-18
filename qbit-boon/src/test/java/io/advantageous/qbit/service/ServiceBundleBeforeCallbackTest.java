/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.service;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.util.MultiMap;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/19/15.
 */
public class ServiceBundleBeforeCallbackTest {


    boolean ok;

    volatile boolean called;

    volatile boolean beforeHandlerCalled;

    MultiMap<String, String> params = null;

    @Before
    public void setup() {
        called = false;
        beforeHandlerCalled = false;
    }

    @Test
    public void testRejectCall() {
        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setBeforeMethodCall(new BeforeMethodCall() {
            @Override
            public boolean before(MethodCall call) {
                beforeHandlerCalled = true;
                return false;
            }
        }).buildAndStart();


        serviceBundle.addService(new MockServer());

        final MethodCall<Object> method = QBit.factory().createMethodCallByAddress("/services/mockserver/callme", "", Collections.emptyList(), params);

        serviceBundle.call(method);

        serviceBundle.flush();


        Sys.sleep(100);

        ok = !called || die();

        ok = beforeHandlerCalled || die();


    }

    @Test
    public void testAllowCall() {
        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setBeforeMethodCall(new BeforeMethodCall() {
            @Override
            public boolean before(MethodCall call) {
                beforeHandlerCalled = true;
                return true;
            }
        }).buildAndStart();


        serviceBundle.addService(new MockServer());

        serviceBundle.startReturnHandlerProcessor();

        final MethodCall<Object> method = QBit.factory().createMethodCallByAddress("/services/mockserver/callme", "", Collections.emptyList(), params);

        serviceBundle.call(method);

        serviceBundle.flush();


        Sys.sleep(100);

        ok = called || die();

        ok = beforeHandlerCalled || die();


    }

    public class MockServer {

        public void callme() {
            called = true;

        }
    }
}
