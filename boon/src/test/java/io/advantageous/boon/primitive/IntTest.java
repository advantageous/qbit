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

package io.advantageous.boon.primitive;

import org.junit.Test;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.primitive.Int.reduceBy;

/**
 * Created by Richard on 3/15/14.
 */
public class IntTest {

    public long reduce(long s, int b) {return s+b;}

    public long sum(long s, int b) {return s+b;}

    boolean ok;


    @Test
    public void testSliceOf() {
        int[] array = Int.array(0, 1, 2, 3, 4, 5, 6);
        int[] ints = Int.sliceOf(array, 0, 100);
        Int.equalsOrDie(array, ints);

    }

    @Test
    public void test() {



        long sum =  reduceBy(new int[]{1,2,3,4,5,6,7,8}, this);


        if (sum != 36)
            die(sputs("Sum was 36", sum));


    }


    @Test
    public void testNewMethod() {



        long sum =  reduceBy(new int[]{1,2,3,4,5,6,7,8}, this, "sum");


        if (sum != 36) {
            die(sputs("Sum was 36", sum));
        }

    }
}
