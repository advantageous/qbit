package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.reflection.Annotated;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.http.HttpHeaders;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.ServiceMeta;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Making this more compatible with swagger.
 * Allows you to build a context for service definitions.
 */
public class ContextMetaBuilder {

    public final static String CONTEXT = "qbit.contextBuilder.";
    private String rootURI = "/services";
    private List<ServiceMeta> services = new ArrayList<>();
    /**
     * The title of the application.
     */
    private String title = "application title goes here";

    /**
     * A short description of the application.
     * GFM syntax can be used for rich text representation.
     * GFM is https://help.github.com/articles/github-flavored-markdown/
     * GitHub Flavored Markdown.
     */
    private String description = "Description not set";
    private String contactName = "ContactName not set";
    private String contactURL = "Contact URL not set";
    private String contactEmail = "no.contact.email@set.me.please.com";
    private String licenseName = "licenseName not set";
    private String licenseURL = "http://www.license.url.com/not/set/";
    private String version = "0.1-NOT-SET";
    private String hostAddress = "localhost";


    public ContextMetaBuilder(final PropertyResolver propertyResolver) {
        description = propertyResolver.getStringProperty("description", description);
        contactName = propertyResolver.getStringProperty("contactName", contactName);
        contactEmail = propertyResolver.getStringProperty("contactEmail", contactEmail);
        licenseName = propertyResolver.getStringProperty("licenseName", licenseName);
        licenseURL = propertyResolver.getStringProperty("licenseURL", licenseURL);
        version = propertyResolver.getStringProperty("licenseURL", version);
        hostAddress = propertyResolver.getStringProperty("hostAddress", hostAddress);
        title = propertyResolver.getStringProperty("title", title);
    }

    public ContextMetaBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(CONTEXT, properties));
    }

    public ContextMetaBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(CONTEXT));
    }


    public static ContextMetaBuilder contextMetaBuilder() {
        return new ContextMetaBuilder();
    }

    public static List<String> getRequestPathsByAnnotated(Annotated classMeta, String name) {
        Object value = getRequestPath(classMeta, name);

        if (value instanceof String) {
            return Lists.list(asPath(value.toString()));
        } else if (value instanceof String[]) {

            final String[] varray = (String[]) value;
            if (varray.length > 0) {
                return Lists.list((String[]) value);
            } else {
                return Lists.list("/" + name);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public static List<RequestMethod> getRequestMethodsByAnnotated(Annotated annotated) {

        final AnnotationData requestMapping = getAnnotationData(annotated);

        if (requestMapping == null) {
            return Collections.singletonList(RequestMethod.GET);
        }

        final Object method = requestMapping.getValues().get("method");

        if (method == null) {

            return Collections.singletonList(RequestMethod.GET);
        }


        if (method instanceof RequestMethod[]) {
            //noinspection UnnecessaryLocalVariable
            final List<RequestMethod> requestMethods = Arrays.asList(((RequestMethod[]) method));
            return requestMethods;
        }

        if (method instanceof Object[]) {

            final Object[] methods = (Object[]) method;
            if (methods.length == 0) {

                return Collections.singletonList(RequestMethod.GET);
            }
            final List<RequestMethod> requestMethods = new ArrayList<>(methods.length);

            for (Object object : methods) {
                requestMethods.add(RequestMethod.valueOf(object.toString()));
            }

            return requestMethods;

        }

        return Collections.singletonList(RequestMethod.valueOf(method.toString()));


    }

    public static AnnotationData getAnnotationData(Annotated annotated) {
        AnnotationData requestMapping = annotated.annotation("RequestMapping");

        if (requestMapping == null) {
            requestMapping = annotated.annotation("GET");
        }
        if (requestMapping == null) {
            requestMapping = annotated.annotation("POST");
        }
        if (requestMapping == null) {
            requestMapping = annotated.annotation("PUT");
        }
        if (requestMapping == null) {
            requestMapping = annotated.annotation("DELETE");
        }
        if (requestMapping == null) {
            requestMapping = annotated.annotation("Bridge");
        }

        return requestMapping;
    }

    static Object getRequestPath(Annotated classMeta, final String name) {
        final AnnotationData requestMapping = getAnnotationData(classMeta);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("value");
            if (value == null) {
                value = "/" + name.toLowerCase();
            }
            return value;
        } else {
            return "/" + name.toLowerCase();
        }
    }

    static String getDescriptionFromRequestMapping(Annotated annotated) {
        final AnnotationData requestMapping = getAnnotationData(annotated);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("description");
            return value.toString();
        } else {
            return "no description";
        }
    }

    static int getCodeFromRequestMapping(Annotated annotated) {
        final AnnotationData requestMapping = getAnnotationData(annotated);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("code");
            return Conversions.toInt(value);
        } else {
            return -1;
        }
    }

    static String getContentTypeFromRequestMapping(Annotated annotated) {
        final AnnotationData requestMapping = getAnnotationData(annotated);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("contentType");
            return Conversions.toString(value);
        } else {
            return "application/json";
        }
    }

    static String getReturnDescriptionFromRequestMapping(Annotated annotated) {
        final AnnotationData requestMapping = getAnnotationData(annotated);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("returnDescription");
            return value.toString();
        } else {
            return "no description of return";
        }
    }

    static String getSummaryFromRequestMapping(Annotated annotated) {
        final AnnotationData requestMapping = getAnnotationData(annotated);

        if (requestMapping != null) {
            Object value = requestMapping.getValues().get("summary");
            return value.toString();
        } else {
            return "no summary";
        }
    }

    public static String asPath(String s) {
        String path = s;
        if (!s.startsWith("/")) {
            path = "/" + s;
        }

        if (s.endsWith("/")) {
            if (s.length() > 2) {
                path = path.substring(0, path.length() - 1);
            }
        }

        return path;
    }

    public String getTitle() {
        return title;
    }

    public ContextMetaBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ContextMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getContactName() {
        return contactName;
    }

    public ContextMetaBuilder setContactName(String contactName) {
        this.contactName = contactName;
        return this;
    }

    public String getContactURL() {
        return contactURL;
    }

    public ContextMetaBuilder setContactURL(String contactURL) {
        this.contactURL = contactURL;
        return this;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public ContextMetaBuilder setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        return this;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public ContextMetaBuilder setLicenseName(String licenseName) {
        this.licenseName = licenseName;
        return this;
    }

    public String getLicenseURL() {
        return licenseURL;
    }

    public ContextMetaBuilder setLicenseURL(String licenseURL) {
        this.licenseURL = licenseURL;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ContextMetaBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public ContextMetaBuilder setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
        return this;
    }

    public String getRootURI() {
        return rootURI;
    }

    public ContextMetaBuilder setRootURI(String rootURI) {
        this.rootURI = rootURI;
        return this;
    }

    public List<ServiceMeta> getServices() {
        return services;
    }

    public ContextMetaBuilder setServices(List<ServiceMeta> services) {
        this.services = services;
        return this;
    }

    public ContextMetaBuilder addService(ServiceMeta service) {
        this.services.add(service);
        return this;
    }


    public ContextMetaBuilder addService(String alias, Class<?> serviceClass) {

        final ClassMeta<?> classMeta = ClassMeta.classMeta(serviceClass);
        String name = alias != null ? alias : getServiceName(classMeta);

        final List<String> requestPaths = getRequestPathsByAnnotated(classMeta, name);

        if (alias != null) {
            requestPaths.add(asPath(alias));
        }

        final String description = getDescriptionFromRequestMapping(classMeta);


        final ServiceMetaBuilder serviceMetaBuilder = ServiceMetaBuilder.serviceMetaBuilder()
                .setRequestPaths(requestPaths).setName(name).setDescription(description)
                .setResponseHeaders(getResponseHeaders(classMeta));

        serviceMetaBuilder.addMethods(this.getRootURI(), Lists.list(classMeta.methods()));


        addService(serviceMetaBuilder.build());

        return this;

    }

    public ContextMetaBuilder addService(final Class<?> serviceClass) {

        return addService(null, serviceClass);
    }

    private String getServiceName(ClassMeta<?> classMeta) {

        String name = AnnotationUtils.readServiceName(classMeta);

        if (Str.isEmpty(name)) {
            name = Str.camelCaseLower(classMeta.name());
        }

        return name;
    }

    public ContextMetaBuilder addServices(ServiceMeta... serviceArray) {
        Collections.addAll(this.services, serviceArray);
        return this;
    }

    public ContextMeta build() {
        return new ContextMeta(getTitle(), getRootURI(), getServices(),
                getDescription(), getContactName(), getContactURL(), getContactEmail(),
                getLicenseName(), getLicenseURL(), getVersion(), getHostAddress());
    }


    private MultiMap<String, String> getResponseHeaders(final Annotated annotated) {

        MultiMap<String, String> responseHeadersMap = MultiMap.empty();

        final AnnotationData responseHeaderAnnotation = annotated.annotation("ResponseHeader");


        if (responseHeaderAnnotation != null) {
            final String name = responseHeaderAnnotation.getValues().get("name").toString();
            final String value = responseHeaderAnnotation.getValues().get("value").toString();
            responseHeadersMap = new MultiMapImpl<>();
            responseHeadersMap.add(name, value);
        }


        final AnnotationData responseHeadersAnnotation = annotated.annotation("ResponseHeaders");

        if (responseHeadersAnnotation != null) {
            if (responseHeadersMap.size() == 0) {
                responseHeadersMap = new MultiMapImpl<>();
            }
            final Object[] values = (Object[]) responseHeadersAnnotation.getValues().get("value");

            for (Object object : values) {

                if (object instanceof Annotation) {
                    AnnotationData annotationData = new AnnotationData((Annotation) object);

                    final String name = annotationData.getValues().get("name").toString();
                    final String value = annotationData.getValues().get("value").toString();
                    responseHeadersMap.add(name, value);
                }
            }

        }


        final AnnotationData noCache = annotated.annotation("NoCacheHeaders");
        if (noCache != null) {
            if (responseHeadersMap.size() == 0) {
                responseHeadersMap = new MultiMapImpl<>();
            }
            responseHeadersMap.add(HttpHeaders.CACHE_CONTROL, "max-age=0");
            responseHeadersMap.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
        }


        return responseHeadersMap;
    }

}
