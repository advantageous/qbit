package io.advantageous.qbit.http.interceptor;

import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.service.RequestContext;

import java.util.Optional;

public class ForwardCallMethodInterceptor implements BeforeMethodSent {

    final RequestContext requestContext;

    public ForwardCallMethodInterceptor(final RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public void beforeMethodSent(final MethodCallBuilder methodBuilder) {

        if (methodBuilder.getOriginatingRequest() == null) {
            final Optional<Request> request = requestContext.getRequest();
            if (request.isPresent()) {
                methodBuilder.setOriginatingRequest((Request<Object>)request.get());
            }
        }
    }
}
