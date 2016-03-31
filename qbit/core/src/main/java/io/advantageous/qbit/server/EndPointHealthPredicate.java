package io.advantageous.qbit.server;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.stats.StatCollection;

import java.util.function.Predicate;

public class EndPointHealthPredicate implements Predicate<HttpRequest> {

    private final boolean healthEnabled;
    private final boolean statsEnabled;
    private final HealthServiceAsync healthServiceAsync;
    private final StatCollection statCollection;


    public EndPointHealthPredicate(boolean healthEnabled, boolean statsEnabled,
                                   HealthServiceAsync healthServiceAsync, StatCollection statCollection
    ) {
        this.healthEnabled = healthEnabled;
        this.statsEnabled = statsEnabled;
        this.healthServiceAsync = healthServiceAsync;
        this.statCollection = statCollection;
    }

    @Override
    public boolean test(final HttpRequest httpRequest) {

        boolean shouldContinue = true;
        if (healthEnabled && httpRequest.getUri().startsWith("/__health")) {
            healthServiceAsync.ok(ok -> {
                if (ok) {
                    httpRequest.getReceiver().respondOK("\"ok\"");
                } else {
                    httpRequest.getReceiver().error("\"fail\"");
                }
            });
            shouldContinue = false;
        } else if (statsEnabled && httpRequest.getUri().startsWith("/__stats")) {

            if (httpRequest.getUri().equals("/__stats/instance")) {
                if (statCollection != null) {
                    statCollection.collect(stats -> {
                        String json = JsonFactory.toJson(stats);
                        httpRequest.getReceiver().respondOK(json);
                    });
                } else {
                    httpRequest.getReceiver().error("\"failed to load stats collector\"");
                }
            } else if (httpRequest.getUri().equals("/__stats/global")) {
                /* We don't support global stats, yet. */
                httpRequest.getReceiver().respondOK("{\"version\":1}");
            } else {

                httpRequest.getReceiver().notFound();
            }
            shouldContinue = false;
        }

        return shouldContinue;

    }
}
