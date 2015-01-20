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

    public class MockServer {

        public void callme() {
            called = true;

        }
    }

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
        }).build();


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
        }).build();


        serviceBundle.addService(new MockServer());

        serviceBundle.startReturnHandlerProcessor();

        final MethodCall<Object> method = QBit.factory().createMethodCallByAddress("/services/mockserver/callme", "", Collections.emptyList(), params);

        serviceBundle.call(method);

        serviceBundle.flush();


        Sys.sleep(100);

        ok = called || die();

        ok = beforeHandlerCalled || die();



    }
}
