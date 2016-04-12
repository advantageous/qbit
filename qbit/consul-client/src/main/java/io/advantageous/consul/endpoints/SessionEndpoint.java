package io.advantageous.consul.endpoints;

import io.advantageous.consul.domain.Session;
import io.advantageous.consul.domain.SessionId;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.http.request.HttpRequestBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static io.advantageous.boon.json.JsonFactory.*;
import static io.advantageous.consul.domain.ConsulException.die;

public class SessionEndpoint extends Endpoint {

    public SessionEndpoint(String scheme, String host, String port, String rootPath) {
        super(scheme, host, port, rootPath);
    }

    public SessionEndpoint(URI rootURI, String rootPath) {
        super(rootURI, rootPath);
    }


    /**
     * The create endpoint is used to initialize a new session.
     * Sessions must be associated with a node and may be associated with any number of checks.
     * The create endpoint expects a JSON request body to be PUT. The request body must look like:
     * <p>
     * <pre>
     * <code>
     * {
     * "LockDelay": "15s",
     * "Name": "my-service-lock",
     * "Node": "foobar",
     * "Checks": ["a", "b", "c"],
     * "Behavior": "release",
     * "TTL": "0s"
     * }
     * </code>
     * </pre>
     * </p>
     * None of the fields are mandatory, and in fact no body needs to be PUT if the defaults are to be used.
     * By default, the agent's local datacenter is used; another datacenter can be specified using
     * the "?dc=" query parameter. However, it is not recommended to use cross-datacenter sessions.
     * /v1/session/create: Creates a new session
     *
     * @param session    session to create
     * @param datacenter datacenter
     * @return a session id
     */
    public String create(Session session, final String datacenter) {


        final URI uri = createURI("/create");
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, null, null, "");


        HTTP.Response httpResponse = HTTP.jsonRestCallViaPUT(uri.toString() + "?" + httpRequestBuilder.paramString(),
                toJson(session));

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable to create the session", uri, httpResponse);
        }

        final String id = fromJson(httpResponse.body(), SessionId.class).getId();
        session.setId(id);
        return id;
    }

    /**
     * @param session session
     * @return sessionId session id
     */
    public String create(Session session) {

        return create(session, null);
    }


    /**
     * The destroy endpoint is hit with a PUT and destroys the given session.
     * <p>
     * By default, the local datacenter is used, but the "?dc=" query parameter
     * can be used to specify the datacenter.
     * <p>
     * The session being destroyed must be provided on the path.
     * The return code is 200 on success.
     * /v1/session/destroy/<session>: Destroys a given session
     *
     * @param sessionId sessionId
     * @return true if success
     */
    public boolean destroy(String sessionId, final Session session) {

        return destroy(sessionId, session, null);
    }

    /**
     * @param datacenter datacenter
     * @return true if success
     */
    public boolean destroy(final String sessionId, final Session session, final String datacenter) {

        final URI uri = createURI("/destroy/" + sessionId);
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, null, null, "");


        HTTP.Response httpResponse = HTTP.jsonRestCallViaPUT(uri.toString() + "?" + httpRequestBuilder.paramString(),
                toJson(session));

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable destroy the session", sessionId, uri, httpResponse);
        }

        return httpResponse.code() == 200;
    }


    /**
     * List all active sessions
     *
     * @return active sessions
     */
    public List<Session> getSessions() {

        return getSessions(null, RequestOptions.BLANK);
    }


    /**
     * Lists all active sessions
     * /v1/session/list:
     * <p>
     * This endpoint is hit with a GET and returns the active sessions for a given datacenter. By default,
     * the datacenter of the agent is queried; however, the dc can be provided using the "?dc=" query parameter.
     * This endpoint supports blocking queries and all consistency modes.
     *
     * @param requestOptions request options for consistency and long poll
     * @return list of sessions
     */
    public List<Session> getSessions(final RequestOptions requestOptions) {

        return getSessions(null, requestOptions);
    }


    /**
     * @param datacenter     datacenter
     * @param requestOptions request options for long poll and consistency.
     * @return list of sessions
     */
    public List<Session> getSessions(final String datacenter, final RequestOptions requestOptions) {

        final URI uri = createURI("/list");
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, null, requestOptions, "");

        HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable to get the sessions", uri, httpResponse);
        }

        return fromJsonArray(httpResponse.body(), Session.class);

    }

    /**
     * The renew endpoint is hit with a PUT and renews the given session.
     * This is used with sessions that have a TTL, and it extends the expiration by the TTL.
     * By default, the local datacenter is used, but the "?dc=" query parameter can be used to
     * specify the datacenter.
     * <p>
     * The session being renewed must be provided on the path.
     * <p>
     * The return code is 200 on success.
     * The response JSON body is a single Session in a list.
     * <p>
     * /v1/session/renew: Renews a TTL-based session
     *
     * @return session
     */
    public Session renew(final String sessionId) {

        return renew(sessionId, null);
    }

    /**
     * /v1/session/renew: Renews a TTL-based session
     *
     * @param datacenter datacenter
     * @return session
     */
    public Session renew(final String sessionId, final String datacenter) {

        final URI uri = createURI("/renew/" + sessionId);
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, null, null, "");


        HTTP.Response httpResponse = HTTP.jsonRestCallViaPUT(uri.toString() + "?" + httpRequestBuilder.paramString(),
                "");

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable to renew the session", uri, httpResponse);
        }

        System.out.println(httpResponse.body());
        return fromJsonArray(httpResponse.body(), Session.class).get(0);
    }


    /**
     * This endpoint is hit with a GET and returns the requested session information
     * within a given datacenter. By default, the datacenter of the agent is queried; however,
     * the dc can be provided using the "?dc=" query parameter.
     * <p>
     * The session being queried must be provided on the path.
     * It returns a JSON body like this:
     * <pre>
     * <code>
     * [{
     * "LockDelay": 1.5e+10,
     * "Checks": [
     * "serfHealth"
     * ],
     * "Node": "foobar",
     * "ID": "adf4238a-882b-9ddc-4a9d-5b6758e4159e",
     * "CreateIndex": 1086449
     * }]
     * </code>
     * </pre>
     * <p>
     * If the session is not found, null is returned instead of a JSON list.
     * This endpoint supports blocking queries and all consistency modes.
     * <p>
     * /v1/session/info/<session>: Queries a given session
     *
     * @param sessionId      id of a session
     * @param requestOptions request options
     * @return Session for this session id.
     */
    public Optional<Session> info(final String sessionId, RequestOptions requestOptions) {

        return info(sessionId, null, requestOptions);

    }

    public Optional<Session> info(final String sessionId) {

        return info(sessionId, null, RequestOptions.BLANK);

    }


    /**
     * /v1/session/info/<session>:
     * Queries a given session
     *
     * @param sessionId      id of a session
     * @param requestOptions request options
     * @param datacenter     datacenter
     * @return Session for this session id.
     */
    public Optional<Session> info(final String sessionId, final String datacenter, final RequestOptions requestOptions) {


        final URI uri = createURI("/info/" + sessionId);
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, null, null, "");


        HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable to get the sessions", uri, httpResponse);
        }

        final List<Session> sessions = fromJsonArray(httpResponse.body(), Session.class);
        if (sessions == null || sessions.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(sessions.get(0));

    }


    /**
     * This endpoint is hit with a GET and returns the active sessions for a given node and datacenter.
     * By default, the datacenter of the agent is queried; however,
     * the dc can be provided using the "?dc=" query parameter.
     * The node being queried must be provided on the path.
     * /v1/session/node/<node>:
     * Lists sessions belonging to a node
     *
     * @param node           node
     * @param requestOptions request options for consistency and long poll
     * @return list of sessions for this node
     */
    public List<Session> getSessionsForNode(final String node, final RequestOptions requestOptions) {

        return getSessionsForNode(node, null, requestOptions);
    }

    public List<Session> getSessionsForNode(final String node) {
        return getSessionsForNode(node, null, RequestOptions.BLANK);
    }

    /**
     * /v1/session/node/<node>:
     * Lists sessions belonging to a node
     *
     * @param requestOptions request options for consistency and long poll
     * @param datacenter     data center param
     * @return list of sessions for this node
     */
    public List<Session> getSessionsForNode(final String node, final String datacenter, final RequestOptions requestOptions) {


        final URI uri = createURI("/node/" + node);
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, null, requestOptions, "");


        HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable to get the sessions", uri, httpResponse);
        }

        return fromJsonArray(httpResponse.body(), Session.class);
    }

}
