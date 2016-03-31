package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.ApiInfo;
import io.advantageous.qbit.meta.swagger.Contact;
import io.advantageous.qbit.meta.swagger.License;

public class ApiInfoBuilder {
    /**
     * Required. The title of the application.
     */
    private String title;

    /**
     * A short description of the application. GFM syntax can be used for rich text representation.
     */
    private String description;

    /**
     * The Terms of Service for the API.
     */
    private String termsOfService;


    /**
     * The Contact info for the Service  API.
     */
    private Contact contact;
    private ContactBuilder contactBuilder;


    /**
     * The License info for the Service  API.
     */
    private License license;
    private LicenseBuilder licenseBuilder;


    /**
     * Required Provides the version of the application API (not to be confused with the specification version)
     */
    private String version;

    public String getTitle() {
        return title;
    }

    public ApiInfoBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApiInfoBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTermsOfService() {
        return termsOfService;
    }

    public ApiInfoBuilder setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
        return this;
    }

    public ContactBuilder getContactBuilder() {
        if (contactBuilder == null) {
            contactBuilder = new ContactBuilder();
        }
        return contactBuilder;
    }

    public ApiInfoBuilder setContactBuilder(ContactBuilder contactBuilder) {
        this.contactBuilder = contactBuilder;
        return this;
    }

    public Contact getContact() {
        if (contact == null) {
            contact = getContactBuilder().build();
        }
        return contact;
    }

    public ApiInfoBuilder setContact(Contact contact) {
        this.contact = contact;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApiInfoBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public License getLicense() {
        if (license == null) {
            license = getLicenseBuilder().build();
        }
        return license;
    }

    public ApiInfoBuilder setLicense(License license) {
        this.license = license;
        return this;
    }


    public LicenseBuilder getLicenseBuilder() {
        if (licenseBuilder == null) {
            licenseBuilder = new LicenseBuilder();
        }
        return licenseBuilder;
    }

    public ApiInfoBuilder setLicenseBuilder(LicenseBuilder licenseBuilder) {
        this.licenseBuilder = licenseBuilder;
        return this;
    }

    public ApiInfo build() {
        return new ApiInfo(getTitle(), getDescription(), getTermsOfService(),
                getContact(), getVersion(), getLicense());
    }

}
