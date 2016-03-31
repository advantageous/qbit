package io.advantageous.qbit.meta.swagger;

import java.util.List;
import java.util.Map;

/**
 * This is like the swagger object.
 * https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md
 */
public class ServiceEndpointInfo {

    /**
     * Specifies the Swagger Specification version being used.
     * It can be used by the Swagger UI and other clients to interpret the API listing.
     * The value MUST be "2.0".
     */
    private final String swagger = "2.0";


    /**
     * Required. Provides metadata about the API. The metadata can be used by the clients if needed.
     */
    private final ApiInfo info;


    /**
     * The host (name or ip) serving the API. This MUST be the host only and does not include the scheme nor sub-paths. It MAY include a port.
     * If the host is not included, the host serving the documentation is to be used (including the port). The host does not support path templating.
     */
    private final String host;

    /**
     * string	The base path on which the API is served, which is relative to the host. If it is not included, the API is served directly under the host. The value MUST start with a leading slash (/). The basePath does not support path templating.
     */
    private final String basePath;

    /**
     * The transfer protocol of the API. Values MUST be from the list: "http",
     * "https", "ws", "wss". If the schemes is not included, the default scheme
     * to be used is the one used to access the Swagger definition itself.
     */
    private final List<String> schemes;


    /**
     * A list of MIME types the APIs can consume. This is global to all APIs but can be overridden
     * on specific API calls. Value MUST be as described under Mime Types.
     */
    private final List<String> consumes;
    private final Map<String, Definition> definitions;
    /**
     * Required. The available paths and operations for the API.
     */
    private final Map<String, Path> paths;
    /**
     * A list of MIME types the APIs can consume. This is global to all APIs but can be overridden
     * on specific API calls. Value MUST be as described under Mime Types.
     */
    private List<String> produces;

    public ServiceEndpointInfo(ApiInfo info, String host, String basePath, Map<String, Path> paths,
                               List<String> schemes,
                               List<String> produces, List<String> consumes, Map<String, Definition> definitions) {

        this.schemes = schemes;
        this.produces = produces;
        this.consumes = consumes;
        this.info = info;
        this.host = host;
        this.basePath = basePath;
        this.paths = paths;
        this.definitions = definitions;
    }

    public String getSwagger() {
        return swagger;
    }

    public ApiInfo getInfo() {
        return info;
    }

    public String getHost() {
        return host;
    }

    public String getBasePath() {
        return basePath;
    }

    public List<String> getSchemes() {
        return schemes;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public List<String> getProduces() {
        return produces;
    }

    public Map<String, Definition> getDefinitions() {
        return definitions;
    }

    public Map<String, Path> getPaths() {
        return paths;
    }
}
