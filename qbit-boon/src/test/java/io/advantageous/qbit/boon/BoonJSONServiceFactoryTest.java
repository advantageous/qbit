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

package io.advantageous.qbit.boon;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.EndPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.Exceptions.die;

public class BoonJSONServiceFactoryTest {

    BoonServiceProxyFactory boonJSONServiceFactory;
    ServiceProxyFactory objectUnderTest;

    List<MethodCall<Object>> methodCalls = new ArrayList<>();
    int flushCounter = 0;
    boolean ok;

    @Before
    public void setup() {
        boonJSONServiceFactory = new BoonServiceProxyFactory(QBit.factory());
        objectUnderTest = boonJSONServiceFactory;
        methodCalls = new ArrayList<>();
        flushCounter = 0;
        ok = true;
    }

    @Test
    public void testCreateProxy() throws Exception {


        final MockServiceInterface service = boonJSONServiceFactory.createProxy(MockServiceInterface.class, "testService", new EndPointMock());
        service.method1();


        ok |= methodCalls.size() == 1 || die();

        final MethodCall<Object> methodCall = methodCalls.get(0);

        ok |= methodCall.name().equals("method1");


    }


    public static interface MockServiceInterface {
        void method1();
    }

    public class EndPointMock implements EndPoint {

        @Override
        public String address() {
            return "mock";
        }

        @Override
        public void call(MethodCall<Object> methodCall) {
            methodCalls.add(methodCall);

        }

        @Override
        public void call(List<MethodCall<Object>> methodCalls) {

            methodCalls.addAll(methodCalls);
        }

        @Override
        public void flush() {
            flushCounter++;
        }
    }
}