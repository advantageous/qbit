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

package io.advantageous.boon.cache;

import io.advantageous.boon.Lists;
import io.advantageous.boon.collections.SortableConcurrentList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

/**
 * Created by rick on 12/16/13.
 */
public class SortableConcurrentListTest {

    private SortableConcurrentList list;

    @Before
    public void before() {

        list = new SortableConcurrentList();
    }

    @After
    public void after() {

    }


    @Test
    public void test() {
        list.add( 9 );
        list.add( 66 );
        list.add( 7 );
        list.add( 55 );
        list.add( 5 );
        list.add( 33 );
        list.add( 3 );
        list.add( 2 );
        list.add( 1 );
        list.add( 0 );
        list.sort();
        boolean ok = Lists.list( 0, 1, 2, 3, 5, 7, 9, 33, 55, 66 ).equals( list ) || die();
        final List purgeList = list.sortAndReturnPurgeList( 0.20f );
        ok |= Lists.list( 0, 1 ).equals( purgeList ) || die();
        ok |= Lists.list( 2, 3, 5, 7, 9, 33, 55, 66 ).equals( list ) || die();
        puts( "test", ok );

    }

}
