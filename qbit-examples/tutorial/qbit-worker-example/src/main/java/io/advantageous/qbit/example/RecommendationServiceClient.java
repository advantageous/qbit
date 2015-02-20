package io.advantageous.qbit.example;

import io.advantageous.qbit.service.Callback;

import java.util.List;

/**
 * Created by rhightower on 2/20/15.
 */
public interface RecommendationServiceClient {


    void recommend(final Callback<List<Recommendation>> recommendationsCallback,
                          final String userName);
}
