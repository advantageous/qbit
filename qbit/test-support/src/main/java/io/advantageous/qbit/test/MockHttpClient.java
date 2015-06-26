package io.advantageous.qbit.test;


import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class MockHttpClient implements HttpClient {

    private List<HttpRequest> requests = new ArrayList<>();

    public void sendHttpRequest(final HttpRequest request) {
        requests.add(request);
    }

    @Override
    public void start() {
    }

    public List<HttpRequest> getRequests() {
        return new ArrayList<>(requests);
    }

    public void clear() {
        requests.clear();
    }
}
