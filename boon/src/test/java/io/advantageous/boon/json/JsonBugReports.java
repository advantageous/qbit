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

package io.advantageous.boon.json;


import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonParserFactory;
import org.junit.Test;

import java.util.Map;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.boon.json.JsonFactory.toJson;

public class JsonBugReports {


    @Test
    public void testForIssue47() {
        Map<String, Object> map = (Map<String, Object>) fromJson("{\"empty\":\"\",\"docId\":111,\"serviceName\":\"cafe\"}");
        puts (map);
        puts (toJson(map));

        boolean ok = toJson(map).equals("{\"docId\":111,\"empty\":\"\",\"serviceName\":\"cafe\"}") ||
                die(toJson(map) );
    }


    @Test
    public void testForBug202() {
        String test = "{  \n" +
                "   \"timestamp\":1405673028,\n" +
                "   \"status\":200,\n" +
                "   \"request\":{  \n" +
                "      \"mbean\":\"com.openexchange.pooling:name=ConfigDB Read\",\n" +
                "      \"attribute\":\"MaxUseTime\",\n" +
                "      \"type\":\"read\"\n" +
                "   },\n" +
                "   \"value\":24\n" +
                "}";

        Map map = (Map) fromJson(test);
    }


    @Test
    public void testForBug202_1() {
        String test = "{  \n" +
                "   \"timestamp\":1405673028,\n" +
                "   \"status\":200,\n" +
                "   \"request\":{  \n" +
                "      \"mbean\":\"com.openexchange.pooling:name=ConfigDB Read\",\n" +
                "      \"attribute\":\"MaxUseTime\",\n" +
                "      \"type\":\"read\"\n" +
                "   },\n" +
                "   \"value\":24\n" +
                "}";


        Map map = (Map) JsonFactory.create().fromJson(test);
    }



    @Test
    public void testForBug202_2() {
        String test = "{  \n" +
                "   \"timestamp\":1405673028,\n" +
                "   \"status\":200,\n" +
                "   \"request\":{  \n" +
                "      \"mbean\":\"com.openexchange.pooling:name=ConfigDB Read\",\n" +
                "      \"attribute\":\"MaxUseTime\",\n" +
                "      \"type\":\"read\"\n" +
                "   },\n" +
                "   \"value\":24\n" +
                "}";


        Map map = (Map) JsonFactory.create().fromJson(test);
    }


    @Test
    public void testForBug202_3() {
        String test = "{  \n" +
                "   \"timestamp\":1405673028,\n" +
                "   \"status\":200,\n" +
                "   \"request\":{  \n" +
                "      \"mbean\":\"com.openexchange.pooling:name=ConfigDB Read\",\n" +
                "      \"attribute\":\"MaxUseTime\",\n" +
                "      \"type\":\"read\"\n" +
                "   },\n" +
                "   \"value\":24\n" +
                "}";


        final JsonParserFactory jsonParserFactory = new JsonParserFactory();
        final Map<String, Object> stringObjectMap = jsonParserFactory.createFastParser().parseMap(test);
     }

    @Test
    public void testForBug202_4() {
        String test = "{  \n" +
                "   \"timestamp\":1405673028,\n" +
                "   \"status\":200,\n" +
                "   \"request\":{  \n" +
                "      \"mbean\":\"com.openexchange.pooling:name=ConfigDB Read\",\n" +
                "      \"attribute\":\"MaxUseTime\",\n" +
                "      \"type\":\"read\"\n" +
                "   },\n" +
                "   \"value\":24\n" +
                "}";


        final JsonParserFactory jsonParserFactory = new JsonParserFactory();
        final Map<String, Object> stringObjectMap =
                jsonParserFactory.createJsonCharArrayParser().parseMap(test);

    }



}
