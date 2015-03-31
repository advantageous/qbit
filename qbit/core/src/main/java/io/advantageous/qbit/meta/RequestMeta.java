package io.advantageous.qbit.meta;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.annotation.RequestMethod;

import java.util.Collections;
import java.util.List;

public class RequestMeta {


    public static RequestMeta[] requests(final RequestMeta... requests) {
        return requests;
    }

    public static List<RequestMethod> requestMethods(final RequestMethod... methods) {
        return Lists.list(methods);
    }

    public static RequestMeta requestMeta(final CallType callType,
                       final RequestMethod requestMethod,
                       final String requestURI,
                       final ParameterMeta... parameterMetaList) {

        return new RequestMeta(callType, Collections.singletonList(requestMethod),
                requestURI, Lists.list(parameterMetaList));
    }


    public static RequestMeta request(final CallType callType,
                                          final RequestMethod requestMethod,
                                          final String requestURI,
                                          final ParameterMeta... parameterMetaList) {

        return new RequestMeta(callType, Collections.singletonList(requestMethod),
                requestURI, Lists.list(parameterMetaList));
    }


    public static RequestMeta request(final CallType callType,
                                      final List<RequestMethod> requestMethods,
                                      final String requestURI,
                                      final ParameterMeta... parameterMetaList) {

        return new RequestMeta(callType, requestMethods,
                requestURI, Lists.list(parameterMetaList));
    }


    public static RequestMeta requestByAddress(
                                      final RequestMethod requestMethod,
                                      final String requestURI,
                                      final ParameterMeta... parameterMetaList) {

        if (!requestURI.contains("{")) {

            return new RequestMeta(CallType.ADDRESS,
                    Collections.singletonList(requestMethod),
                    requestURI, Lists.list(parameterMetaList));
        } else {
            return new RequestMeta(CallType.ADDRESS_WITH_PATH_PARAMS,
                    Collections.singletonList(requestMethod),
                    requestURI, Lists.list(parameterMetaList));
        }
    }


    public static RequestMeta getRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {

        return requestByAddress(RequestMethod.GET, requestURI, parameterMetaList);
    }


    public static RequestMeta postRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.POST, requestURI, parameterMetaList);
    }


    public static RequestMeta putRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.PUT, requestURI, parameterMetaList);
    }

    public static RequestMeta deleteRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.DELETE, requestURI, parameterMetaList);
    }


    public static RequestMeta headRequest(
            final String requestURI,
            final ParameterMeta... parameterMetaList) {
        return requestByAddress(RequestMethod.HEAD, requestURI, parameterMetaList);
    }

    private final CallType callType;
    private final String requestURI;
    private final List<ParameterMeta> parameters;
    private final List<RequestMethod> requestMethods;

    public RequestMeta(final CallType callType,
                       final List <RequestMethod> requestMethods,
                       final String requestURI,
                       final List<ParameterMeta> parameterMetaList) {
        this.callType = callType;
        this.requestURI = requestURI;
        this.parameters = parameterMetaList;
        this.requestMethods = requestMethods;
    }

    public CallType getCallType() {
        return callType;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public List<ParameterMeta> getParameters() {
        return parameters;
    }

    public List<RequestMethod> getRequestMethods() {
        return requestMethods;
    }
}
