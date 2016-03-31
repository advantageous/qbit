package io.advantageous.qbit.vertx.bugs;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextReceiver;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.boon.core.IO.puts;

public class Bug458 {


    @Test
    @Ignore
    public void testWeb() {

        final EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder().setUri("/");

        endpointServerBuilder.getRequestQueueBuilder().setBatchSize(1000).setLinkTransferQueue().setCheckEvery(100).setPollWait(10);
        endpointServerBuilder.getResponseQueueBuilder().setBatchSize(1000).setLinkTransferQueue().setCheckEvery(100).setPollWait(10);

        endpointServerBuilder.addService(new HRService()).build().startServerAndWait();


        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPoolSize(1_000).buildAndStart();

        final HttpTextResponse response = httpClient.postJson("/hr/emp", "{\"name\": \"Rick\"}");

        System.out.println(response.body());


        for (int x = 0; x < 100; x++) {

            final AtomicInteger count = new AtomicInteger();

            final int totalCount = 60_000;
            final long startTime = System.currentTimeMillis();
            for (int index = 0; index < totalCount; index++) {
                httpClient.sendJsonPostAsync("/hr/emp", "{\"name\": \"Rick\"}", new HttpTextReceiver() {
                    @Override
                    public void response(int code, String contentType, String body) {
                        if (code == 200) {
                            count.incrementAndGet();
                        }
                    }
                });
            }

            while (count.get() < totalCount) {
                Sys.sleep(10);
            }

            puts(count, System.currentTimeMillis() - startTime);
        }

        System.out.println("DONE");

    }

    @Test
    @Ignore
    public void test() {

        final ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder().setInvokeDynamic(false);
        serviceBundleBuilder.getRequestQueueBuilder().setBatchSize(1000).setLinkTransferQueue().setCheckEvery(100).setPollWait(10);
        serviceBundleBuilder.getResponseQueueBuilder().setBatchSize(1000).setLinkTransferQueue().setCheckEvery(100).setPollWait(10);
        //serviceBundleBuilder.getRequestQueueBuilder().setBatchSize(100);//.setBatchSize(1000).setLinkTransferQueue().setCheckEvery(100);
        //serviceBundleBuilder.getResponseQueueBuilder().setBatchSize(100);//.setBatchSize(1000).setLinkTransferQueue().setCheckEvery(100);

        final ServiceBundle serviceBundle = serviceBundleBuilder.build().startServiceBundle();

        serviceBundle.addServiceObject("hr", new HRService());

        for (int x = 0; x < 100; x++) {

            final IHRService proxy = serviceBundle.createLocalProxy(IHRService.class, "hr");

            final AtomicInteger count = new AtomicInteger();

            final int totalCount = 2_000_000;
            for (int index = 0; index < totalCount; index++) {
                proxy.getAddEmployee(employees -> count.incrementAndGet(), new Employee("Bob"));
            }

            ServiceProxyUtils.flushServiceProxy(proxy);

            while (count.get() < totalCount) {
                Sys.sleep(100);
            }

            System.out.println(count);
        }
    }

    public interface IHRService {

        void getAddEmployee(Callback<List<Employee>> callback, Employee employee);
    }

    public class Employee {
        final String name;

        public Employee(String name) {
            this.name = name;
        }
    }

    @RequestMapping("/hr")
    public class HRService {

        @RequestMapping(value = "/emp", method = RequestMethod.POST)
        public List<Employee> getAddEmployee(Employee employee) {
            return Lists.list(new Employee("Rick"));
        }
    }

}
