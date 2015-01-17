package io.advantageous.qbit.boon;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.client.ServiceProxyFactory;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.EndPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.boon.Exceptions.die;

public class BoonJSONServiceFactoryTest {

    BoonServiceProxyFactory boonJSONServiceFactory;
    ServiceProxyFactory objectUnderTest;

    List<MethodCall<Object>> methodCalls = new ArrayList<>();
    int flushCounter = 0;
    boolean ok;


    public static interface MockServiceInterface {
        void method1();
    }


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