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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.http.config;

/**
 * Created by rhightower on 2/14/15.
 */
public class HttpServerOptions implements Cloneable {

    protected String host;
    protected int port = 8080;
    protected boolean manageQueues = false;
    protected int maxRequestBatches = 1_000_000;
    protected boolean pipeline = true;
    protected int pollTime = 100;
    protected int requestBatchSize = 10;
    protected int flushInterval = 100;
    protected int workers = -1;


//TODO add these and then use them from Jetty.
//    httpServer.setTCPNoDelay(true);//TODO this needs to be in builder
//    httpServer.setSoLinger(0); //TODO this needs to be in builder
//    httpServer.setUsePooledBuffers(true); //TODO this needs to be in builder
//    httpServer.setReuseAddress(true); //TODO this needs to be in builder
//    httpServer.setAcceptBacklog(1_000_000); //TODO this needs to be in builder
//    httpServer.setTCPKeepAlive(true); //TODO this needs to be in builder
//    httpServer.setCompressionSupported(false);//TODO this needs to be in builder
//    httpServer.setMaxWebSocketFrameSize(100_000_000);

    public HttpServerOptions() {

    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getWorkers() {
        return workers;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isManageQueues() {
        return manageQueues;
    }

    public int getMaxRequestBatches() {
        return maxRequestBatches;
    }

    public boolean isPipeline() {
        return pipeline;
    }

    public int getPollTime() {
        return pollTime;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    @Override
    public String toString() {
        return "HttpServerOptions{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", manageQueues=" + manageQueues +
                ", maxRequestBatches=" + maxRequestBatches +
                ", pipeline=" + pipeline +
                ", pollTime=" + pollTime +
                ", requestBatchSize=" + requestBatchSize +
                ", flushInterval=" + flushInterval +
                ", workers=" + workers +
                '}';
    }
}
