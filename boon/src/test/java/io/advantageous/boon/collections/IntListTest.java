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

import io.advantageous.boon.collections.IntList;
import junit.framework.TestCase;
import io.advantageous.boon.primitive.Int;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 2/18/14.
 */
public class IntListTest extends TestCase {
    @Test
    public void testMath() {
        IntList list = new IntList();
        list.addInt(1);
        list.addInt(2);
        list.addInt(3);

        Int.equalsOrDie(3, list.max());
        Int.equalsOrDie(1, list.min());
        Int.equalsOrDie(2, list.median());
        Int.equalsOrDie(2, list.mean());
        Int.equalsOrDie(1, list.standardDeviation());

        for (int index = 0; index< 15; index++) {
            list.addInt(3);
        }


        Int.equalsOrDie(3, list.max());
        Int.equalsOrDie(1, list.min());
        Int.equalsOrDie(3, list.median());
        Int.equalsOrDie(3, list.mean());
        Int.equalsOrDie(0, list.standardDeviation());

        for (int index = 0; index< 20; index++) {
            list.addInt(2);
        }

        Int.equalsOrDie(3, list.max());
        Int.equalsOrDie(1, list.min());
        Int.equalsOrDie(2, list.median());
        Int.equalsOrDie(2, list.mean());
        Int.equalsOrDie(1, list.standardDeviation());

        list.addInt(10);

        list.addInt(-10);


        Int.equalsOrDie(10, list.max());
        Int.equalsOrDie(-10, list.min());
        Int.equalsOrDie(2, list.median());
        Int.equalsOrDie(2, list.mean());
        Int.equalsOrDie(2, list.standardDeviation());


        list.addInt(100);

        list.addInt(-100);


        Int.equalsOrDie(100, list.max());
        Int.equalsOrDie(-100, list.min());
        Int.equalsOrDie(2, list.median());
        Int.equalsOrDie(2, list.mean());
        Int.equalsOrDie(22, list.standardDeviation());
        Int.equalsOrDie(42, list.size());



        for (int index = 0; index< 4000; index++) {
            list.addInt(2);
        }


        Int.equalsOrDie(100, list.max());
        Int.equalsOrDie(-100, list.min());
        Int.equalsOrDie(2, list.median());
        Int.equalsOrDie(2, list.mean());
        Int.equalsOrDie(2, list.standardDeviation());
        Int.equalsOrDie(4042, list.size());


    }


    @Test
    public void test() {
        IntList list = new IntList();
        list.addInt(1);
        list.addInt(2);
        list.addInt(3);

        Int.equalsOrDie(3, list.max());
        Int.equalsOrDie(1, list.min());
        Int.equalsOrDie(2, list.median());
        Int.equalsOrDie(2, list.mean());
        Int.equalsOrDie(1, list.standardDeviation());


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
