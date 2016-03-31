package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.ApiInfo;
import io.advantageous.qbit.meta.swagger.Definition;
import io.advantageous.qbit.meta.swagger.Path;
import io.advantageous.qbit.meta.swagger.ServiceEndpointInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceEndpointInfoBuilder {

    private ApiInfoBuilder apiInfoBuilder;

    /**
     * Specifies the Swagger Specification version being used.
     * It can be used by the Swagger UI and other clients to interpret the API listing.
     * The value MUST be "2.0".
     */
    private String swagger = "2.0";


    /**
     * Required. Provides metadata about the API. The metadata can be used by the clients if needed.
     */
    private ApiInfo info;


    /**
     * The host (name or ip) serving the API. This MUST be the host only and does not include the scheme nor sub-paths. It MAY include a port.
     * If the host is not included, the host serving the documentation is to be used (including the port). The host does not support path templating.
     */
    private String host;

    /**
     * string	The base path on which the API is served, which is relative to the host. If it is not included, the API is served directly under the host. The value MUST start with a leading slash (/). The basePath does not support path templating.
     */
    private String basePath;

    /**
     * The transfer protocol of the API. Values MUST be from the list: "http",
     * "https", "ws", "wss". If the schemes is not included, the default scheme
     * to be used is the one used to access the Swagger definition itself.
     */
    private List<String> schemes;

    /**
     * A list of MIME types the APIs can consume. This is global to all APIs but can be overridden
     * on specific API calls. Value MUST be as described under Mime Types.
     */
    private List<String> consumes;

    /**
     * A list of MIME types the APIs can consume. This is global to all APIs but can be overridden
     * on specific API calls. Value MUST be as described under Mime Types.
     */
    private List<String> produces;

    /**
     * Required. The available paths and operations for the API.
     */
    private Map<String, Path> paths;


    private Map<String, Definition> definitions;


    public Map<String, Definition> getDefinitions() {
        if (definitions == null) {
            definitions = new LinkedHashMap<>();
        }
        return definitions;
    }

    public ServiceEndpointInfoBuilder setDefinitions(Map<String, Definition> definitions) {
        this.definitions = definitions;
        return this;
    }

    public ServiceEndpointInfoBuilder addDefinition(final String name, final Definition definition) {
        getDefinitions().put(name, definition);
        return this;
    }

    public String getSwagger() {
        return swagger;
    }

    public ServiceEndpointInfoBuilder setSwagger(String swagger) {
        this.swagger = swagger;
        return this;
    }

    public ApiInfo getInfo() {
        if (info == null) {
            info = getApiInfoBuilder().build();
        }
        return info;
    }

    public ServiceEndpointInfoBuilder setInfo(ApiInfo info) {
        this.info = info;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ServiceEndpointInfoBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public String getBasePath() {
        return basePath;
    }

    public ServiceEndpointInfoBuilder setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public List<String> getSchemes() {
        if (schemes == null) {
            schemes = new ArrayList<>();
            schemes.add("http");
            schemes.add("https");
            schemes.add("wss");
            schemes.add("ws");
        }
        return schemes;
    }

    public ServiceEndpointInfoBuilder setSchemes(List<String> schemes) {
        this.schemes = schemes;
        return this;
    }

    public List<String> getConsumes() {
        if (consumes == null) {
            consumes = new ArrayList<>();
            consumes.add("application/json");
        }
        return consumes;
    }

    public ServiceEndpointInfoBuilder setConsumes(List<String> consumes) {
        this.consumes = consumes;
        return this;
    }

    public List<String> getProduces() {
        if (produces == null) {
            produces = new ArrayList<>();
            produces.add("application/json");
        }
        return produces;
    }

    public ServiceEndpointInfoBuilder setProduces(List<String> produces) {
        this.produces = produces;
        return this;
    }

    public Map<String, Path> getPaths() {
        if (paths == null) {
            paths = new LinkedHashMap<>();
        }
        return paths;
    }

    public ServiceEndpointInfoBuilder setPaths(Map<String, Path> paths) {
        this.paths = paths;
        return this;
    }

    public ServiceEndpointInfoBuilder addPath(String uri, Path path) {
        getPaths().put(uri, path);
        return this;
    }

    public ApiInfoBuilder getApiInfoBuilder() {
        if (apiInfoBuilder == null) {
            apiInfoBuilder = new ApiInfoBuilder();
        }
        return apiInfoBuilder;
    }

    public void setApiInfoBuilder(ApiInfoBuilder apiInfoBuilder) {
        this.apiInfoBuilder = apiInfoBuilder;
    }

    public ServiceEndpointInfo build() {
        return new ServiceEndpointInfo(getInfo(), getHost(),
                getBasePath(), getPaths(), getSchemes(),
                getConsumes(), getProduces(), getDefinitions());
    }
}
