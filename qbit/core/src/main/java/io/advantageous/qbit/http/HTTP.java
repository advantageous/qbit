package io.advantageous.qbit.http;

import io.advantageous.boon.core.*;
import io.advantageous.boon.primitive.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HTTP {

    public static final int DEFAULT_TIMEOUT_SECONDS = Sys.sysProp("HTTP.timeout.seconds", 180);
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Does a simple HTTP get
     *
     * @param url pass the URL you want to get.
     * @return contents as UTF-8 string
     */
    public static String get(
            final String url) {

        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;

                final Map<String, String> accept = Maps.map(
                        "Accept", "text/html,application/xhtml+xml,application/xml,application/json,text/plain;"
                );

                connection = doGet(url, accept, null, null);
                return extractResponseString(connection);
            }
        });

    }


    /**
     * Does a simple blocking post
     *
     * @param url  url you want to pass
     * @param body body of POST
     * @return contents as string
     */
    public static String post(
            final String url,
            final String body) {

        return postBodyTextWithContentType(url, "text/plain", body);
    }


    /**
     * Does a get but you get a Response object
     *
     * @param url url
     * @return Response object
     */
    public static Response getResponse(
            final String url) {

        return Exceptions.tryIt(Response.class, new Exceptions.TrialWithReturn<Response>() {
                    @Override
                    public Response tryIt() throws Exception {
                        URLConnection connection;

                        final Map<String, String> accept = Maps.map(
                                "Accept", "text/html,application/xhtml+xml,application/xml,application/json,text/plain;"
                        );

                        connection = doGet(url, accept, null, null);
                        return extractResponseObject(connection);
                    }
                }

        );

    }

    public static Response deleteResponse(
            final String url) {

        return Exceptions.tryIt(Response.class, new Exceptions.TrialWithReturn<Response>() {
                    @Override
                    public Response tryIt() throws Exception {
                        URLConnection connection;

                        final Map<String, String> accept = Maps.map(
                                "Accept", "text/html,application/xhtml+xml,application/xml,application/json,text/plain;"
                        );

                        connection = doDelete(url, accept, null, null);
                        return extractResponseObject(connection);
                    }
                }

        );

    }

    /**
     * GET that expects contents back as a byte array.
     *
     * @param url         url
     * @param contentType contentType
     * @return bytes from location
     */
    public static byte[] getBytes(
            final String url, final String contentType) {

        return Exceptions.tryIt(byte[].class, new Exceptions.TrialWithReturn<byte[]>() {
            @Override
            public byte[] tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, null, contentType, null, true);
                return extractResponseBytes(connection);
            }
        });

    }


    /**
     * GET that expects contents back as a byte array and allows you to pass headers.
     *
     * @param url         url
     * @param contentType contentType
     * @param headers     to pass.
     * @return bytes from location
     */
    public static byte[] getBytesUsingHeaders(
            final String url, final String contentType, final Map<String, ?> headers) {

        return Exceptions.tryIt(byte[].class, new Exceptions.TrialWithReturn<byte[]>() {
            @Override
            public byte[] tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, contentType, null, true);
                return extractResponseBytes(connection);
            }
        });

    }


    /**
     * GET that expects contents back as a byte array and allows you to pass headers.
     *
     * @param url     url
     * @param headers to pass.
     * @return string from location
     */
    public static String getWithHeaders(
            final String url,
            final Map<String, ?> headers) {

        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, null, null);
                return extractResponseString(connection);
            }
        });

    }


    /**
     * GET that expects contents back as a string and allows you to pass headers and content type.
     *
     * @param url         url
     * @param headers     to pass.
     * @param contentType contentType
     * @return UTF-8 string from location
     */
    public static String getWithContentTypeHeaders(
            final String url,
            final Map<String, ?> headers,
            final String contentType) {

        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, contentType, null);
                return extractResponseString(connection);
            }
        });

    }

    public static String getWithCharSet(
            final String url,
            final Map<String, ?> headers,
            final String contentType,
            final String charSet) {


        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, contentType, charSet);
                return extractResponseString(connection);
            }
        });

    }

    public static String postText(
            final String url,
            final String body) {
        return postBodyTextWithContentType(url, "text/plain", body);
    }

    public static String postBodyTextWithContentType(
            final String url,
            final String contentType,
            final String body) {


        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doPost(url, null, contentType, null, body);
                return extractResponseString(connection);
            }
        });

    }


    public static String putBodyTextWithContentType(
            final String url,
            final String contentType,
            final String body) {


        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doPost(url, null, contentType, null, body);
                return extractResponseString(connection);
            }
        });

    }

    public static String postJSON(
            final String url,
            final String jsonString) {

        return postBodyTextWithContentType(url, APPLICATION_JSON, jsonString);
    }


    public static String putJSON(
            final String url,
            final String jsonString) {

        return putBodyTextWithContentType(url, APPLICATION_JSON, jsonString);
    }


    public static Response jsonRestCallViaPOST(
            final String url,
            final String jsonString) {

        return postBodyTextWithContentTypeReturnResponse(url, APPLICATION_JSON, jsonString);
    }


    public static Response jsonRestCallViaPUT(
            final String url,
            final String jsonString) {

        return putBodyTextWithContentTypeReturnResponse(url, APPLICATION_JSON, jsonString);
    }


    public static String getJSON(
            final String url,
            final Map<String, ?> headers
    ) {

        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, APPLICATION_JSON, null);
                return extractResponseString(connection);
            }
        });

    }


    public static Response jsonRestCallWithHeaders(
            final String url,
            final Map<String, ?> headers
    ) {

        return Exceptions.tryIt(Response.class, new Exceptions.TrialWithReturn<Response>() {
            @Override
            public Response tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, APPLICATION_JSON, null);
                return extractResponseObject(connection);
            }
        });

    }

    public static Response jsonRestCall(
            final String url
    ) {

        return Exceptions.tryIt(Response.class, new Exceptions.TrialWithReturn<Response>() {
            @Override
            public Response tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, null, APPLICATION_JSON, null);
                return extractResponseObject(connection);
            }
        });

    }


    public static Response postBodyTextWithContentTypeReturnResponse(final String url,
                                                                     final String contentType,
                                                                     final String body) {


        return Exceptions.tryIt(Response.class, new Exceptions.TrialWithReturn<Response>() {
            @Override
            public Response tryIt() throws Exception {
                URLConnection connection;
                connection = doPost(url, null, contentType, null, body);
                return extractResponseObject(connection);
            }
        });

    }


    public static Response putBodyTextWithContentTypeReturnResponse(final String url,
                                                                    final String contentType,
                                                                    final String body) {


        return Exceptions.tryIt(Response.class, new Exceptions.TrialWithReturn<Response>() {
            @Override
            public Response tryIt() throws Exception {
                URLConnection connection;
                connection = doPut(url, null, contentType, null, body);
                return extractResponseObject(connection);
            }
        });

    }


    public static String getJSONWithParams(
            final String url,
            final Map<String, ?> headers, final Map<String, ?> params
    ) {

        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doGet(url, headers, APPLICATION_JSON, null, params);
                return extractResponseString(connection);
            }
        });

    }

    public static String postXML(
            final String url,
            final String jsonString) {

        return postBodyTextWithContentType(url, "text/xml", jsonString);
    }

    public static String postWithHeaders(
            final String url,
            final Map<String, ?> headers,
            final String body) {

        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doPost(url, headers, "text/plain", null, body);
                return extractResponseString(connection);
            }
        });

    }


    public static String postWithContentType(
            final String url,
            final Map<String, ?> headers,
            final String contentType,
            final String body) {


        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doPost(url, headers, contentType, null, body);
                return extractResponseString(connection);
            }
        });

    }


    public static String postWithCharset(
            final String url,
            final Map<String, ?> headers,
            final String contentType,
            final String charSet,
            final String body) {


        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doPost(url, headers, contentType, charSet, body);
                return extractResponseString(connection);
            }
        });

    }

    private static URLConnection doPost(String url, Map<String, ?> headers,
                                        String contentType, String charset, String body
    ) throws IOException {
        HttpURLConnection connection;/* Handle output. */


        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        connection.setDoOutput(true);
        manageContentTypeHeaders(contentType, charset, connection);

        manageHeaders(headers, connection);


        IO.write(connection.getOutputStream(), body, IO.DEFAULT_CHARSET);
        return connection;
    }


    private static URLConnection doPut(String url, Map<String, ?> headers,
                                       String contentType, String charset, String body
    ) throws IOException {
        HttpURLConnection connection;/* Handle output. */


        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        manageContentTypeHeaders(contentType, charset, connection);

        manageHeaders(headers, connection);


        IO.write(connection.getOutputStream(), body, IO.DEFAULT_CHARSET);
        return connection;
    }

    public static String postForm(final String url, final Map<String, ?> headers,
                                  final Map<String, Object> formData
    ) {
        return Exceptions.tryIt(String.class, new Exceptions.TrialWithReturn<String>() {
            @Override
            public String tryIt() throws Exception {
                URLConnection connection;
                connection = doPostFormData(url, headers, formData);
                return extractResponseString(connection);
            }
        });

    }

    private static URLConnection doPostFormData(String url, Map<String, ?> headers,
                                                Map<String, Object> formData
    ) throws IOException {
        HttpURLConnection connection;/* Handle output. */


        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        connection.setDoOutput(true);

        connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        ByteBuf buf = ByteBuf.create(244);

        final Set<String> keys = formData.keySet();

        int index = 0;
        for (String key : keys) {

            Object value = formData.get(key);

            if (index > 0) {
                buf.addByte('&');
            }


            buf.addUrlEncoded(key);
            buf.addByte('=');

            if (!(value instanceof byte[])) {
                buf.addUrlEncoded(value.toString());
            } else {
                buf.addUrlEncodedByteArray((byte[]) value);
            }
            index++;
        }

        manageContentTypeHeaders("application/x-www-form-urlencoded",
                StandardCharsets.UTF_8.name(), connection);

        manageHeaders(headers, connection);


        int len = buf.len();
        IO.write(connection.getOutputStream(),
                new String(buf.readForRecycle(), 0, len, StandardCharsets.UTF_8), IO.DEFAULT_CHARSET);
        return connection;
    }

    private static void manageHeaders(Map<String, ?> headers, URLConnection connection) {
        if (headers != null) {
            for (Map.Entry<String, ?> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue().toString());
            }
        }
    }


    private static void manageContentTypeHeaders(String contentType, String charset, URLConnection connection, boolean binary) {

        if (!binary) {
            connection.setRequestProperty("Accept-Charset", charset == null ? StandardCharsets.UTF_8.displayName() : charset);
        }
        if (contentType != null && !contentType.isEmpty()) {
            connection.setRequestProperty("Content-Type", contentType);
        }

    }

    private static URLConnection doGet(String url, Map<String, ?> headers,
                                       String contentType, String charset, boolean binary) throws IOException {
        URLConnection connection;/* Handle output. */
        connection = new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        manageContentTypeHeaders(contentType, charset, connection, binary);

        manageHeaders(headers, connection);

        return connection;
    }


    private static String extractResponseString(URLConnection connection) throws IOException {

        /* Handle input. */
        HttpURLConnection http = (HttpURLConnection) connection;
        int status = http.getResponseCode();
        String charset = getCharset(connection.getHeaderField("Content-Type"));


        if (status == 200) {
            return readResponseBody(http, charset);
        } else {
            return readErrorResponseBody(http, status, charset);
        }
    }


    private static Response extractResponseObject(URLConnection connection) throws IOException {

        /* Handle input. */
        HttpURLConnection http = (HttpURLConnection) connection;
        int status = http.getResponseCode();

        String charset = getCharset(connection.getHeaderField("Content-Type"));


        String body;

        if (status == 200) {
            body = readResponseBody(http, charset);
        } else {
            body = readErrorResponseBodyDoNotDie(http, status, charset);
        }

        return Response.response(status, http.getHeaderFields(), http.getResponseMessage(), body);
    }

    private static byte[] extractResponseBytes(URLConnection connection) throws IOException {

        /* Handle input. */
        HttpURLConnection http = (HttpURLConnection) connection;
        int status = http.getResponseCode();

        //System.out.println("CONTENT-TYPE" + connection.getHeaderField("Content-TypeT"));


        if (status == 200) {
            return readResponseBodyAsBytes(http);
        } else {
            String charset = getCharset(connection.getHeaderField("Content-Type"));

            readErrorResponseBody(http, status, charset);
            return null;
        }
    }

    private static byte[] readResponseBodyAsBytes(HttpURLConnection http) {
        try {
            return IO.input(http.getInputStream());
        } catch (IOException e) {
            return Exceptions.handle(byte[].class, e);
        }

    }

    private static String readErrorResponseBody(HttpURLConnection http, int status, String charset) {
        InputStream errorStream = http.getErrorStream();
        if (errorStream != null) {
            String error = charset == null ? IO.read(errorStream) :
                    IO.read(errorStream, charset);
            return Exceptions.die(String.class, "STATUS CODE =" + status + "\n\n" + error);
        } else {
            return Exceptions.die(String.class, "STATUS CODE =" + status);
        }
    }


    private static String readErrorResponseBodyDoNotDie(HttpURLConnection http, int status, String charset) {
        InputStream errorStream = http.getErrorStream();
        if (errorStream != null) {
            String error = charset == null ? IO.read(errorStream) :
                    IO.read(errorStream, charset);
            return error;
        } else {
            return "";
        }
    }

    private static String readResponseBody(HttpURLConnection http, String charset) throws IOException {
        if (charset != null) {
            return IO.read(http.getInputStream(), charset);
        } else {
            return IO.read(http.getInputStream());
        }
    }

    private static String getCharset(String contentType) {
        if (contentType == null) {
            return null;
        }
        String charset = null;
        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                charset = param.split("=", 2)[1];
                break;
            }
        }
        charset = charset == null ? StandardCharsets.UTF_8.displayName() : charset;

        return charset;
    }


    private static void manageContentTypeHeaders(String contentType, String charset, URLConnection connection) {
        connection.setRequestProperty("Accept-Charset", charset == null ? StandardCharsets.UTF_8.displayName() : charset);
        if (contentType != null && !contentType.isEmpty()) {
            connection.setRequestProperty("Content-Type", contentType);
        }
    }

    private static URLConnection doGet(String url, Map<String, ?> headers,
                                       String contentType, String charset) throws IOException {
        URLConnection connection;/* Handle output. */
        connection = new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        manageContentTypeHeaders(contentType, charset, connection);

        manageHeaders(headers, connection);

        return connection;
    }


    private static URLConnection doDelete(String url, Map<String, ?> headers,
                                          String contentType, String charset) throws IOException {


        HttpURLConnection connection;/* Handle output. */


        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        connection.setRequestMethod("DELETE");

        manageContentTypeHeaders(contentType, charset, connection);

        manageHeaders(headers, connection);

        return connection;
    }

    private static URLConnection doGet(String url, Map<String, ?> headers,
                                       String contentType, String charset, Map<String, ?> params) throws IOException {

        if (charset == null) {
            charset = StandardCharsets.UTF_8.name();
        }
        URLConnection connection;/* Handle output. */
        connection = new URL(url).openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000);

        manageContentTypeHeaders(contentType, charset, connection);

        manageHeaders(headers, connection);

        final Set<String> keys = params.keySet();

        for (String key : keys) {

            Object value = params.get(key);
            connection.addRequestProperty(URLEncoder.encode(key, charset), URLEncoder.encode(value.toString(), charset));
        }
        return connection;
    }

    /**
     * Created by Richard on 3/3/14.
     */
    public static class Response {

        private final int status; //200 Ok, 500 error, etc. may not be HTTP could be some other scheme, but most likely HTTP codes
        private final Map<String, List<String>> headers; //could be map or list or object or JSON string
        private final Object statusMessage; //Could be "OK" or the message from a java exception
        private final Object payload;
        private final Class<? extends Enum> enumStatusClass;


        public Response(int status, Map<String, List<String>> headers, Object statusMessage, Object payload) {
            this.status = status;
            this.headers = headers;
            this.statusMessage = statusMessage;
            this.payload = payload;
            enumStatusClass = null;
        }


        public Response(int status, Map<String, List<String>> headers, Object statusMessage, Object payload, Class<? extends Enum> enumStatusClass) {
            this.status = status;
            this.headers = headers;
            this.statusMessage = statusMessage;
            this.payload = payload;
            this.enumStatusClass = enumStatusClass;
        }

        public static Response response(int status, Map<String, List<String>> headers, String statusMessage, String payload) {
            return new Response(status, headers, statusMessage, payload);
        }

        public int status() {
            return status;
        }

        public <E extends Enum> E statusEnum(Class<E> enumClass) {
            return Conversions.toEnum(enumClass, status);
        }

        public Enum statusEnum() {
            return Conversions.toEnum(this.enumStatusClass, status);
        }

        public Map<String, List<String>> headers() {
            return headers;
        }

        public Map<String, Object> headerMap() {
            return Conversions.toMap(headers);
        }

        public Object statusMessage() {
            return statusMessage;
        }

        public String statusMessageAsString() {
            return Conversions.toString(statusMessage);
        }

        public Object payload() {
            return payload;
        }

        public String payloadAsString() {
            return Conversions.toString(payload);
        }

        public String body() {
            return payloadAsString();
        }

        public int code() {
            return status();
        }

        @Override
        public String toString() {
            return "Response{" +
                    "status=" + status +
                    ", headers=" + headers +
                    ", statusMessage=" + statusMessage +
                    ", payload=" + payload +
                    ", enumStatusClass=" + enumStatusClass +
                    '}';
        }
    }
}
