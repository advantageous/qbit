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

package io.advantageous.com.examples.benchmarkmodel;

import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 5/10/14.
 */
public class SmokeTestForBenchMark {


    final static JsonSerializer serializer = new JsonSerializerFactory().useFieldsOnly().setSerializeAsSupport(false).create();
    final static JsonParserAndMapper parser = new JsonParserFactory().create();

    public static void main(String... args) {

        StringPerformance sp = new StringPerformance(1);

        final String json = serializer.serialize(sp).toString();
        puts (json);

        StringPerformance sp2 = parser.parse(StringPerformance.class, json);

        boolean ok = sp.equals(sp2) || die();

    }
}
