package io.advantageous.qbit.example;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.dispatchers.ServiceWorkers;
import io.advantageous.boon.core.Sys;

import static io.advantageous.qbit.service.ServiceBundleBuilder.serviceBundleBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

import java.util.List;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;
import static io.advantageous.qbit.service.dispatchers.ServiceWorkers.shardOnFirstArgumentWorkers;
import static io.advantageous.qbit.service.dispatchers.ServiceWorkers.workers;
imio.advantageous.boontic org.boon.Lists.list;

/**
 * Created by rhightower on 2/20/15.
 */
public class PrototypeMain {

    public static void main(String... args) {


        QBit.factory().systemEventManager();


        final ServiceBundle serviceBundle = serviceBundleBuilder()
                .setAddress("/root").build();


        serviceBundle.start();

        final UserDataServiceClient userDataServiceClient =
                createUserDataServiceClientProxy(serviceBundle, 8);


        final RecommendationServiceClient recommendationServiceClient =
                createRecommendationServiceClientProxy(serviceBundle,
                        userDataServiceClient, 4);




        List<String> userNames = list("Bob", "Joe", "Scott", "William");

        userNames.forEach( userName->
                recommendationServiceClient.recommend(recommendations -> {
                    System.out.println("Recommendations for: " + userName);
                    recommendations.forEach(recommendation->
                            System.out.println("\t" + recommendation));
                }, userName)
        );



        flushServiceProxy(recommendationServiceClient);
        Sys.sleep(1000);

    }

    private static RecommendationServiceClient createRecommendationServiceClientProxy(
            final ServiceBundle serviceBundle,
            final UserDataServiceClient userDataServiceClient,
            int numWorkers) {


        final ServiceWorkers recommendationShardedWorkers = shardOnFirstArgumentWorkers();

        for (int index = 0; index < numWorkers; index++) {
            RecommendationService recommendationServiceImpl =
                    new RecommendationService(userDataServiceClient);

            ServiceQueue serviceQueue = serviceBuilder()
                    .setServiceObject(recommendationServiceImpl)
                    .build();
            serviceQueue.startCallBackHandler();
            recommendationShardedWorkers.addService(serviceQueue);
        }

        recommendationShardedWorkers.start();

        serviceBundle.addServiceConsumer("recomendation", recommendationShardedWorkers);

        return serviceBundle.createLocalProxy(RecommendationServiceClient.class, "recomendation");
    }

    private static UserDataServiceClient createUserDataServiceClientProxy(
            final ServiceBundle serviceBundle,
            final int numWorkers) {
        final ServiceWorkers userDataServiceWorkers = workers();

        for (int index =0; index < numWorkers; index++) {
            ServiceQueue userDataService = serviceBuilder()
                    .setServiceObject(new UserDataService())
                    .build();
            userDataService.startCallBackHandler();
            userDataServiceWorkers.addService(userDataService);
        }

        userDataServiceWorkers.start();



        serviceBundle.addServiceConsumer("workers", userDataServiceWorkers);

        return serviceBundle.createLocalProxy(UserDataServiceClient.class, "workers");
    }
}
