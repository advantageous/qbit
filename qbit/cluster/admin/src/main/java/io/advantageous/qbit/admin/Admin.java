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

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.swagger.MetaTransformerFromQbitMetaToSwagger;
import io.advantageous.qbit.meta.swagger.ServiceEndpointInfo;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.NodeHealthStat;

import java.util.List;

@RequestMapping("/__admin")
public class Admin {


    private final HealthServiceAsync healthService;

    private final ServiceEndpointInfo serviceEndpointInfo;

    public Admin(final HealthServiceAsync healthService,
                 final ContextMetaBuilder contextMetaBuilder) {



        final ContextMeta context = contextMetaBuilder.build();
        final MetaTransformerFromQbitMetaToSwagger metaToSwagger =
                new MetaTransformerFromQbitMetaToSwagger();

        serviceEndpointInfo = metaToSwagger.serviceEndpointInfo(context);


        this.healthService = healthService;
    }

    @RequestMapping("/meta")
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


    @RequestMapping("/healthy-nodes")
    public void findAllHealthyNodes(final Callback<List<String>> callback) {

        healthService.findHealthyNodes(callback::accept);
        healthService.clientProxyFlush();
    }


    @RequestMapping("/load-nodes")
    public void loadNodes(final Callback<List<NodeHealthStat>> callback) {
        healthService.loadNodes(callback);
        healthService.clientProxyFlush();
    }


}
