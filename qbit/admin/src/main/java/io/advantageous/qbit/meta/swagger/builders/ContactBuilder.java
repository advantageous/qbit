package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Contact;

public class ContactBuilder {


    private String name;
    private String url;
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Contact build() {
        return new Contact(getName(), getUrl(), getEmail());
    }
}
