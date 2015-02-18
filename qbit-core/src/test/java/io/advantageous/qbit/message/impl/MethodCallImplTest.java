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

package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class MethodCallImplTest {

    MethodCall<Object> methodCall1;

    MethodCall<Object> methodCall2;

    boolean ok;

    @Before
    public void setUp() throws Exception {

        methodCall1 = new MethodCallBuilder()
                .setAddress("address")
                .setReturnAddress("return")
                .setBody("body")
                .setId(1L)
                .setName("name")
                .setTimestamp(2L)
                .setObjectName("objectName")
                .build();


        methodCall2 = new MethodCallBuilder()
                .setAddress("address")
                .setReturnAddress("return")
                .setBody("body")
                .setId(1L)
                .setName("name")
                .setTimestamp(2L)
                .setObjectName("objectName")
                .build();

    }

    @Test
    public void test() throws Exception {

        ok = methodCall1.address().equals(methodCall2.address()) || die();
        ok = methodCall1.returnAddress().equals(methodCall2.returnAddress()) || die();
        ok = methodCall1.name().equals(methodCall2.name()) || die();
        ok = methodCall1.objectName().equals(methodCall2.objectName()) || die();
        ok = methodCall1.isHandled() == methodCall2.isHandled() || die();
        ok = methodCall1.isSingleton() == methodCall2.isSingleton() || die();
        ok = methodCall1.id() == methodCall2.id() || die();
        ok = methodCall1.timestamp() == methodCall2.timestamp() || die();

        ok = methodCall1.hashCode() == methodCall2.hashCode() || die();

        ok = methodCall1.equals(methodCall2) || die();
        methodCall1.handled();
        methodCall1.originatingRequest();
        puts(methodCall1);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testName() throws Exception {

    }
}