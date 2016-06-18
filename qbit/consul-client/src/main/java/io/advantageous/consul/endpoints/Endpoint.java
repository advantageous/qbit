package io.advantageous.consul.endpoints;

import io.advantageous.boon.core.Str;
import io.advantageous.qbit.json.JsonMapper;

import java.net.URI;
import java.util.List;

public class Endpoint {


    protected final JsonMapper mapper;
    private final String rootPath;
    private final String scheme;
    private final String port;
    private final String host;


    public Endpoint(final String scheme, final String host, final String port, final String rootPath, final JsonMapper mapper) {

        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.rootPath = rootPath;
        this.mapper = mapper;
    }

    public Endpoint(final URI rootURI, final String rootPath, final JsonMapper mapper) {

        this(rootURI.getScheme(), rootURI.getHost(), "" + rootURI.getPort(), rootPath, mapper);
    }

    protected String toJson(Object object) {
        return mapper.toJson(object);
    }

    protected <T> T fromJson(String json, Class<T> cls) {
        return mapper.fromJson(json, cls);
    }

    protected <T> List<T> fromJsonArray(String json, Class<T> componentClass) {
        return mapper.fromJsonArray(json, componentClass);
    }

    protected URI createURI(String path) {

        URI uri = URI.create(Str.add(scheme, "://", host, ":", port, rootPath, path));
        return uri;
    }


}
