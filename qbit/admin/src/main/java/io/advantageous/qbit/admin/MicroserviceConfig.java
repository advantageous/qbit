package io.advantageous.qbit.admin;

import io.advantageous.boon.core.IO;
import io.advantageous.boon.core.Sys;
import io.advantageous.boon.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * Hold service config for a give service.
 * This is all of the stuff that will show up in Swagger file.
 * It is all informational, and will allow for generation of swagger clients.
 */
public class MicroserviceConfig {


    /**
     * Holds the key.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String CONTEXT = "qbit.service.config.";
    /**
     * Contains the resource location of the configuration.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String resourceLocation =
            Sys.sysProp(CONTEXT + "file", "/opt/qbit/conf/service.json");
    public static final String resourceLocationEnv = System.getenv("QBIT_CONF_FILE");
    /**
     * Title
     */
    private final String title;
    /**
     * Description
     */
    private final String description;
    /**
     * publicHost this is where the default API from swagger will make calls.
     */
    private final String publicHost;
    /**
     * publicPort this is the port where the default API from swagger will make calls.
     */
    private final int publicPort;
    /**
     * this is the port where the default API from swagger will make calls.
     */
    private final int port;
    /**
     * Contact name. This is Stephane's home phone number.
     */
    private final String contactName;
    /**
     * Contact email. This is Stephane's home email address.
     */
    private final String contactEmail;
    /**
     * Contact email. This is Stephane's website.
     */
    private final String contactURL;
    /**
     * Version of the API.
     */
    private final String version;
    /**
     * Root URI, this should be /v1, /v2, etc.
     */
    private final String rootURI;
    /**
     * License Name of the microservice.
     */
    private String licenseName;
    /**
     * License URL of the microservice.
     */
    private String licenseURL;
    /**
     * StatsD host where we put things.
     */
    private String statsDHost;
    /**
     * StatsD port. -1 means use default StatsD port.
     */
    private int statsDPort = -1;
    private int sampleStatFlushRate = 5;
    private int checkTimingEveryXCalls = 100;
    /**
     * Enables local stats collection.
     */
    private boolean enableLocalStats = true;
    /**
     * Enables sending stats to stats D.
     */
    private boolean enableStatsD = false;
    /**
     * Enables local health stats collection.
     */
    private boolean enableLocalHealth = true;
    /**
     * Enables the collection of stats.
     */
    private boolean enableStats = true;
    private boolean statsD;


    /**
     * @param title        title
     * @param description  description
     * @param publicHost   publicHost
     * @param publicPort   publicPort
     * @param port         port
     * @param contactName  contactName
     * @param contactEmail contactEmail
     * @param version      version
     * @param rootURI      rootURI
     */
    public MicroserviceConfig(String title, String description, String publicHost, int publicPort,
                              int port, String contactName,
                              String contactEmail, String version, String rootURI, String contactURL) {
        this.title = title;
        this.description = description;
        this.publicHost = publicHost;
        this.publicPort = publicPort;
        this.port = port;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.version = version;
        this.rootURI = rootURI;
        this.contactURL = contactURL;
    }

    /**
     * Reads the readConfig file, which can be a classpath or file system resource.
     *
     * @param serviceName the name of the service to load
     * @return service config
     */
    public static MicroserviceConfig readConfig(final String serviceName) {


        final Logger logger = LoggerFactory.getLogger(MicroserviceConfig.class);
        if (new File(resourceLocation).exists()) {
            final String json = IO.read(resourceLocation);

            return JsonFactory.fromJson(json, MicroserviceConfig.class);

        } else if (resourceLocationEnv != null && new File(resourceLocationEnv).exists()) {

            final String json = IO.read(resourceLocationEnv);
            return JsonFactory.fromJson(json, MicroserviceConfig.class);
        } else {
            logger.info("Reading config from classpath as it is not found on file system");

            final String qbitEnv = System.getenv("QBIT_ENV");

            final String resourceLocationOnClasspath =
                    qbitEnv != null && !qbitEnv.isEmpty() ? serviceName + "_" + qbitEnv + ".json" :
                            serviceName + ".json";
            final String json = IO.read(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(resourceLocationOnClasspath));
            return JsonFactory.fromJson(json, MicroserviceConfig.class);
        }
    }

    public String getPublicHost() {
        return publicHost;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public String getContactName() {
        return contactName;
    }

    public String getTitle() {
        return title;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getVersion() {
        return version;
    }

    public String getRootURI() {
        return rootURI;
    }

    public String getDescription() {
        return description;
    }


    public String getContactURL() {
        return contactURL;
    }

    @Override
    public String toString() {
        return "MicroserviceConfig{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", publicHost='" + publicHost + '\'' +
                ", publicPort=" + publicPort +
                ", contactName='" + contactName + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", contactURL='" + contactURL + '\'' +
                ", version='" + version + '\'' +
                ", rootURI='" + rootURI + '\'' +
                '}';
    }

    public int getPort() {
        return port == 0 ? 8080 : port;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseURL() {
        return licenseURL;
    }

    public void setLicenseURL(String licenseURL) {
        this.licenseURL = licenseURL;
    }

    public boolean isStatsD() {
        return statsD;
    }

    public void setStatsD(boolean statsD) {
        this.statsD = statsD;
    }

    public String getStatsDHost() {
        return statsDHost;
    }

    public void setStatsDHost(String statsDHost) {
        this.statsDHost = statsDHost;
    }

    public int getStatsDPort() {
        return statsDPort <= 0 ? 8125 : statsDPort;
    }

    public void setStatsDPort(int statsDPort) {
        this.statsDPort = statsDPort;
    }

    public int getSampleStatFlushRate() {
        return sampleStatFlushRate;
    }

    public void setSampleStatFlushRate(int sampleStatFlushRate) {
        this.sampleStatFlushRate = sampleStatFlushRate;
    }

    public int getCheckTimingEveryXCalls() {
        return checkTimingEveryXCalls;
    }

    public void setCheckTimingEveryXCalls(int checkTimingEveryXCalls) {
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
    }

    public boolean isEnableLocalStats() {
        return enableLocalStats;
    }

    public void setEnableLocalStats(boolean enableLocalStats) {
        this.enableLocalStats = enableLocalStats;
    }

    public boolean isEnableStatsD() {
        return enableStatsD;
    }

    public void setEnableStatsD(boolean enableStatsD) {
        this.enableStatsD = enableStatsD;
    }

    public boolean isEnableLocalHealth() {
        return enableLocalHealth;
    }

    public void setEnableLocalHealth(boolean enableLocalHealth) {
        this.enableLocalHealth = enableLocalHealth;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}
