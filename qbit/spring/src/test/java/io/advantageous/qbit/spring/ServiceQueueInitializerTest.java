package io.advantageous.qbit.spring;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;

import static org.junit.Assert.*;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SpringConfig.class})
@Ignore
public class ServiceQueueInitializerTest {


    @Test
    public void test() {
        //HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort();
    }
}