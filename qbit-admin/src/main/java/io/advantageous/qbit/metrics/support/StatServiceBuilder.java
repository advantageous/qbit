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


import io.advantageous.qbit.metrics.StatRecorder;
import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.metrics.StatService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhightower on 1/28/15.
 */
public class StatServiceBuilder {

    private StatRecorder recorder = new NoOpRecorder();

    private StatReplicator replicator = new NoOpReplicator();


    private List<StatReplicator> replicators = new ArrayList<>();


    public StatServiceBuilder addReplicator(StatReplicator replicator) {
        replicators.add(replicator);
        return this;
    }

    public StatRecorder getRecorder() {
        return recorder;
    }

    public StatServiceBuilder setRecorder(StatRecorder recorder) {
        this.recorder = recorder;
        return this;
    }

    public StatReplicator getReplicator() {
        return replicator;
    }

    public StatServiceBuilder setReplicator(StatReplicator replicator) {
        this.replicator = replicator;
        return this;
    }

    public StatService build() {

        if (replicators.size() == 0) {
            return new StatService(this.getRecorder(), this.getReplicator());
        } else {
            return new StatService(this.getRecorder(), new ReplicatorHub(replicators));
        }
    }
}
