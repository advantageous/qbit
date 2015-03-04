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

import io.advantageous.boon.collections.DoubleList;
import io.advantageous.boon.primitive.Dbl;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 3/19/14.
 */
public class DoubleListTest {
    @Test
    public void testMath() {
        DoubleList list = new DoubleList();
        list.add(1);
        list.add(2);
        list.add(3);

        Dbl.equalsOrDie(3, list.max());
        Dbl.equalsOrDie(1, list.min());
        Dbl.equalsOrDie(2, list.median());
        Dbl.equalsOrDie(2, list.mean());
        Dbl.equalsOrDie(0.816496580927726, list.standardDeviation());

        for (int index = 0; index< 15; index++) {
            list.add(3);
        }


        Dbl.equalsOrDie(3, list.max());
        Dbl.equalsOrDie(1, list.min());
        Dbl.equalsOrDie(3, list.median());
        Dbl.equalsOrDie(2.8333333333333335, list.mean());
        Dbl.equalsOrDie(0.49999999999999994, list.standardDeviation());

        for (int index = 0; index< 20; index++) {
            list.addFloat(2);
        }

        Dbl.equalsOrDie(3, list.max());
        Dbl.equalsOrDie(1, list.min());
        Dbl.equalsOrDie(2, list.median());
        Dbl.equalsOrDie(2.3947368421052633, list.mean());
        Dbl.equalsOrDie(0.5399548560179785, list.standardDeviation());

        list.addFloat(10);

        list.addFloat(-10);


        Dbl.equalsOrDie(10, list.max());
        Dbl.equalsOrDie(-10, list.min());
        Dbl.equalsOrDie(2, list.median());
        Dbl.equalsOrDie(2.275, list.mean());
        Dbl.equalsOrDie(2.355711145280762, list.standardDeviation());


        list.addFloat(100);

        list.addFloat(-100);


        Dbl.equalsOrDie(100, list.max());
        Dbl.equalsOrDie(-100, list.min());
        Dbl.equalsOrDie(2, list.median());
        Dbl.equalsOrDie(2.1666666666666665, list.mean());
        Dbl.equalsOrDie(21.947900069489965, list.standardDeviation());
        Dbl.equalsOrDie(42, list.size());



        for (int index = 0; index< 4000; index++) {
            list.addFloat(2);
        }


        Dbl.equalsOrDie(100, list.max());
        Dbl.equalsOrDie(-100, list.min());
        Dbl.equalsOrDie(2, list.median());
        Dbl.equalsOrDie(2.0017318159327067, list.mean());
        Dbl.equalsOrDie(2.2373393245468116, list.standardDeviation());
        Dbl.equalsOrDie(4042, list.size());


    }


    @Test
    public void test() {
        DoubleList list = new DoubleList();
        list.addFloat(1);
        list.addFloat(2);
        list.addFloat(3);

        Dbl.equalsOrDie(3, list.max());
        Dbl.equalsOrDie(1, list.min());
        Dbl.equalsOrDie(2, list.median());
        Dbl.equalsOrDie(2, list.mean());
        Dbl.equalsOrDie(0.816496580927726, list.standardDeviation());


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

