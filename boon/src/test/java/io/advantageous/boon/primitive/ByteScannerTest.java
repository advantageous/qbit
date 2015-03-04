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


import io.advantageous.boon.primitive.ByteScanner;
import org.junit.Test;

import static io.advantageous.boon.primitive.Byt.bytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ByteScannerTest {

    @Test
    public void autoSplitThisEndsInSpace() {

        byte[] letters =
                bytes( "This is a string " );


        byte[][] splitted = ByteScanner.split(letters, ' ');


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }

    @Test
    public void autoSplitThis() {

        byte[] letters =
                bytes( "This is a string" );


        byte[][] splitted = ByteScanner.split( letters, ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void autoSplitThisStartSpace() {

        byte[] letters =
                bytes( " This is a string" );


        byte[][] splitted = ByteScanner.split( letters, ' ' );


        assertEquals(
                5,
                splitted.length
        );


        assertEquals(
                0,
                splitted[ 0 ].length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 3 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "" ), bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void autoSplitThisByTabOrSpace() {

        byte[] letters =
                bytes( "This\tis a string" );


        byte[][] splitted = ByteScanner.splitByChars( letters, '\t', ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void autoSplitThis3DoubleSpaceAfterA() {

        byte[] letters =
                bytes( "This is a  string" );


        byte[][] splitted = ByteScanner.split( letters, ' ' );


        assertEquals(
                5,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );

        assertEquals(
                0,
                splitted[ 3 ].length
        );

        assertArrayEquals(
                bytes( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThisEndsInSpace() {

        byte[] letters =
                bytes( "This is a string " );


        byte[][] splitted = ByteScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }

    @Test
    public void splitThis() {

        byte[] letters =
                bytes( "This is a string" );


        byte[][] splitted = ByteScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThisStartSpace() {

        byte[] letters =
                bytes( " This is a string" );


        byte[][] splitted = ByteScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                5,
                splitted.length
        );


        assertEquals(
                0,
                splitted[ 0 ].length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 3 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "" ), bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThisByTabOrSpace() {

        byte[] letters =
                bytes( "This\tis a string" );


        byte[][] splitted = ByteScanner.splitExact( letters, 10, '\t', ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                bytes( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThis3DoubleSpaceAfterA() {

        byte[] letters =
                bytes( "This is a  string" );


        byte[][] splitted = ByteScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                5,
                splitted.length
        );

        assertArrayEquals(
                bytes( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                bytes( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                bytes( "a" ),
                splitted[ 2 ]
        );

        assertEquals(
                0,
                splitted[ 3 ].length
        );

        assertArrayEquals(
                bytes( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new byte[][]{ bytes( "This" ), bytes( "is" ), bytes( "a" ), bytes( "" ), bytes( "string" ) },
                splitted
        );


    }


}
