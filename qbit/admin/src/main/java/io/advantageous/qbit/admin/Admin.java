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

package io.advantageous.qbit.admin;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.swagger.MetaTransformerFromQbitMetaToSwagger;
import io.advantageous.qbit.meta.swagger.ServiceEndpointInfo;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.NodeHealthStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Health system is used to provide information about the microserivce for health,
 * JVM parameters for debugging, system properties, info about the OS and Swagger
 * meta data for API gateway documents and client generation.
 */
@RequestMapping("/__admin")
public class Admin {


    /**
     * Health service. Admin provides a facade to the health service.
     */
    private final HealthServiceAsync healthService;


    /**
     * End point that describes the admin.
     */
    private final ServiceEndpointInfo adminServiceEndpoint;


    /**
     * End point that describes the services.
     */
    private final ServiceEndpointInfo serviceEndpointInfo;


    private final Logger logger = LoggerFactory.getLogger(Admin.class);

    /**
     * Admin uses the reactor to manage callbacks and periodic jobs.
     */
    private final Reactor reactor;


    /**
     * Memory JMX bean to query JVM state.
     */
    private final MemoryMXBean memoryMXBean;

    /**
     * OS JMX bean to query OS info.
     */
    private final OperatingSystemMXBean operatingSystemMXBean;

    /**
     * Thread JMX bean to query # of threads.
     */
    private final ThreadMXBean threadMXBean;

    /**
     * Runtime JMX bean to query info about JVM version.
     */
    private final RuntimeMXBean runtimeMXBean;


    /**
     * List of blacklisted words that we do not allow when we query the env variables and system properties.
     */
    private final List<String> blackListForSystemProperties;


    /**
     * Construct the admin
     *
     * @param healthService                health service
     * @param adminContextMetaBuilder      meta data support for the Admin itself.
     * @param contextMetaBuilder           meta data support
     * @param adminJobs                    list of periodic admin jobs
     * @param reactor                      reactor
     * @param blackListForSystemProperties black list for env variables and system properties we don't want queried.
     */
    public Admin(final HealthServiceAsync healthService,
                 final ContextMetaBuilder contextMetaBuilder,
                 final ContextMetaBuilder adminContextMetaBuilder,
                 final List<AdminJob> adminJobs,
                 final Reactor reactor,
                 final List<String> blackListForSystemProperties) {

        this.reactor = reactor;
        for (AdminJob adminJob : adminJobs) {
            reactor.addRepeatingTask(adminJob.every(), adminJob.timeUnit(),
                    adminJob.runnable());
        }

        this.serviceEndpointInfo = createServiceEndpointInfo(contextMetaBuilder);
        this.adminServiceEndpoint = createServiceEndpointInfo(adminContextMetaBuilder);
        this.healthService = healthService;

        memoryMXBean = ManagementFactory.getMemoryMXBean();
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        threadMXBean = ManagementFactory.getThreadMXBean();
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.blackListForSystemProperties = blackListForSystemProperties;

    }

    private ServiceEndpointInfo createServiceEndpointInfo(final ContextMetaBuilder contextMetaBuilder) {


        final ContextMeta context = contextMetaBuilder.build();
        final MetaTransformerFromQbitMetaToSwagger metaToSwagger =
                new MetaTransformerFromQbitMetaToSwagger();

        ServiceEndpointInfo serviceEndpointInfo;

        try {
            serviceEndpointInfo = metaToSwagger.serviceEndpointInfo(context);
        } catch (Exception ex) {
            serviceEndpointInfo = null;
            logger.warn("Unable to handle initialize swagger meta-data. The admin will still start.", ex);
        }
        return serviceEndpointInfo;
    }


    /**
     * Run a gc if possible.
     *
     * @return true if no exceptions
     */
    @RequestMapping(value = "/suggest-gc", summary = "run a gc if possible",
            description = "Run a System.gc().",
            returnDescription = "if run and there were no exceptions, returns true"
    )
    public boolean suggestGC() {
        System.gc();
        return true;
    }

    /**
     * Read annotation.
     *
     * @return swagger meta data
     */
    @RequestMapping(value = "/meta/", summary = "swagger meta data about this service",
            description = "Swagger meta data. Swagger is used to generate " +
                    "documents and clients.",
            returnDescription = "returns Swagger 2.0 JSON meta data."
    )
    public ServiceEndpointInfo getServiceEndpointInfo() {

        return serviceEndpointInfo;
    }


    /**
     * Read annotation.
     *
     * @return swagger meta data for admin
     */
    @RequestMapping(value = "/admin-meta/", summary = "swagger meta data about the admin services",
            description = "Swagger admin meta data for the admin itself. Swagger is used to generate " +
                    "documents and clients.",
            returnDescription = "returns Swagger 2.0 JSON meta data."
    )
    public ServiceEndpointInfo getAdminServiceEndpoint() {

        return adminServiceEndpoint;
    }


    /**
     * Read annotation.
     *
     * @param callback callback
     */
    @RequestMapping(value = "/ok",
            summary = "simple health check",
            description = "Health check. This returns true if all nodes (service actors) are healthy",
            returnDescription = "true if all nodes are healthy, false if all nodes are not healthy")
    public void ok(final Callback<Boolean> callback) {

        healthService.ok(callback::accept);
        healthService.clientProxyFlush();
    }


    /**
     * Checks to see if the key is black listed
     *
     * @param key key
     * @return true if blacklisted
     */
    private final boolean isBlackListed(final String key) {
        return blackListForSystemProperties.stream()
                .anyMatch(blackListedWord -> key.toUpperCase()
                        .contains(blackListedWord.toUpperCase()));
    }


    @RequestMapping(value = "/system/property/",
            summary = "",
            description = "",
            returnDescription = "")
    public Map<String, String> getSystemProperties() {
        Map<String, String> propertyMap = new LinkedHashMap<>(System.getProperties().size());

        System.getProperties().entrySet().stream()
                .filter(entry -> !isBlackListed((String) entry.getKey()))
                .forEach(
                        entry -> propertyMap.put(entry.getKey().toString(), entry.getValue().toString()));
        return propertyMap;
    }


    @RequestMapping(value = "/system/property",
            summary = "",
            description = "",
            returnDescription = "")
    public String getSystemProperty(@RequestParam(value = "p", required = true) final String propertyName) {

        if (!isBlackListed(propertyName)) {
            return System.getProperties().getProperty(propertyName);
        } else {
            return "***********";
        }
    }


    @RequestMapping(value = "/env/variable/",
            summary = "",
            description = "",
            returnDescription = "")
    public Map<String, String> getEnvironmentVariables() {

        final Map<String, String> envMap = new LinkedHashMap<>(System.getenv().size());
        System.getenv().entrySet().stream()
                .filter(entry -> !isBlackListed(entry.getKey()))
                .forEach(entry -> envMap.put(entry.getKey(), entry.getValue()));

        return envMap;
    }

    @RequestMapping(value = "/env/variable",
            summary = "",
            description = "",
            returnDescription = "")
    public String getEnvironmentVariable(@RequestParam(value = "v", required = true)
                                         final String variableName) {

        if (!isBlackListed(variableName)) {
            return System.getenv(variableName);

        } else {
            return "***********";
        }
    }

    @RequestMapping(value = "/available-processors",
            summary = "Available Processors",
            description = "This value may change during a particular invocation of the virtual" +
                    " machine.  Applications that are sensitive to the number of available" +
                    " processors should therefore occasionally poll this property and adjust" +
                    " their resource usage appropriately.",
            returnDescription = "the maximum number of processors available to the virtual" +
                    "machine; never smaller than one")
    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }


    @RequestMapping(value = "/memory/free",
            summary = "Free Memory",
            description = "The amount of free memory in the Java Virtual Machine." +
                    "Calling the `gc` method may result in increasing the value returned" +
                    "by `freeMemory`.",
            returnDescription = "Returns the amount of free memory in the Java Virtual Machine.")
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }


    @RequestMapping(value = "/memory/total",
            summary = "Total Memory",
            description = "Total Memory",
            returnDescription = "Total Memory")
    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }


    @RequestMapping(value = "/memory/max",
            summary = "Max Memory",
            description = "Max Memory",
            returnDescription = "Max Memory")
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @RequestMapping(value = "/memory/heap/usage",
            summary = "Heap Usage",
            description = "Heap Usage",
            returnDescription = "Heap Usage")
    public long getMemoryHeapUsage() {

        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    @RequestMapping(value = "/memory/non-heap/usage",
            summary = "Non-heap Usage",
            description = "Non-heap Usage",
            returnDescription = "Non-heap Usage")
    public long getMemoryNonHeapUsage() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }


    @RequestMapping(value = "/thread/count",
            summary = "Thread count",
            description = "Thread count",
            returnDescription = "Thread count")
    public int getThreadCount() {
        return threadMXBean.getThreadCount();
    }


    @RequestMapping(value = "/os/load-average",
            summary = "OS load average for last minute",
            description = "The system load average is the sum of the number of runnable entities" +
                    " queued to the {@linkplain #getAvailableProcessors available processors}" +
                    " and the number of runnable entities running on the available processors" +
                    " averaged over a period of time." +
                    " The way in which the load average is calculated is operating system" +
                    " specific but is typically a damped time-dependent average.",
            returnDescription = "Returns the system load average for the last minute.")
    public double getSystemLoadAverage() {
        return operatingSystemMXBean.getSystemLoadAverage();
    }


    @RequestMapping(value = "/os/name",
            summary = "OS Name",
            description = "The operating system name",
            returnDescription = "Returns the operating system name")
    public String getOSName() {
        return operatingSystemMXBean.getName();
    }

    @RequestMapping(value = "/os/arch",
            summary = "OS Architecture",
            description = "OS Architecture",
            returnDescription = "OS Architecture")
    public String getOSArch() {
        return operatingSystemMXBean.getArch();
    }


    @RequestMapping(value = "/os/version",
            summary = "OS Version",
            description = "OS Version",
            returnDescription = "OS Version")
    public String getOSVersion() {
        return operatingSystemMXBean.getVersion();
    }


    @RequestMapping(value = "/runtime/classpath",
            summary = "Classpath",
            description = "The classpath of the JVM that is running",
            returnDescription = "Classpath")
    public String getClassPath() {
        return runtimeMXBean.getClassPath();
    }


    @RequestMapping(value = "/runtime/boot-classpath",
            summary = "Boot classpath",
            description = "Boot classpath",
            returnDescription = "Boot classpath")
    public String getBootClassPath() {
        return runtimeMXBean.getBootClassPath();
    }


    @RequestMapping(value = "/runtime/vm-version",
            summary = "JVM version",
            description = "JVM version",
            returnDescription = "JVM version")
    public String getVmVersion() {
        return runtimeMXBean.getVmVersion();
    }


    @RequestMapping(value = "/runtime/vm-vendor",
            summary = "JVM Vendor",
            description = "JVM Vendor",
            returnDescription = "JVM Vendor")
    public String getVmVendor() {
        return runtimeMXBean.getVmVendor();
    }


    @RequestMapping(value = "/runtime/lib-path",
            summary = "Runtime library path",
            description = "Runtime library path",
            returnDescription = "Runtime library path")
    public String getLibPath() {
        return runtimeMXBean.getLibraryPath();
    }


    @RequestMapping(value = "/runtime/spec-name",
            summary = "Spec name",
            description = "Specification of JVM name",
            returnDescription = "Spec name")
    public String getSpecName() {
        return runtimeMXBean.getSpecName();
    }


    @RequestMapping(value = "/runtime/spec-version",
            summary = "Spec version",
            description = "Spec version",
            returnDescription = "Spec version")
    public String getSpecVersion() {
        return runtimeMXBean.getSpecVersion();
    }

    @RequestMapping(value = "/runtime/spec-vendor",
            summary = "Spec vendor",
            description = "Spec vendor",
            returnDescription = "Spec vendor")
    public String getSpecVendor() {
        return runtimeMXBean.getSpecVendor();
    }


    @RequestMapping(value = "/healthy-nodes/",
            summary = "List of healthy nodes",
            description = "List of nodes that are healthy.",
            returnDescription = "List of healthy nodes")
    public void findAllHealthyNodes(final Callback<List<String>> callback) {

        healthService.findHealthyNodes(callback::accept);
        healthService.clientProxyFlush();
    }


    @RequestMapping(value = "/load-nodes/",
            summary = "Load all health info about all nodes",
            description = "Load all health info about all nodes",
            returnDescription = "list of healthy nodes")
    public void loadNodes(final Callback<List<NodeHealthStat>> callback) {
        healthService.loadNodes(callback);
        healthService.clientProxyFlush();
    }

    /**
     * Read annotation.
     *
     * @param callback callback
     */
    @RequestMapping(value = "/all-nodes/",
            summary = "Finds all nodes that have registered with health system",
            description = "Finds all service actors and endpoints that are registered with the health system." +
                    "Each node will periodically check in with the health system." +
                    "Nodes can mark themselves unhealthy or just fail to check in",
            returnDescription = "List of node names")
    public void findAllNodes(final Callback<List<String>> callback) {

        healthService.findAllNodes(callback::accept);
        healthService.clientProxyFlush();
    }


    @QueueCallback({QueueCallbackType.IDLE, QueueCallbackType.EMPTY,
            QueueCallbackType.LIMIT})
    public void process() {
        this.reactor.process();
    }


}
