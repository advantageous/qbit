package io.advantageous.qbit.http.server;

import io.advantageous.qbit.http.request.HttpRequest;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class RequestContinuePredicate implements Predicate<HttpRequest> {

    private final CopyOnWriteArrayList<Predicate<HttpRequest>> predicates = new CopyOnWriteArrayList<>();

    public RequestContinuePredicate add(final Predicate<HttpRequest> predicate) {
        predicates.add(predicate);
        return this;
    }

    @Override
    public boolean test(final HttpRequest httpRequest) {
        boolean shouldContinue;

        for (Predicate<HttpRequest> shouldContinuePredicate : predicates) {
            shouldContinue = shouldContinuePredicate.test(httpRequest);
            if (!shouldContinue) {
                return false;
            }
        }
        return true;
    }
}
