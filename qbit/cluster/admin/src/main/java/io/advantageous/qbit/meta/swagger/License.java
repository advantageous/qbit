package io.advantageous.qbit.meta.swagger;

public class License {


    /**
     * Required. The license name used for the API.
     */
    private final String name;


    /**
     * Required. A URL to the license used for the API. MUST be in the format of a URL.
     */
    private final String url;

    public License(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
