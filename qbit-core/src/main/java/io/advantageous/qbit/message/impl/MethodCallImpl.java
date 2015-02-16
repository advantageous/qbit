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

package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.util.MultiMap;

/**
 * This is runs a method call for an RPC call.
 * <p>
 * Created by Richard on 8/11/14.
 */
public class MethodCallImpl implements MethodCall<Object> {


    private final long timestamp;
    private final long id;
    private final String name;
    private final String address;
    private final MultiMap<String, String> params;
    private final MultiMap<String, String> headers;
    private final Object body;
    private final String objectName;
    private final String returnAddress;
    private Object transformedBody;
    private Request<Object> originatingRequest;


    public MethodCallImpl(long timestamp, long id, String name, String address, MultiMap<String, String> params, MultiMap<String, String> headers, Object body, String objectName, String returnAddress, Request<Object> originatingRequest) {
        this.timestamp = timestamp;
        this.id = id;
        this.name = name;
        this.address = address;
        this.params = params;
        this.headers = headers;
        this.body = body;
        this.objectName = objectName;
        this.returnAddress = returnAddress;
        this.originatingRequest = originatingRequest;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public boolean isHandled() {
        return false;
    }

    @Override
    public void handled() {

    }

    @Override
    public String objectName() {
        return objectName;
    }

    @Override
    public Request<Object> originatingRequest() {
        return originatingRequest;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public String returnAddress() {
        return returnAddress;
    }

    @Override
    public MultiMap<String, String> params() {
        return this.params;
    }

    @Override
    public MultiMap<String, String> headers() {
        return this.headers;
    }

    @Override
    public Object body() {
        return transformedBody == null ? body : transformedBody;
    }


    public void setBody(Object[] body) {

        this.transformedBody = body;
    }


    public void originatingRequest(Request<Object> originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean hasParams() {
        return params != null && params.size() > 0;
    }

    @Override
    public boolean hasHeaders() {

        return headers != null && headers.size() > 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodCallImpl)) return false;
        final MethodCallImpl method = (MethodCallImpl) o;
        return id == method.id && !(address != null ? !address.equals(method.address) : method.address != null)
                && !(body != null ? !body.equals(method.body) : method.body != null)
                && !(name != null ? !name.equals(method.name) : method.name != null)
                && !(params != null ? !params.equals(method.params) : method.params != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MethodCallImpl{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", params=" + params +
                ", body=" + body +
                ", timestamp=" + timestamp +
                ", id=" + id +
                ", objectName='" + objectName + '\'' +
                ", returnAddress='" + returnAddress + '\'' +
                '}';
    }


}
