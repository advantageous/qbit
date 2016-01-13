/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */
package io.advantageous.qbit.meta;

import io.advantageous.boon.core.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Holds the root of a service bundle's metadata.
 * A service bundle is more or less a group of endpoints, that are mapped to the same host/port.
 */
public class ContextMeta {

    private final String rootURI;
    private final List<ServiceMeta> services;

    /**
     * The title of the application.
     */
    private final String title;

    /**
     * A short description of the application.
     * GFM syntax can be used for rich text representation.
     * GFM is https://help.github.com/articles/github-flavored-markdown/
     * GitHub Flavored Markdown.
     */
    private final String description;
    private final String contactName;
    private final String contactURL;
    private final String contactEmail;
    private final String licenseName;
    private final String licenseURL;
    private final String version;
    private final String hostAddress;

    public ContextMeta(final String title, final String rootURI, final List<ServiceMeta> services, String description, String contactName, String contactURL, String contactEmail, String licenseName, String licenseURL, String version, String hostAddress) {
        this.rootURI = rootURI;
        this.description = description;
        this.contactName = contactName;
        this.contactURL = contactURL;
        this.contactEmail = contactEmail;
        this.licenseName = licenseName;
        this.licenseURL = licenseURL;
        this.version = version;
        this.hostAddress = hostAddress;
        this.services = Collections.unmodifiableList(services);
        this.title = title;
    }

    public static ContextMeta context(@SuppressWarnings("SameParameterValue") final String rootURI, final ServiceMeta... services) {
        return new ContextMeta("title", rootURI, Lists.list(services), null, null, null, null, null, null, null, null);
    }

    public String getRootURI() {
        return rootURI;
    }

    public List<ServiceMeta> getServices() {
        return services;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactURL() {
        return contactURL;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getLicenseURL() {
        return licenseURL;
    }

    public String getVersion() {
        return version;
    }

    public String getHostAddress() {
        return hostAddress;
    }
}
