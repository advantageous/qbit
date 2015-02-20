package io.advantageous.qbit.example;

import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.Service;
import org.boon.Lists;

import java.util.List;

import static io.advantageous.qbit.service.ServiceBuilder.serviceBuilder;

/**
 * Created by rhightower on 2/20/15.
 */
public class PrototypeMain {

    public static void main(String... args) {

        Service userDataService = serviceBuilder()
                                    .setServiceObject(new UserDataService())
                                    .build();

        UserDataServiceClient userDataServiceClient = userDataService
                                .createProxy(UserDataServiceClient.class);


        RecommendationService recommendationServiceImpl =
                new RecommendationService(userDataServiceClient);


        Service recommendationService = serviceBuilder()
                .setServiceObject(recommendationServiceImpl)
                .build();


        RecommendationServiceClient recommendationServiceClient =
                recommendationService.createProxy(RecommendationServiceClient.class);


        userDataService.startCallBackHandler();
        recommendationService.startCallBackHandler();


        List<String> userNames = Lists.list("Bob", "Joe", "Scott", "William");

        userNames.forEach( userName->
                recommendationServiceClient.recommend(recommendations -> {
                    System.out.println("Recommendations for:" + userName);
                    recommendations.forEach(recommendation->
                            System.out.println("\t" + recommendation));
                }, userName)
        );

    }
}
