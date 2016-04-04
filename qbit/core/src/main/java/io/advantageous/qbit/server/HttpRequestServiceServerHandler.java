package io.advantageous.qbit.server;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.service.Startable;

public interface HttpRequestServiceServerHandler extends Startable {
    void httpRequestQueueIdle(Void v);

    void handleRestCall(HttpRequest request);

    void addRestSupportFor(Class cls, String baseURI);

    void checkTimeoutsForRequests();

    void handleResponseFromServiceToHttpResponse(Response<Object> response, HttpRequest originatingRequest);

    void addRestSupportFor(String alias, Class<?> aClass, String address1);
}
