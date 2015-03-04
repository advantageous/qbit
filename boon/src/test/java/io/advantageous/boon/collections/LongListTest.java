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

package io.advantageous.boon.collections;

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

import io.advantageous.boon.collections.LongList;
import junit.framework.TestCase;
import io.advantageous.boon.primitive.Lng;

import static io.advantageous.boon.primitive.Lng.equalsOrDie;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 2/18/14.
 */
public class LongListTest extends TestCase {
    @Test
    public void testMath() {
        LongList list = new LongList();
        list.addLong(1);
        list.addLong(2);
        list.addLong(3);

        Lng.equalsOrDie(3L, list.max());
        Lng.equalsOrDie(1L, list.min());
        Lng.equalsOrDie(2L, list.median());
        Lng.equalsOrDie(2L, list.mean());
        Lng.equalsOrDie(1L, list.standardDeviation());

        for (int index = 0; index< 15; index++) {
            list.addLong(3);
        }


        Lng.equalsOrDie(3, list.max());
        Lng.equalsOrDie(1, list.min());
        Lng.equalsOrDie(3, list.median());
        Lng.equalsOrDie(3, list.mean());
        Lng.equalsOrDie(0, list.standardDeviation());

        for (int index = 0; index< 20; index++) {
            list.addLong(2);
        }

        Lng.equalsOrDie(3, list.max());
        Lng.equalsOrDie(1, list.min());
        Lng.equalsOrDie(2, list.median());
        Lng.equalsOrDie(2, list.mean());
        Lng.equalsOrDie(1, list.standardDeviation());

        list.addLong(10);

        list.addLong(-10);


        equalsOrDie(10, list.max());
        equalsOrDie(-10, list.min());
        equalsOrDie(2, list.median());
        equalsOrDie(2, list.mean());
        equalsOrDie(2, list.standardDeviation());


        list.addLong(100);

        list.addLong(-100);


        equalsOrDie(100, list.max());
        equalsOrDie(-100, list.min());
        equalsOrDie(2, list.median());
        equalsOrDie(2, list.mean());
        equalsOrDie(22, list.standardDeviation());
        equalsOrDie(42, list.size());



        for (int index = 0; index< 4000; index++) {
            list.addLong(2);
        }


        equalsOrDie(100, list.max());
        equalsOrDie(-100, list.min());
        equalsOrDie(2, list.median());
        equalsOrDie(2, list.mean());
        equalsOrDie(2, list.standardDeviation());
        equalsOrDie(4042, list.size());


    }


    @Test
    public void test() {
        LongList list = new LongList();
        list.addLong(1);
        list.addLong(2);
        list.addLong(3);

        equalsOrDie(3, list.max());
        equalsOrDie(1, list.min());
        equalsOrDie(2, list.median());
        equalsOrDie(2, list.mean());
        equalsOrDie(1, list.standardDeviation());


        if (list.getInt(0) != 1) die();


        if (list.getInt(1) != 2) die();


        if (list.getInt(2) != 3) die();


        if (list.size() != 3) die();


        if (list.sum() != 6) die();


        list.addArray(2, 3, 5);


        if (list.sum() != 16) die();

        if (list.size() != 6) die();


    }


}
