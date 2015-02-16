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

package io.advantageous.qbit.vertx;

import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class BufferUtilsTest {

    boolean ok;

    @Test
    public void testWriteString() throws Exception {

        Buffer buffer = new Buffer();
        BufferUtils.writeString(buffer, "hi mom");

        final short size = buffer.getShort(0);
        ok = size == 6 || die();

        final String utf_8 = buffer.getString(2, size + 2, StandardCharsets.UTF_8.displayName());

        puts(utf_8);

        ok = utf_8.equals("hi mom") || die();

    }

    @Test
    public void testReadString() throws Exception {

        Buffer buffer = new Buffer();
        BufferUtils.writeString(buffer, "hi mom");

        int[] location = new int[]{0};

        String backOut = BufferUtils.readString(buffer, location);


        ok = backOut.equals("hi mom") || die();

    }


    @Test
    public void testWriteMap() throws Exception {

        Buffer buffer = new Buffer();

        MultiMap<String, String> map = new MultiMapImpl<>();

        map.put("key", "value");

        BufferUtils.writeMap(buffer, map);


        int[] location = new int[]{0};


        MultiMap<String, String> map2 = BufferUtils.readMap(buffer, location);

        final String value = map2.get("key");

        ok = value.equals("value") || die();


    }

    @Test
    public void testWriteMapTwoValues() throws Exception {

        Buffer buffer = new Buffer();

        MultiMap<String, String> map = new MultiMapImpl<>();

        map.add("key", "value0");

        map.add("key", "value1");

        BufferUtils.writeMap(buffer, map);


        BufferUtils.writeString(buffer, "body");


        int[] location = new int[]{0};


        MultiMap<String, String> map2 = BufferUtils.readMap(buffer, location);

        final String value = map2.get("key");

        ok = value.equals("value0") || die();

        final Collection<String> values = (Collection<String>) map2.getAll("key");

        ok = values.contains("value1") || die();


        String body = BufferUtils.readString(buffer, location);

        ok = body.equals("body") || die();


    }
}