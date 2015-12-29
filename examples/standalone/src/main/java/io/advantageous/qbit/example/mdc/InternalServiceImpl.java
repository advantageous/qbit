package io.advantageous.qbit.example.mdc;


import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.service.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class InternalServiceImpl {


    private final Logger logger = LoggerFactory.getLogger(InternalServiceImpl.class);
    private final RequestContext requestContext;

    public InternalServiceImpl(final RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public List<String> getCallStack() {

        logger.info("GET CallStack called");

        final Optional<MethodCall<Object>> currentMethodCall = requestContext.getMethodCall();

        if (!currentMethodCall.isPresent()) {
            logger.info("Method not found");
            return Arrays.asList("MethodCall Not Found");
        }

        final List<String> callStack = new ArrayList<>();
        MethodCall<Object> methodCall = currentMethodCall.get();


        callStack.add("Service Call(" + methodCall.objectName()
                + "." + methodCall.name() + ")");

        while (methodCall!=null) {

            final Request<Object> request = methodCall.originatingRequest();
            if (request ==null) {
                methodCall = null;
            } else if (request instanceof MethodCall) {
                methodCall = ((MethodCall<Object>) request);
                callStack.add("Service Call(" + methodCall.objectName()
                        + "." + methodCall.name() + ")");
            } else if (request instanceof HttpRequest) {
                final HttpRequest httpRequest = ((HttpRequest) request);

                callStack.add("REST Call(" + httpRequest.getRemoteAddress()
                        + "." + httpRequest.getUri() + ")");

                methodCall = null;
            } else {
                methodCall = null;
            }
        }

        return callStack;
    }

}
