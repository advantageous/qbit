package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.util.MultiMap;
import org.boon.Boon;
import org.boon.Lists;
import org.boon.Str;
import org.boon.core.Conversions;
import org.boon.core.Sys;
import org.junit.Before;
import org.junit.Test;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.service.ServiceBundle;

import java.util.List;

import static org.boon.Boon.puts;

public class ServiceBundleImplTest {


    ServiceBundle serviceBundle;
    ServiceBundleImpl serviceBundleImpl;
    AdderService adderService;

    Factory factory;
    MultiMap<String, String> params = null;
    MethodCall<Object> call = null;


    ReceiveQueue<Response<Object>> responseReceiveQueue = null;

    Response<Object> response;

    Object responseBody = null;




    public static class AdderService {
        int sum;
        public int add(int a, int b) {

            puts("ADDER SERVICE CALLED", a, b);
            sum += (a+b);
            return a+b;
        }
    }


    @Before
    public void before() {

        factory = QBit.factory();
        final ServiceBundle bundle = factory.createServiceBundle("/foo");
        serviceBundle = bundle;
        serviceBundleImpl = (ServiceBundleImpl) bundle;
        adderService = new AdderService();
    }

    @Test
    public void testAddress() throws Exception {

        Str.equalsOrDie("/foo", serviceBundle.address());

    }

    @Test
    public void testAddService() throws Exception {

        serviceBundle.addService("/adder", adderService);
        final List<String> endPoints = serviceBundle.endPoints();
        puts(endPoints);
        endPoints.contains("/foo/adder");
    }

    @Test
    public void testResponses() throws Exception {

        call = factory.createMethodCallByAddress("/foo/adder/add", "", Lists.list(1, 2), params);
        serviceBundle.addService("/adder", adderService);

        serviceBundle.call(call);

        serviceBundle.flushSends();

        Sys.sleep(1000);

        responseReceiveQueue = serviceBundle.responses().receiveQueue();

        serviceBundle.flush();

        Sys.sleep(200);

        response = responseReceiveQueue.pollWait();

        responseBody = response.body();

        int sum = Conversions.toInt(responseBody);

        Boon.equalsOrDie("Sum should be 3", 3, sum);

        serviceBundle.stop();
    }

    @Test
    public void testCall() throws Exception {

    }
}
