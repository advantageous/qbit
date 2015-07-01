package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.License;

public class LicenseBuilder {


    /**
     * Required. The license name used for the API.
     */
    private String name;


    /**
     * Required. A URL to the license used for the API. MUST be in the format of a URL.
     */
    private String url;

    public String getName() {
        return name;
    }

    public LicenseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LicenseBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public License build() {
        return new License(getName(), getUrl());
    }
}
