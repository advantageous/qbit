/*
 * Copyright 2013-2014 Richard M. Hightower
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
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.com.examples;
import io.advantageous.boon.IO;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;

import java.util.Map;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by Richard on 5/12/14.
 */
public class EvilJsonParse {
    public static class Employee {String firstName; String lastName; int salary; }
    public static void main (String... args) {
        long start;
        long stop;
        long time;


        final String json = IO.read("/Users/Richard/dos_100000.json"); //talking IO time out of the equation
        final JsonParserAndMapper mapper = new JsonParserFactory().create();

        start = System.nanoTime();

        final Map<String,Object> map = mapper.parseMap(json);

        map.size();



        stop = System.nanoTime();
        time = (stop - start) / 1_000_000; //milli-seconds
        puts ("time", time, "ms");

    }
}
