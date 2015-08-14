package io.advantageous.qbit.http.server;

import io.advantageous.qbit.bindings.HttpMethod;
import io.advantageous.qbit.http.config.CorsSupport;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by cmathias on 8/13/15.
 */
public class CorsSupportBuilder {

    CorsSupport corsSupport = new CorsSupport();

    public static CorsSupportBuilder corsSupportBuilder() {
        return new CorsSupportBuilder();
    }

    private CorsSupportBuilder() {
        corsSupport.setCorsOn(true);
    }

    public CorsSupportBuilder withAllowedMethods(HttpMethod... methods) {
        corsSupport.setAllowedMethods(Arrays.asList(methods));
        return this;
    }

    public CorsSupportBuilder withAllowedOrigins(String... origins) {
        corsSupport.setAllowedOrigins(new ArrayList(Arrays.asList(origins)));
        return this;
    }

    public CorsSupportBuilder withAllowedHeaders(String... headers) {
        corsSupport.setAllowedHeaders(new ArrayList(Arrays.asList(headers)));
        return this;
    }

    public CorsSupportBuilder withExposedHeaders(String... headers) {
        corsSupport.setExposedHeaders(new ArrayList(Arrays.asList(headers)));
        return this;
    }

    public CorsSupportBuilder allowCredentials(boolean allow) {
        corsSupport.setAllowCredentials(allow);
        return this;
    }

    public CorsSupport build() {
        return corsSupport;
    }

}
