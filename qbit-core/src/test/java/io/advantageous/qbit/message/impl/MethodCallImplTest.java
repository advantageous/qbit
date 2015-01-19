package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

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