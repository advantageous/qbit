/*******************************************************************************
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
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
  ******************************************************************************/

package io.advantageous.qbit.service.dispatchers;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.queue.SendQueue;

import java.util.concurrent.TimeUnit;

import static io.advantageous.boon.Boon.puts;

/**
 * @author  rhightower
 * on 2/18/15.
 */
public class ShardedMethodDispatcher extends ServiceWorkers {


    private final ShardRule shardRule;


    public ShardedMethodDispatcher(final ShardRule shardRule) {

        this.shardRule = shardRule;
    }

    public ShardedMethodDispatcher(final int flushInterval, final TimeUnit timeUnit, final ShardRule shardRule) {
       super(flushInterval, timeUnit);
       this.shardRule = shardRule;
    }

    @Override
    public void accept(final MethodCall<Object> methodCall) {

        final Object[] args = methodCall.args();
        final int shard = shardRule.shard(methodCall.name(), args, serviceQueues.size());

        final int index = shard >=0 ? shard : shard * -1;

        final SendQueue<MethodCall<Object>> methodCallSendQueue = sendQueues.get(index) ;

        methodCallSendQueue.send(methodCall);
    }
}
