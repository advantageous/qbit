package io.advantageous.qbit.meta.swagger;

public class ExternalDocumentation {

    private final String description;
    private final String url;

    public ExternalDocumentation(String description, String url) {
        this.description = description;
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }
}
