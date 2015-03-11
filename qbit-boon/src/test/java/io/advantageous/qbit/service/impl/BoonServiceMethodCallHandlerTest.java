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

package io.advantageous.qbit.service.impl;

import io.advantageous.boon.Lists;
import io.advantageous.boon.Pair;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.bindings.MethodBinding;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;


/**
 * Created by Richard on 9/26/14.
 */
public class BoonServiceMethodCallHandlerTest {


    boolean methodCalled;
    boolean ok;

    @Test
    public void test() {
        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(true);
        impl.init(new Foo(), "", "", null);

        final String address = impl.address();
        Str.equalsOrDie("/boo/baz", address);

        final Collection<String> addresses = impl.addresses();
        ok = addresses.contains("/boo/baz/baaah/pluck") || die(addresses);

        puts(addresses);

        final Map<String, Map<String, Pair<MethodBinding, MethodAccess>>> methodMap = impl.methodMap();

        for (String key : methodMap.keySet()) {
            puts(key);

        }

        final Factory factory = QBit.factory();

        methodCalled = false;
        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/baaah/pluck", null, null, null));

        ok = methodCalled || die();


    }

    @Test
    public void testTwoBasicArgsNotDynamic() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(false);
        impl.init(new Foo(), "", "", null);

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/geoff/chandles/", null, Lists.list("1", 2), null));


        ok = methodCalled || die();

    }

    @Test
    public void testTwoBasicArgs() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(true);
        impl.init(new Foo(), "", "", null);

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/geoff/chandles/", null, Lists.list(1, 2), null));


        ok = methodCalled || die();

    }

    @Test
    public void testTwoBasicArgsInURIParams() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(true);
        impl.init(new Foo(), "", "", null);

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/geoff/chandles/twoargs/5/11/", null, null, null));


        ok = methodCalled || die();

    }

    @Test
    public void someMethod2() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(true);
        impl.init(new Foo(), null, null,null);

        final Factory factory = QBit.factory();

        methodCalled = false;

        MultiMap<String, String> params = new MultiMapImpl<>();
        params.put("methodName", "someMethod2");

        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/beyondHereDontMatter", null,

                Lists.list(1, 99), params));


        ok = methodCalled || die();

    }

    @Test
    public void someMethod3() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(true);
        impl.init(new Foo(), "/root", "/service", null);


        final String address = impl.address();
        Str.equalsOrDie("/root/service", address);

        final Collection<String> addresses = impl.addresses();
        ok = addresses.contains("/root/service/somemethod3") || die(addresses);

        final Factory factory = QBit.factory();

        methodCalled = false;


        impl.receiveMethodCall(factory.createMethodCallByAddress("/root/service/someMethod3/", null,

                Lists.list(1, 99), null));


        ok = methodCalled || die();

    }

    @Test
    public void someMethod4() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(true);
        impl.init(new Foo(), "/root", "/service", null);


        final String address = impl.address();
        Str.equalsOrDie("/root/service", address);

        final Collection<String> addresses = impl.addresses();
        ok = addresses.contains("/root/service/somemethod3") || die(addresses);

        final Factory factory = QBit.factory();

        methodCalled = false;


        final Response<Object> response = impl.receiveMethodCall(factory.createMethodCallByAddress("/root/service/someMethod3/", "returnAddress",

                Lists.list(1, 99), null));

        ok = response != null || die();

        ok = methodCalled || die();

        //void does not return, its void.

    }

    @Test
    public void someMethod4NotDynamic() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler(false);
        impl.init(new Foo(), "/root", "/service", null);


        final String address = impl.address();
        Str.equalsOrDie("/root/service", address);

        final Collection<String> addresses = impl.addresses();
        ok = addresses.contains("/root/service/somemethod3") || die(addresses);

        final Factory factory = QBit.factory();

        methodCalled = false;


        final Response<Object> response = impl.receiveMethodCall(factory.createMethodCallByAddress("/root/service/someMethod3/", "returnAddress",

                Lists.list(1, 99), null));

        ok = response != null || die();

        ok = methodCalled || die();

        //void does not return, its void.

    }

    @RequestMapping("/boo/baz")
    class Foo {

        @RequestMapping("/baaah/pluck")
        public void foo() {

            methodCalled = true;
            puts("foo");
        }


        @RequestMapping("/geoff/chandles/twoargs/{0}/{1}/")
        public void geoff(String a, int b) {

            methodCalled = true;
            puts("geoff a", a, "b", b);
        }

        @RequestMapping("/geoff/chandles/")
        public void someMethod(String a, int b) {

            methodCalled = true;
            puts("geoff");
        }


        public void someMethod2(String a, int b) {

            methodCalled = true;
            puts("geoff", a, b);
        }


        public void someMethod3() {

            methodCalled = true;
        }
    }

}
