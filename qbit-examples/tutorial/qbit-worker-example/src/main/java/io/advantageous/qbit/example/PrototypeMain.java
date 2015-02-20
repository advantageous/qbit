package io.advantageous.qbit.example;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceProxyUtils;
import org.boon.Lists;
import org.boon.core.Sys;

import java.util.List;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;

/**
 * Created by rhightower on 2/20/15.
 */
public class PrototypeMain {

    public static void main(String... args) {


        QBit.factory().systemEventManager();


        Service userDataService = serviceBuilder()
                                    .setServiceObject(new UserDataService())
                                    .build().start();

        UserDataServiceClient userDataServiceClient = userDataService
                                .createProxy(UserDataServiceClient.class);


        RecommendationService recommendationServiceImpl =
                new RecommendationService(userDataServiceClient);


        Service recommendationService = serviceBuilder()
                .setServiceObject(recommendationServiceImpl)
                .build().start();

        recommendationService.startCallBackHandler();


        RecommendationServiceClient recommendationServiceClient =
                recommendationService.createProxy(RecommendationServiceClient.class);

        Callback<List<Recommendation>> callback = new Callback<List<Recommendation>>() {
            @Override
            public void accept(List<Recommendation> recommendations) {
                System.out.println("recommendations" + recommendations);
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        };

        recommendationServiceClient.recommend(callback, "Rick");

        ServiceProxyUtils.flushServiceProxy(recommendationServiceClient);
        Sys.sleep(1000);

//        List<String> userNames = Lists.list("Bob", "Joe", "Scott", "William");
//
//        userNames.forEach( userName->
//                recommendationServiceClient.recommend(recommendations -> {
//                    System.out.println("Recommendations for:" + userName);
//                    recommendations.forEach(recommendation->
//                            System.out.println("\t" + recommendation));
//                }, userName)
//        );

        ServiceProxyUtils.flushServiceProxy(recommendationServiceClient);

        Sys.sleep(1000);

    }
}
