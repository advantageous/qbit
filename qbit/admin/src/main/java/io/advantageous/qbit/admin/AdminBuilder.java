package io.advantageous.qbit.admin;


import io.advantageous.boon.core.IO;
import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
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


    private List<String> blackListForSystemProperties;

    private String microServiceName = null;

    /**
     * Used to generate meta data for services.
     */
    private ContextMetaBuilder contextBuilder;


    /**
     * Used to generate meta data for admin.
     */
    private ContextMetaBuilder adminContextBuilder;

    /**
     * Used to manage admin jobs.
     */
    private List<AdminJob> adminJobs;

    /**
     * Reactor to schedule admin jobs.
     */
    private Reactor reactor;


    /**
     * Reactor to schedule admin jobs.
     */
    private ReactorBuilder reactorBuilder;
    private String hostName;
    private String machineName;
    private boolean useMachineName = Sys.sysProp("QBIT_USE_MACHINE_NAME_FOR_STATS", true);
    private String statName;


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

    public List<String> getBlackListForSystemProperties() {

        if (blackListForSystemProperties == null) {

            final String blackListForSystemProps = System.getenv().get("BLACK_LIST_FOR_SYSTEM_PROPS");

            if (blackListForSystemProps == null) {

                blackListForSystemProperties = new ArrayList<>();
                blackListForSystemProperties.add("PWD");
                blackListForSystemProperties.add("PASSWORD");
            } else {
                final String[] names = Str.splitComma(blackListForSystemProps);
                final List<String> list = Lists.list(names);
                blackListForSystemProperties = list;
            }
        }
        return blackListForSystemProperties;
    }

    public AdminBuilder setBlackListForSystemProperties(
            final List<String> blackListForSystemProperties) {
        this.blackListForSystemProperties =
                blackListForSystemProperties;
        return this;
    }

    public AdminBuilder addBlackListSystemProperty(final String pattern) {

        getBlackListForSystemProperties().add(pattern);
        return this;
    }

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

    public AdminBuilder setAdminJobs(final List<AdminJob> adminJobs) {
        this.adminJobs = adminJobs;
        return this;
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

    public AdminBuilder setMicroServiceName(final String microServiceName) {
        this.microServiceName = microServiceName;
        return this;
    }

    public AdminBuilder registerJavaVMStatsJob(final StatsCollector statsCollector) {

        final JavaStatsCollectorJob jvmStatsJob = new JavaStatsCollectorJob(60, TimeUnit.SECONDS, statsCollector,
                getStatName());
        return addAdminJob(jvmStatsJob);
    }

    public AdminBuilder registerJavaVMStatsJobEveryNSeconds(final StatsCollector statsCollector, final int everySeconds) {
        final JavaStatsCollectorJob jvmStatsJob = new JavaStatsCollectorJob(everySeconds, TimeUnit.SECONDS, statsCollector, getMachineName());
        return addAdminJob(jvmStatsJob);
    }

    public String getHtmlPageLocation() {
        return htmlPageLocation;
    }

    public AdminBuilder setHtmlPageLocation(final String htmlPageLocation) {
        this.htmlPageLocation = htmlPageLocation;
        return this;
    }


    public AdminBuilder setWebCotentsSupplier(final Supplier<String> webPageContentsSupplier) {
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

    public String getWebPageContents() {

        if (webPageContents == null || webPageContents.isEmpty()) {

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

    public AdminBuilder setWebPageContents(String webPageContents) {
        this.webPageContents = webPageContents;
        return this;
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

    public AdminBuilder setHttpServer(HttpServer httpServer) {

        this.httpServer = httpServer;
        return this;
    }

    public HttpServerBuilder getHttpServerBuilder() {
        if (httpServerBuilder == null) {
            httpServerBuilder = HttpServerBuilder.httpServerBuilder();
        }
        return httpServerBuilder;
    }

    public AdminBuilder setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
        return this;
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
            admin = new Admin(getHealthService(), getContextBuilder(), getAdminContextBuilder(),
                    getAdminJobs(), getReactor(), new ArrayList<>(this.getBlackListForSystemProperties()));
        }
        return admin;
    }

    public AdminBuilder setAdmin(Admin admin) {
        this.admin = admin;
        return this;
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

    public AdminBuilder setServiceEndpointServer(final ServiceEndpointServer serviceEndpointServer) {
        this.serviceEndpointServer = serviceEndpointServer;
        return this;
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

    public ContextMetaBuilder getContextBuilder() {

        if (contextBuilder == null) {
            contextBuilder = ContextMetaBuilder.contextMetaBuilder();
        }
        return contextBuilder;
    }

    public AdminBuilder setContextBuilder(final ContextMetaBuilder contextBuilder) {
        this.contextBuilder = contextBuilder;
        return this;
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

    public String getMachineName() {
        if (machineName == null) {
            machineName = (getMicroServiceName() + "." + getHostName()).replace("..", ".");
        }
        return machineName;
    }

    public AdminBuilder setMachineName(String machineName) {
        this.machineName = machineName;
        return this;
    }

    public ContextMetaBuilder getAdminContextBuilder() {
        if (adminContextBuilder == null) {
            adminContextBuilder = ContextMetaBuilder.contextMetaBuilder();
            adminContextBuilder.setDescription("QBit Admin interface, used to administrate and query status of QBit services");
            adminContextBuilder.setTitle("QBit Admin interface");
            adminContextBuilder.setVersion("0.9");
            adminContextBuilder.setLicenseURL("https://github.com/advantageous/qbit/blob/master/License");
            adminContextBuilder.setContactURL("http://www.mammatustech.com/");
            adminContextBuilder.setRootURI(this.getEndpointServerBuilder().getUri());
            if (this.getEndpointServerBuilder().getHost() != null) {
                adminContextBuilder.setHostAddress(this.getEndpointServerBuilder().getHost() + ":" + this.getEndpointServerBuilder().getPort());
            } else {
                adminContextBuilder.setHostAddress("localhost:" + this.getEndpointServerBuilder().getPort());
            }
            adminContextBuilder.addService(Admin.class);
        }
        return adminContextBuilder;

    }

    public AdminBuilder setAdminContextBuilder(final ContextMetaBuilder adminContextBuilder) {
        this.adminContextBuilder = adminContextBuilder;
        return this;
    }

    public boolean isUseMachineName() {
        return useMachineName;
    }

    public AdminBuilder setUseMachineName(boolean useMachineName) {
        this.useMachineName = useMachineName;
        return this;
    }

    public String getStatName() {
        if (statName == null) {
            statName = System.getenv("MICRO_SERVICE_STAT_NAME");
            if (statName == null) {
                statName = isUseMachineName() ? getMachineName() : getMicroServiceName();
            }
        }
        return statName;
    }

    public void setStatName(String statName) {
        this.statName = statName;
    }
}
