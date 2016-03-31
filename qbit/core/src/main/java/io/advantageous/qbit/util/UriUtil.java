package io.advantageous.qbit.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {

    public static URI parseURI(final String string) {
        return URI.create(string);
    }

    public static URI createURI(final String scheme, final String host, final int port) {
        return createURIWithFull(scheme, null, host, port, null, null, null);
    }

    public static URI createURIWithPath(final String scheme, final String host, final int port, final String path) {
        return createURIWithFull(scheme, null, host, port, path, null, null);
    }

    public static URI createURIWithFull(final String scheme,
                                        final String userInfo, final String host, final int port,
                                        final String path, final String query, final String fragment) {
        try {
            return new URI(scheme, userInfo, host, port, path, query, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to create URI", e);
        }
    }
}
