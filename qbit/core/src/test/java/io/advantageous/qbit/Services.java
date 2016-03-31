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

package io.advantageous.qbit;

import io.advantageous.qbit.boon.spi.BoonJsonMapper;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.impl.ServiceQueueImpl;
import io.advantageous.qbit.transforms.JsonRequestBodyToArgListTransformer;
import io.advantageous.qbit.transforms.JsonResponseTransformer;

/**
 * created by Richard on 8/26/14.
 */
public class Services {

    public static ServiceQueue jsonService(final String name, Object service
    ) {
        JsonMapper mapper = new BoonJsonMapper();


        ServiceQueueImpl serviceQueue = (ServiceQueueImpl) ServiceBuilder.serviceBuilder().setServiceAddress(name).setServiceObject(service).build();

        serviceQueue.requestObjectTransformer(new JsonRequestBodyToArgListTransformer(mapper));
        serviceQueue.responseObjectTransformer(new JsonResponseTransformer(mapper));
        serviceQueue.start();
        return serviceQueue;
    }

    public static ServiceQueue regularService(final String name, Object service) {


        ServiceQueueImpl serviceQueue = (ServiceQueueImpl) ServiceBuilder.serviceBuilder().setServiceAddress(name).setServiceObject(service).build();
        serviceQueue.start();
        return serviceQueue;

    }
}
