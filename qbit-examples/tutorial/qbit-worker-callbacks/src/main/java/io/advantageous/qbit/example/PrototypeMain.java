package io.advantageous.qbit.example;

import io.advantageous.qbit.QBit;
//import io.advantageous.qbit.service.Callback;
//import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
//import io.advantageous.boon.Liio.advantageous.boonort org.boon.core.Sys;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;
//import static java.lang.System.out;

import java.util.List;

import static io.advantageous.qbit.service.ServiceBuilder.servicio.advantageous.boon;
import static org.boon.Lists.list;

/**
 * Created by rhightower on 2/20/15.
 */
public class PrototypeMain {

    public static void main(String... args) {


        QBit.factory().systemEventManager();


        ServiceQueue userDataService = serviceBuilder()
                                    .setServiceObject(new UserDataService())
                                    .build().start();

        userDataService.startCallBackHandler();

        UserDataServiceClient userDataServiceClient = userDataService
                                .createProxy(UserDataServiceClient.class);



        /* Not using userDataService part 1. */
//
//        RecommendationService recommendationServiceImpl =
//                new RecommendationService(userDataServiceClient);

        RecommendationService recommendationServiceImpl =
                new RecommendationService(userDataServiceClient);


        ServiceQueue recommendationServiceQueue = serviceBuilder()
                .setServiceObject(recommendationServiceImpl)
                .build().start().startCallBackHandler();

        RecommendationServiceClient recommendationServiceClient =
                recommendationServiceQueue.createProxy(RecommendationServiceClient.class);

        /**** NO LAMBDA ****/
//        Callback<List<Recommendation>> callback = new Callback<List<Recommendation>>() {
//            @Override
//            public void accept(List<Recommendation> recommendations) {
//                System.out.println("recommendations " + recommendations);
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                error.printStackTrace();
//            }
//        };
//
//        recommendationServiceClient.recommend(callback, "Rick");


        //import static java.lang.System.out;

        //recommendationServiceClient.recommend(out::println, "Rick");

        flushServiceProxy(recommendationServiceClient);
        Sys.sleep(1000);

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
}
