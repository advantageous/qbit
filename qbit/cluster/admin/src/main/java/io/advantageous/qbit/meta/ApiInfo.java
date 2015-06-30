package io.advantageous.qbit.meta;

public class ApiInfo {

    /**
     * Required. The title of the application.
     */
    private final String title;

    /**
     * A short description of the application. GFM syntax can be used for rich text representation.
     */
    private final String description;

    /**
     * The Terms of Service for the API.
     */
    private final String termsOfService;


    /**
     * The Contact info for the Service  API.
     */
    private final Contact contact;

    /**
     * string	Required Provides the version of the application API (not to be confused with the specification version)
     */
    private final String version;

    public ApiInfo(String title, String description, String termsOfService, Contact contact, String version) {
        this.title = title;
        this.description = description;
        this.termsOfService = termsOfService;
        this.contact = contact;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTermsOfService() {
        return termsOfService;
    }


    public String getVersion() {
        return version;
    }

    public Contact getContact() {
        return contact;
    }
}
