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

import io.advantageous.boon.cache.CacheType;
import io.advantageous.boon.cache.FastConcurrentReadLruLfuFifoCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by rick on 12/17/13.
 */
public class CacheTest {

    private FastConcurrentReadLruLfuFifoCache<Integer, Integer> lruCache;

    @Before
    public void setUp() throws Exception {
        lruCache = new FastConcurrentReadLruLfuFifoCache<>( 10 );

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void fifo() throws Exception {

        lruCache = new FastConcurrentReadLruLfuFifoCache<>( true, 10, CacheType.FIFO );

        lruCache.put( 0, 10 );
        lruCache.put( 1, 10 );
        lruCache.put( 2, 20 );
        lruCache.put( 3, 30 );
        lruCache.put( 4, 40 );
        lruCache.put( 5, 50 );
        lruCache.put( 6, 60 );
        lruCache.put( 7, 70 );
        lruCache.put( 8, 80 );
        lruCache.put( 9, 90 );
        lruCache.put( 10, 10 );
        lruCache.put( 11, 10 );
        lruCache.put( 12, 10 );
        lruCache.put( 13, 10 );
        lruCache.put( 14, 10 );

        boolean ok = lruCache.get( 12 ) != null || die();
        ok |= lruCache.get( 0 ) == null || die();

        lruCache.put( 15, 10 );
        ok = lruCache.get( 12 ) != null || die();
        ok |= lruCache.get( 1 ) == null || die();

        lruCache.put( 16, 10 );
        ok = lruCache.get( 12 ) != null || die();
        ok |= lruCache.get( 2 ) == null || die();


        outputs( "fifo", lruCache );

    }

    private void outputs( Object... args ) {
    }

    @Test
    public void test() throws Exception {


        lruCache = new FastConcurrentReadLruLfuFifoCache<>( true, 10, CacheType.LFU );


        lruCache.put( -1, 10 );

        lruCache.put( -2, 10 );

        lruCache.put( 0, 10 );
        lruCache.put( 1, 10 );
        lruCache.get( 1 );
        lruCache.get( 1 );
        lruCache.get( 1 );


        lruCache.put( 2, 20 );
        lruCache.put( 3, 30 );
        lruCache.get( 3 );

        lruCache.put( 4, 40 );
        lruCache.get( 4 );

        lruCache.put( 5, 50 );
        lruCache.get( 5 );

        lruCache.put( 6, 60 );
        lruCache.get( 6 );

        outputs( lruCache );
        lruCache.put( 7, 70 );


        lruCache.put( 8, 80 );
        lruCache.put( 9, 90 );

        lruCache.put( 98, 60 );


        outputs( "here is what is there", lruCache.size(), lruCache );
        boolean ok = lruCache.get( -1 ) == null || die();
        ok |= lruCache.getSilent( 2 ) == 20 || die();
        ok |= lruCache.getSilent( 9 ) == 90 || die();


        lruCache.put( 11, 10 );
        lruCache.put( 12, 20 );
        lruCache.put( 13, 30 );
        lruCache.put( 14, 40 );
        lruCache.put( 15, 50 );
        lruCache.put( 16, 60 );
        lruCache.put( 21, 10 );

        ok |= lruCache.get( 2 ) == null || die();
        ok |= lruCache.get( 3 ) == 30 || die();
        ok |= lruCache.get( 4 ) == 40 || die();
        ok |= lruCache.get( 5 ) == 50 || die();
        ok |= lruCache.get( 6 ) == 60 || die();
        ok |= lruCache.getSilent( 7 ) == null || die();


        lruCache.put( 22, 20 );
        lruCache.put( 23, 30 );
        lruCache.put( 24, 40 );
        lruCache.put( 25, 50 );
        lruCache.put( 26, 60 );

        ok |= lruCache.get( 2 ) == null || die();
        ok |= lruCache.get( 3 ) == 30 || die();
        ok |= lruCache.get( 4 ) == 40 || die();
        ok |= lruCache.get( 5 ) == 50 || die();
        ok |= lruCache.get( 6 ) == 60 || die();


        for ( int index = 400; index < 500; index++ ) {
            lruCache.put( index, index );
        }


        ok |= lruCache.get( 11 ) == null || die();
        ok |= lruCache.get( 12 ) == null || die();
        ok |= lruCache.get( 13 ) == null || die();
        ok |= lruCache.get( 14 ) == null || die();

        ok |= lruCache.get( 7 ) == null || die();
        ok |= lruCache.get( 2 ) == null || die();
        ok |= lruCache.get( 3 ) == 30 || die();
        ok |= lruCache.get( 4 ) == 40 || die();
        ok |= lruCache.get( 5 ) == 50 || die();
        ok |= lruCache.get( 6 ) == 60 || die();

        outputs( lruCache );

    }

}
