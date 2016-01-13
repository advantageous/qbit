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

package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.service.discovery.ServiceChangedEventChannel;

import java.util.List;

/**
 * created by rhightower on 1/28/15.
 */
public class ReplicatorHub implements StatReplicator, ServiceChangedEventChannel {

    private final List<StatReplicator> list;

    public ReplicatorHub(List<StatReplicator> list) {
        this.list = list;
    }

    @Override
    public void replicateCount(String name, long count, long now) {
        for (StatReplicator replicator : list) {
            replicator.replicateCount(name, count, now);
        }
    }

    @Override
    public void replicateLevel(String name, long level, long time) {
        for (StatReplicator replicator : list) {
            replicator.replicateLevel(name, level, time);
        }

    }

    @Override
    public void replicateTiming(String name, long level, long time) {
        for (StatReplicator replicator : list) {
            replicator.replicateTiming(name, level, time);
        }

    }


    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public void servicePoolChanged(final String serviceName) {

        list.stream().filter(replicator -> replicator instanceof ServiceChangedEventChannel).forEach(replicator -> {
            ((ServiceChangedEventChannel) replicator).servicePoolChanged(serviceName);
        });

    }


    public void flush() {


        list.forEach(StatReplicator::flush);
    }

}
