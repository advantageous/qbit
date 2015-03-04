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

package io.advantageous.boon.bugs;

import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.boon.json.JsonFactory.toJson;

/**
 * Created by Richard on 4/28/14.
 */
public class BugReport171 {

    public static class TestObject {
        char a;
        Character b;

        public TestObject(char a, Character b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "TestObject{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }
    }


    public static void main(String... args) {
        String json = toJson(new TestObject('a', 'b'));

        puts(json);

        final TestObject testObject = fromJson(json, TestObject.class);

        puts(testObject);

        boolean ok = "{\"a\":\"a\",\"b\":\"b\"}".equals(json) || die();


    }

    @Test
    public void test() {

        BugReport171.main();
    }

}
