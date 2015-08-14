package io.advantageous.qbit.admin;


import io.advantageous.boon.core.IO;
import io.advantageous.qbit.admin.jobs.JavaStatsCollectorJob;
import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.service.stats.StatsCollector;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

    private String microServiceName = null;

    /** Used to generate meta data. */
    private ContextMetaBuilder contextBuilder;

    /** Used to manage admin jobs. */
    private List<AdminJob> adminJobs;

    /** Reactor to schedule admin jobs. */
    private Reactor reactor;


    /** Reactor to schedule admin jobs. */
    private ReactorBuilder reactorBuilder;
    private String hostName;
    private String machinName;

    public Reactor getReactor() {
        if (reactor == null) {
            reactor = getReactorBuilder().build();
        }
        return reactor;
    }

    public AdminBuilder setReactor(Reactor reactor) {
        this.reactor = reactor;
        return this;
    }

    public ReactorBuilder getReactorBuilder() {
        if (reactorBuilder == null) {
            reactorBuilder = ReactorBuilder.reactorBuilder();
        }
        return reactorBuilder;
    }

    public AdminBuilder setReactorBuilder(ReactorBuilder reactorBuilder) {
        this.reactorBuilder = reactorBuilder;
        return this;
    }

    public List<AdminJob> getAdminJobs() {
        if (adminJobs == null) {
            adminJobs = new ArrayList<>();
        }
        return adminJobs;
    }

    public AdminBuilder addAdminJob(final AdminJob adminJob) {
        getAdminJobs().add(adminJob);
        return this;
    }

    public String getMicroServiceName() {
        if (microServiceName == null) {


            if (microServiceName == null) {

                microServiceName = System.getenv("MICRO_SERVICE_NAME");
            }


            if (microServiceName == null) {

                microServiceName = System.getenv("APP_NAME");
            }

            if (microServiceName == null) {
                final String title = getContextBuilder().getTitle();

                if (title != null && !title.isEmpty()) {
                    microServiceName = title.toLowerCase().replace(" ", ".");
                }

            }

            if (microServiceName == null) {
                microServiceName = "my.app";
            }
        }
        return microServiceName;
    }

    public void setMicroServiceName(String microServiceName) {
        this.microServiceName = microServiceName;
    }

    public AdminBuilder registerJavaVMStatsJob(final StatsCollector statsCollector) {

        final JavaStatsCollectorJob jvmStatsJob = new JavaStatsCollectorJob(60, TimeUnit.SECONDS, statsCollector, getMachinName());
        return addAdminJob(jvmStatsJob);
    }

    public AdminBuilder registerJavaVMStatsJobEveryNSeconds(final StatsCollector statsCollector, final int everySeconds) {
        final JavaStatsCollectorJob jvmStatsJob = new JavaStatsCollectorJob(everySeconds, TimeUnit.SECONDS, statsCollector, getMachinName());
        return addAdminJob(jvmStatsJob);
    }


    public AdminBuilder setAdminJobs(final List<AdminJob> adminJobs) {
        this.adminJobs = adminJobs;
        return this;
    }

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
            admin = new Admin(getHealthService(), getContextBuilder(),
                    getAdminJobs(), getReactor());
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

    public String getHostName() {
        if (hostName == null) {

            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostName = UUID.randomUUID().toString();
            }
        }
        return hostName;
    }

    public AdminBuilder setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String getMachinName() {
        if (machinName == null) {
            machinName = getMicroServiceName() + "." + getHostName();
        }
        return machinName;
    }

    public AdminBuilder setMachinName(String machinName) {
        this.machinName = machinName;
        return this;
    }
}
