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
import io.advantageous.qbit.service.stats.StatsCollector;

import java.lang.management.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequestMapping("/__admin")
public class Admin {


    private final HealthServiceAsync healthService;

    private final ServiceEndpointInfo serviceEndpointInfo;
    private final Reactor reactor;


    private final MemoryMXBean memoryMXBean;
    private final OperatingSystemMXBean operatingSystemMXBean;
    private final ThreadMXBean threadMXBean;
    private final RuntimeMXBean runtimeMXBean;




    public Admin(final HealthServiceAsync healthService,
                 final ContextMetaBuilder contextMetaBuilder,
                 final List<AdminJob> adminJobs,
                 final Reactor reactor) {

        this.reactor = reactor;
        for (AdminJob adminJob : adminJobs) {
            reactor.addRepeatingTask(adminJob.every(), adminJob.timeUnit(),
                    adminJob.runnable());
        }

        final ContextMeta context = contextMetaBuilder.build();
        final MetaTransformerFromQbitMetaToSwagger metaToSwagger =
                new MetaTransformerFromQbitMetaToSwagger();

        serviceEndpointInfo = metaToSwagger.serviceEndpointInfo(context);


        this.healthService = healthService;

        memoryMXBean = ManagementFactory.getMemoryMXBean();
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        threadMXBean = ManagementFactory.getThreadMXBean();
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    }

    @RequestMapping("/meta/")
    public ServiceEndpointInfo getServiceEndpointInfo() {

        return serviceEndpointInfo;
    }

    @RequestMapping("/ok")
    public void ok(final Callback<Boolean> callback) {

        healthService.ok(callback::accept);
        healthService.clientProxyFlush();
    }


    @RequestMapping("/all-nodes/")
    public void findAllNodes(final Callback<List<String>> callback) {

        healthService.findAllNodes(callback::accept);
        healthService.clientProxyFlush();
    }

    @RequestMapping("/system/property/")
    public Map<String, String> getSystemProperties() {
        Map<String, String> propertyMap = new LinkedHashMap<>(System.getProperties().size());

        System.getProperties().entrySet().stream()
                .filter(entry ->
                    !entry.getKey().toString().toUpperCase().contains("PASSWORD"))
                .forEach(
                    entry -> propertyMap.put(entry.getKey().toString(), entry.getValue().toString()));
        return propertyMap;
    }


    @RequestMapping("/system/property")
    public String getSystemProperty(@RequestParam(value = "p", required = true) final String propertyName) {

        if (!(propertyName.toUpperCase().contains("PASSWORD"))) {
            return System.getProperties().getProperty(propertyName);
        } else {
            return "***********";
        }
    }



    @RequestMapping("/env/variable/")
    public Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    @RequestMapping("/env/variable")
    public String getEnvironmentVariable(@RequestParam("v") final String variableName) {
        return System.getProperties().getProperty(variableName);
    }

    @RequestMapping("/available-processors")
    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }


    @RequestMapping("/memory/free")
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }


    @RequestMapping("/memory/total")
    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }


    @RequestMapping("/memory/max")
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @RequestMapping("/memory/heap/usage")
    public long getMemoryHeapUsage() {

        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    @RequestMapping("/memory/non-heap/usage")
    public long getMemoryNonHeapUsage() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }


    @RequestMapping("/thread/count")
    public int getThreadCount() {
        return threadMXBean.getThreadCount();
    }


    @RequestMapping("/os/load-average")
    public double getSystemLoadAverage() {
        return operatingSystemMXBean.getSystemLoadAverage();
    }


    @RequestMapping("/os/name")
    public String getOSName() {
        return operatingSystemMXBean.getName();
    }

    @RequestMapping("/os/arch")
    public String getOSArch() {
        return operatingSystemMXBean.getArch();
    }


    @RequestMapping("/os/version")
    public String getOSVersion() {
        return operatingSystemMXBean.getVersion();
    }


    @RequestMapping("/runtime/classpath")
    public String getClassPath() {
        return runtimeMXBean.getClassPath();
    }


    @RequestMapping("/runtime/boot-classpath")
    public String getBootClassPath() {
        return runtimeMXBean.getBootClassPath();
    }


    @RequestMapping("/runtime/vm-version")
    public String getVmVersion() {
        return runtimeMXBean.getVmVersion();
    }


    @RequestMapping("/runtime/vm-vendor")
    public String getVmVendor() {
        return runtimeMXBean.getVmVendor();
    }


    @RequestMapping("/runtime/lib-pat")
    public String getLibPath() {
        return runtimeMXBean.getLibraryPath();
    }


    @RequestMapping("/runtime/spec-name")
    public String getSpecName() {
        return runtimeMXBean.getSpecName();
    }



    @RequestMapping("/runtime/spec-version")
    public String getSpecVersion() {
        return runtimeMXBean.getSpecVersion();
    }

    @RequestMapping("/runtime/spec-vendor")
    public String getSpecVendor() {
        return runtimeMXBean.getSpecVendor();
    }


    @RequestMapping("/healthy-nodes/")
    public void findAllHealthyNodes(final Callback<List<String>> callback) {

        healthService.findHealthyNodes(callback::accept);
        healthService.clientProxyFlush();
    }


    @RequestMapping("/load-nodes/")
    public void loadNodes(final Callback<List<NodeHealthStat>> callback) {
        healthService.loadNodes(callback);
        healthService.clientProxyFlush();
    }


    @QueueCallback({QueueCallbackType.IDLE, QueueCallbackType.EMPTY,
    QueueCallbackType.LIMIT})
    public void process() {
        this.reactor.process();
    }


}
