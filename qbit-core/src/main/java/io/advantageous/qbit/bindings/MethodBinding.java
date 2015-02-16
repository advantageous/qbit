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

package io.advantageous.qbit.bindings;

import java.util.ArrayList;
import java.util.List;

/**
 * This binds a method to an annotated method in a client.
 * <p>
 * Created by Richard on 7/22/14.
 */
public class MethodBinding {

    private final boolean hasURIParams;
    private final String methodName;
    private final String method;

    private final String address;

    private final List<ArgParamBinding> parameters = new ArrayList<>();

    private final List<RequestParamBinding> requestParamBindings = new ArrayList<>();

    private RequestParamBinding[] requestParamBindingsMap;

    public MethodBinding(String method, String methodName, String uri) {
        this.methodName = methodName;
        this.method = method;

        final String[] split = uri.split("/");

        boolean found = false;
        int indexOfFirstParam = -1;

        int index = 0;
        for (String item : split) {
            if (item.startsWith("{") && item.endsWith("}")) {
                if (indexOfFirstParam == -1) {
                    indexOfFirstParam = index;
                }
                found = true;
                item = item.substring(1, item.length() - 1);
                ArgParamBinding binding;
                if (item.matches("\\d+")) {
                    binding = new ArgParamBinding(Integer.parseInt(item), index, "");
                } else {
                    binding = new ArgParamBinding(-1, index, item);
                }
                parameters.add(binding);
            }
            index++;
        }

        if (indexOfFirstParam != -1) {
            final int end = uri.indexOf('{');
            this.address = uri.substring(0, end > -1 ? end : uri.length());
        } else {
            this.address = uri;

        }
        hasURIParams = found;
    }

    public String methodName() {
        return methodName;
    }

    public String method() {
        return method;
    }

    public String address() {
        return address;
    }

    public List<ArgParamBinding> parameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "MethodBinding{" +
                "hasURIParams=" + hasURIParams +
                ", methodName='" + methodName + '\'' +
                ", method='" + methodName + '\'' +
                ", address='" + address + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    public void addRequestParamBinding(int length, int index, String pathVarName,
                                       boolean required, String defaultValue) {

        if (requestParamBindingsMap == null) {
            requestParamBindingsMap = new RequestParamBinding[length];
        }
        RequestParamBinding paramBinding = new RequestParamBinding(pathVarName, index,
                required, defaultValue);

        requestParamBindings.add(paramBinding);

        requestParamBindingsMap[index] = paramBinding;
    }

    public RequestParamBinding requestParamBinding(int index) {

        return requestParamBindingsMap[index];
    }

    public boolean hasRequestParamBindings() {
        return requestParamBindings.size() > 0;
    }
}
