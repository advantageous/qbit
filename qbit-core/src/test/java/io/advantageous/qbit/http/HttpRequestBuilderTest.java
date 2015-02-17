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

package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class HttpRequestBuilderTest {

    boolean ok;

    @Test
    public void test() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.addParam("user", "rick");
        requestBuilder.addParam("password", "duck soup");


        final HttpRequest request = requestBuilder.buildClientRequest();


        ok = "foo/bar/baz?password=duck+soup&user=rick".equals(request.getUri())
                || die();


        ok = "GET".equals(request.getMethod())
                || die();


        ok = request.getBody().length == 0 || die();

        requestBuilder.setId(9);
        requestBuilder.setTimestamp(10);
        requestBuilder.setRemoteAddress("remote");

        final HttpRequest request1 = requestBuilder.build();


        final HttpRequest request2 = requestBuilder.build();


        ok = request1.hashCode() == request2.hashCode() || die();
        ok = request1.equals(request2) || die();
        ok = request.getBody().equals(request1.getBody()) || die();
        ok = request.getBodyAsString().equals(request1.getBodyAsString()) || die();
        ok = request.getContentType().equals(request1.getContentType()) || die(request.getContentType());
        ok = request.getMethod().equals(request1.getMethod()) || die();
        ok = request2.getMessageId() == request1.getMessageId() || die();
        ok = request2.getRemoteAddress().equals(request1.getRemoteAddress()) || die();

        puts(request);

        request.isJson();

    }


    @Test
    public void testFormPost() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.addParam("user", "rick");
        requestBuilder.addParam("password", "duck soup");
        requestBuilder.setMethod("POST");


        final HttpRequest request = requestBuilder.buildClientRequest();


        ok = "password=duck+soup&user=rick".equals(request.getBodyAsString())
                || die(request.getBodyAsString());


        ok = "POST".equals(request.getMethod())
                || die();


        ok = "foo/bar/baz".equals(request.getUri())
                || die();


    }


    @Test
    public void testFormJsonPost() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.setJsonBodyForPost("\"hi\"");

        final HttpRequest request = requestBuilder.build();


        ok = "\"hi\"".equals(request.getBodyAsString())
                || die(request.getBodyAsString());


        ok = "POST".equals(request.getMethod())
                || die();


        ok = "foo/bar/baz".equals(request.getUri())
                || die();


    }
}