package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.service.impl.BoonServiceMethodCallHandler;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.bindings.MethodBinding;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.Lists;
import org.boon.Pair;
import org.boon.Str;
import org.boon.core.reflection.MethodAccess;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class BoonServiceMethodCallHandlerTest {


    static {
        RegisterBoonWithQBit.registerBoonWithQBit();

    }

    boolean methodCalled;

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

    boolean ok;

    @Test
    public void test() {
        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler();
        impl.init(new Foo(), "", "");

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

        ok = methodCalled == true || die();


    }

    @Test
    public void testTwoBasicArgs() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler();
        impl.init(new Foo(), "", "");

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(
                factory.createMethodCallByAddress("/boo/baz/geoff/chandles/", null,
                        Lists.list(1, 2), null));


        ok = methodCalled || die();

    }

    @Test
    public void testTwoBasicArgsInURIParams() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler();
        impl.init(new Foo(), "", "");

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(
                factory.createMethodCallByAddress(
                        "/boo/baz/geoff/chandles/twoargs/5/11/", null, null, null));


        ok = methodCalled || die();

    }

    @Test
    public void someMethod2() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler();
        impl.init(new Foo(), null, null);

        final Factory factory = QBit.factory();

        methodCalled = false;

        MultiMap<String, String> params = new MultiMapImpl<>();
        params.put("methodName", "someMethod2");

        impl.receiveMethodCall(
                factory.createMethodCallByAddress("/boo/baz/beyondHereDontMatter", null,

                        Lists.list(1, 99), params));


        ok = methodCalled || die();

    }


    @Test
    public void someMethod3() {

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler();
        impl.init(new Foo(), "/root", "/service");


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

        BoonServiceMethodCallHandler impl = new BoonServiceMethodCallHandler();
        impl.init(new Foo(), "/root", "/service");


        final String address = impl.address();
        Str.equalsOrDie("/root/service", address);

        final Collection<String> addresses = impl.addresses();
        ok = addresses.contains("/root/service/somemethod3") || die(addresses);

        final Factory factory = QBit.factory();

        methodCalled = false;


        final Response<Object> response = impl.receiveMethodCall(
                factory.createMethodCallByAddress(
                        "/root/service/someMethod3/",
                        "returnAddress",

                        Lists.list(1, 99), null));

        ok = response != null || die();

        ok = methodCalled || die();

        //void does not return, its void.

    }

}
