package io.advantageous.qbit.http;

public class HttpContentTypes {

    public static final String FORM = "application/x-www-form-urlencoded";
    public static final String JSON = "application/json";
    public static final String MULTI_PART_FORM = "multipart/form-data";

    public static boolean isFormContentType(final String contentType) {

        return HttpContentTypes.FORM.equals(contentType)
                || HttpContentTypes.MULTI_PART_FORM.equals(contentType);

    }
}
