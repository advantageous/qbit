package org.qbit.service.impl;

import org.boon.Lists;
import org.boon.Pair;
import org.boon.Str;
import org.boon.collections.MultiMap;
import org.boon.core.reflection.MethodAccess;
import org.junit.Test;
import org.qbit.Factory;
import org.qbit.QBit;
import org.qbit.annotation.RequestMapping;
import org.qbit.bindings.MethodBinding;

import java.util.List;
import java.util.Map;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by Richard on 9/26/14.
 */
public class ServiceMethodCallHandlerImplTest {


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

    }

    boolean ok;

    @Test
    public void test() {
        ServiceMethodCallHandlerImpl impl = new ServiceMethodCallHandlerImpl();
        impl.init(new Foo());

        final String address = impl.address();
        Str.equalsOrDie("/boo/baz", address);

        final List<String> addresses = impl.addresses();
        ok = addresses.contains("/boo/baz/baaah/pluck") || die(addresses);

        puts (addresses);

        final Map<String, Pair<MethodBinding, MethodAccess>> methodMap = impl.methodMap();

        for (String key : methodMap.keySet()) {
            puts(key);
            final Pair<MethodBinding, MethodAccess> methodBindingMethodAccessPair = methodMap.get(key);
            final MethodBinding binding = methodBindingMethodAccessPair.getFirst();
            final MethodAccess methodAccess = methodBindingMethodAccessPair.getSecond();
            puts("BINDING", binding);
            puts("PARAMS", binding.parameters());

            puts("METHOD", methodAccess);

        }

        final Factory factory = QBit.factory();

        methodCalled = false;
        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/baaah/pluck", null, null));

        ok = methodCalled == true || die();


    }

    @Test
    public void testTwoBasicArgs() {

        ServiceMethodCallHandlerImpl impl = new ServiceMethodCallHandlerImpl();
        impl.init(new Foo());

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(factory.createMethodCallByAddress("/boo/baz/geoff/chandles/",
                Lists.list(1, 2), null));


        ok = methodCalled || die();

    }

    @Test
    public void testTwoBasicArgsInURIParams() {

        ServiceMethodCallHandlerImpl impl = new ServiceMethodCallHandlerImpl();
        impl.init(new Foo());

        final Factory factory = QBit.factory();

        methodCalled = false;

        impl.receiveMethodCall(factory.createMethodCallByAddress("/geoff/chandles/twoargs/5/11/", null, null));


        ok = methodCalled || die();

    }

    @Test
    public void someMethod2() {

        ServiceMethodCallHandlerImpl impl = new ServiceMethodCallHandlerImpl();
        impl.init(new Foo());

        final Factory factory = QBit.factory();

        methodCalled = false;

        MultiMap<String, String> params = new MultiMap<>();
        params.put("methodName", "someMethod2");

        impl.receiveMethodCall(factory.createMethodCallByAddress("/geoff/chandles/twoargs/5/11/",

                Lists.list(1, 99), params));


        ok = methodCalled || die();

    }
}
