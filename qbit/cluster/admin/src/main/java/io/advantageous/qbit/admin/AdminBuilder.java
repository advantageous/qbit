package io.advantageous.qbit.admin;


import io.advantageous.boon.core.IO;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class AdminBuilder {


    @SuppressWarnings("WeakerAccess")
    public static final String CONTEXT = "qbit.service.admin.";

    private int port = 7777;
    private String host;
    private EndpointServerBuilder endpointServerBuilder;
    private ServiceEndpointServer serviceEndpointServer;
    private String name;
    private Admin admin;
    private HealthServiceAsync healthService;
    private HealthServiceBuilder healthServiceBuilder;
    private String htmlPageLocation = "/qbit/admin.html";
    private HttpServer httpServer;
    private HttpServerBuilder httpServerBuilder;
    private String webPageContents;
    private Supplier<String> webPageContentsSupplier;
    private ContextMetaBuilder contextBuilder;


    @SuppressWarnings("WeakerAccess")
    public AdminBuilder(final PropertyResolver propertyResolver) {
        port = propertyResolver.getIntegerProperty("port", port);
        host = propertyResolver.getStringProperty("host", host);
        htmlPageLocation = propertyResolver.getStringProperty("htmlPageLocation", htmlPageLocation);
    }


    @SuppressWarnings("WeakerAccess")
    public AdminBuilder() {

        this(PropertyResolver.createSystemPropertyResolver(CONTEXT));
    }

    public AdminBuilder(Properties properties) {

        this(PropertyResolver.createPropertiesPropertyResolver(CONTEXT, properties));
    }

    public static AdminBuilder adminBuilder() {
        return new AdminBuilder();
    }

    public String getHtmlPageLocation() {
        return htmlPageLocation;
    }

    public void setHtmlPageLocation(String htmlPageLocation) {
        this.htmlPageLocation = htmlPageLocation;
    }


    public AdminBuilder setWebCotentsSupplier(Supplier<String> webPageContentsSupplier) {
        this.webPageContentsSupplier = webPageContentsSupplier;
        return this;
    }

    public Supplier<String> getWebPageContentsSupplier() {
        if (webPageContentsSupplier == null) {
            final String webContents = getWebPageContents();
            webPageContentsSupplier = new Supplier<String>() {
                @Override
                public String get() {
                    return webContents;
                }
            };
        }
        return webPageContentsSupplier;
    }

    public AdminBuilder setWebPageContents(String webPageContents) {
        this.webPageContents = webPageContents;
        return this;
    }

    public String getWebPageContents() {

        if (webPageContents==null || webPageContents.isEmpty()) {

            final String htmlPageLocationInitial = getHtmlPageLocation();
            final String pageLocation;
            pageLocation = htmlPageLocationInitial.startsWith("/") ?
                    htmlPageLocationInitial.substring(1, htmlPageLocationInitial.length()) :
                    htmlPageLocationInitial;

            webPageContents = IO.read(
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(pageLocation)
            );
        }
        return webPageContents;
    }
    public HttpServer getHttpServer() {
        if (httpServer == null) {
            httpServer = getHttpServerBuilder()
                    .setPort(getPort())
                    .setHost(getHost())
                    .build();

            final Supplier<String> webPageContentsSupplier = getWebPageContentsSupplier();


            httpServer.setShouldContinueHttpRequest(httpRequest -> {
                /* If not the page uri we want to then,
                 just continue by returning true. */
                if (!httpRequest.getUri().equals(getHtmlPageLocation())) {
                    return true;
                }

                final String webPageContents = webPageContentsSupplier.get();
                /* Send the HTML file out to the browser. */
                httpRequest.getReceiver().response(200, "text/html", webPageContents);
                return false;
            });

        }
        return httpServer;
    }

    public void setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public HttpServerBuilder getHttpServerBuilder() {
        if (httpServerBuilder==null) {
            httpServerBuilder = HttpServerBuilder.httpServerBuilder();
        }
        return httpServerBuilder;
    }

    public void setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
    }

    public HealthServiceAsync getHealthService() {
        if (healthService == null) {
            healthService = getHealthServiceBuilder().buildAndStart();
        }
        return healthService;
    }

    public AdminBuilder setHealthService(HealthServiceAsync healthService) {
        this.healthService = healthService;
        return this;
    }

    public HealthServiceBuilder getHealthServiceBuilder() {
        if (healthServiceBuilder == null) {
            healthServiceBuilder = HealthServiceBuilder.healthServiceBuilder();
        }
        return healthServiceBuilder;
    }

    public AdminBuilder setHealthServiceBuilder(HealthServiceBuilder healthServiceBuilder) {
        this.healthServiceBuilder = healthServiceBuilder;
        return this;
    }

    public Admin getAdmin() {
        if (admin == null) {
            admin = new Admin(getHealthService(), getContextBuilder());
        }
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public AdminBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ServiceEndpointServer getServiceEndpointServer() {

        if (serviceEndpointServer == null) {
            serviceEndpointServer = getEndpointServerBuilder()
                    .setHttpServer(getHttpServer()).build();
        }
        return serviceEndpointServer;
    }

    public void setServiceEndpointServer(ServiceEndpointServer serviceEndpointServer) {
        this.serviceEndpointServer = serviceEndpointServer;
    }

    public int getPort() {
        return port;
    }

    public AdminBuilder setPort(int port) {
        this.port = port;
        return this;

    }

    public String getHost() {
        return host;
    }

    public AdminBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public EndpointServerBuilder getEndpointServerBuilder() {
        if (endpointServerBuilder == null) {
            endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder()
                    .setUri("/")
                    .setPort(this.getPort());
            if (host != null && !"".equals(host)) {
                endpointServerBuilder.setHost(host);
            }
        }
        return endpointServerBuilder;
    }

    public AdminBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }


    public ServiceEndpointServer build() {
        ServiceEndpointServer serviceEndpointServer = getServiceEndpointServer();
        if (getName() == null) {
            serviceEndpointServer.initServices(getAdmin());
        } else {
            serviceEndpointServer.addServiceObject(getName(), getAdmin());
        }
        return serviceEndpointServer;
    }

    public AdminBuilder setContextBuilder(final ContextMetaBuilder contextBuilder) {
        this.contextBuilder = contextBuilder;
        return this;
    }

    public ContextMetaBuilder getContextBuilder() {

        if (contextBuilder == null) {
            contextBuilder = ContextMetaBuilder.contextMetaBuilder();
        }
        return contextBuilder;
    }
}
