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

import io.advantageous.boon.collections.FloatList;
import io.advantageous.boon.primitive.Flt;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 3/19/14.
 */
public class FloatListTest {
     @Test
    public void testMath() {
        FloatList list = new FloatList();
        list.add(1);
        list.add(2);
        list.add(3);

        Flt.equalsOrDie(3, list.max());
        Flt.equalsOrDie(1, list.min());
        Flt.equalsOrDie(2, list.median());
        Flt.equalsOrDie(2, list.mean());
        Flt.equalsOrDie(0.8164966f, list.standardDeviation());

        for (int index = 0; index< 15; index++) {
            list.add(3);
        }


        Flt.equalsOrDie(3, list.max());
        Flt.equalsOrDie(1, list.min());
        Flt.equalsOrDie(3, list.median());
        Flt.equalsOrDie(2.8333333f, list.mean());
        Flt.equalsOrDie(0.5f, list.standardDeviation());

        for (int index = 0; index< 20; index++) {
            list.addFloat(2);
        }

        Flt.equalsOrDie(3, list.max());
        Flt.equalsOrDie(1, list.min());
        Flt.equalsOrDie(2, list.median());
        Flt.equalsOrDie(2.3947368f, list.mean());
        Flt.equalsOrDie(0.53995484f, list.standardDeviation());

        list.addFloat(10);

        list.addFloat(-10);


        Flt.equalsOrDie(10, list.max());
        Flt.equalsOrDie(-10, list.min());
        Flt.equalsOrDie(2, list.median());
        Flt.equalsOrDie(2.275f, list.mean());
        Flt.equalsOrDie(2.3557112f, list.standardDeviation());


        list.addFloat(100);

        list.addFloat(-100);


        Flt.equalsOrDie(100, list.max());
        Flt.equalsOrDie(-100, list.min());
        Flt.equalsOrDie(2, list.median());
        Flt.equalsOrDie(2.1666667f, list.mean());
        Flt.equalsOrDie(21.9479f, list.standardDeviation());
        Flt.equalsOrDie(42, list.size());



        for (int index = 0; index< 4000; index++) {
            list.addFloat(2);
        }


        Flt.equalsOrDie(100, list.max());
        Flt.equalsOrDie(-100, list.min());
        Flt.equalsOrDie(2, list.median());
        Flt.equalsOrDie(2.0017319f, list.mean());
        Flt.equalsOrDie(2.2373393f, list.standardDeviation());
        Flt.equalsOrDie(4042, list.size());


    }


    @Test
    public void test() {
        FloatList list = new FloatList();
        list.addFloat(1);
        list.addFloat(2);
        list.addFloat(3);

        Flt.equalsOrDie(3, list.max());
        Flt.equalsOrDie(1, list.min());
        Flt.equalsOrDie(2, list.median());
        Flt.equalsOrDie(2, list.mean());
        Flt.equalsOrDie(0.8164966f, list.standardDeviation());


        if (list.getFloat(0) != 1) die();


        if (list.getFloat(1) != 2) die();


        if (list.getFloat(2) != 3) die();


        if (list.size() != 3) die();


        if (list.sum() != 6) die();


        list.addArray(2, 3, 5);


        if (list.sum() != 16) die();

        if (list.size() != 6) die();


    }


}

