package io.advantageous.consul.endpoints;

import io.advantageous.boon.core.Str;

import java.net.URI;

public class Endpoint {


    private final String rootPath;
    private final String scheme;
    private final String port;
    private final String host;


    public Endpoint(final String scheme, final String host, final String port, final String rootPath) {

        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.rootPath = rootPath;
    }


    public Endpoint(final URI rootURI, final String rootPath) {

        this(rootURI.getScheme(), rootURI.getHost(), "" + rootURI.getPort(), rootPath);
    }

    protected URI createURI(String path) {

        URI uri = URI.create(Str.add(scheme, "://", host, ":", port, rootPath, path));
        return uri;
    }


}
