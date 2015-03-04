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

import io.advantageous.boon.primitive.Byt;
import io.advantageous.boon.primitive.ByteBuf;
import org.junit.Test;


import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.primitive.Byt.*;
import static org.junit.Assert.*;

public class BytTest {


    @Test
    public void testSliceOf() {
        byte[] array = Byt.array((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6);
        byte[] array2 = Byt.sliceOf(array, 0, 100);
        Byt.equalsOrDie(array, array2);

    }


    @Test
    public void testURLEncodeBytes() {

        ByteBuf buf = ByteBuf.create( 20 );

        buf.addUrlEncodedByteArray( new byte[]{ 0x0, 0x1, 0x2 } );

        final byte[] bytes = buf.readForRecycle();

        boolean ok = true;

        ok |= bytes[ 0 ] == '%' || die();
        ok |= bytes[ 1 ] == '0' || die();
        ok |= bytes[ 2 ] == '0' || die();
        ok |= bytes[ 3 ] == '%' || die();
        ok |= bytes[ 4 ] == '0' || die();
        ok |= bytes[ 5 ] == '1' || die();
        ok |= bytes[ 6 ] == '%' || die();
        ok |= bytes[ 7 ] == '0' || die();
        ok |= bytes[ 8 ] == '2' || die();


    }

    @Test
    public void readUnsignedInt() {
        //0x53, 0x2D, 0x78, 0xAA.
        //http://stackoverflow.com/questions/19874527/conversion-from-bytes-to-large-unsigned-integer-and-string
        ByteBuf buf = ByteBuf.create( 20 );
        buf.addByte( 0xAA );
        buf.addByte( 0x78 );
        buf.addByte( 0x2D );
        buf.addByte( 0x53 );

        byte[] bytes = buf.readForRecycle();

        long val = idxUnsignedInt( bytes, 0 );

        boolean ok = true;

        ok |= val == 2860002643L || die(); //die if not equal to 2860002643L

        buf.add( 2860002643L );

        bytes = buf.readForRecycle();

        val = idxLong( bytes, 0 );

        ok |= val == 2860002643L || die();

        //addObject unsigned int to the byte buffer.
        buf.addUnsignedInt( 2860002643L );

        //read the byte array of the buffer
        bytes = buf.readForRecycle();

        //Read the unsigned int from the array, 2nd arg is offset
        val = idxUnsignedInt( bytes, 0 );

        //Convert it to string and print it to console
        outputs( "" + val );

        ok |= val == 2860002643L || die();

        ok |= ( "" + val ).equals( "2860002643" ) || die();


        //Read the unsigned int from the array, 2nd arg is offset
        byte[] bytes2 = new byte[]{
                ( byte ) 0xAA, 0x78, 0x2D, 0x53, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0 };


        val = idxUnsignedInt( bytes2, 0 );

        ok |= val == 2860002643L || die();


        //Deal direct with bytes
        byte[] bytes3 = new byte[ 20 ];


        unsignedIntTo( bytes3, 0, 2860002643L );

        val = idxUnsignedInt( bytes2, 0 );

        ok |= val == 2860002643L || die();

    }

    private void outputs( String s ) {
    }

    @Test
    public void allocate() {

        byte[] letters =
                arrayOfByte( 500 );

        assertEquals(
                500,
                len( letters )
        );

    }


    @Test
    public void create() {

        byte[] letters =
                array( ( byte ) 0, ( byte ) 1, ( byte ) 2, ( byte ) 3 );

        assertEquals(
                4,
                len( letters )
        );

    }


    @Test
    public void index() {

        byte[] letters =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd' );

        assertEquals(
                'a',
                idx( letters, 0 )
        );


        assertEquals(
                'd',
                idx( letters, -1 )
        );


        assertEquals(
                'd',
                idx( letters, letters.length - 1 )
        );


        idx( letters, 1, ( byte ) 'z' );

        assertEquals(
                ( byte ) 'z',
                idx( letters, 1 )
        );
    }

    @Test
    public void isIn() {

        byte[] letters =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd' );


        assertTrue(
                in( ( byte ) 'a', letters )
        );

        assertFalse(
                in( ( byte ) 'z', letters )
        );

    }


    @Test
    public void isInAtOffset() {

        byte[] letters = { 'a', 'b', 'c', 'd' };

        assertFalse(
                in( 'a', 1, letters )
        );

        assertTrue(
                in( 'c', 1, letters )
        );

    }

    @Test
    public void isInAtRange() {

        byte[] letters = { 'a', 'b', 'c', 'd' };


        assertFalse(
                in( 'a', 1, 2, letters )
        );

        assertTrue(
                in( 'c', 1, 3, letters )
        );

    }

    @Test
    public void slice() {

        byte[] letters =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd' );


        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b' ),
                slc( letters, 0, 2 )
        );

        assertArrayEquals(
                array( ( byte ) 'b', ( byte ) 'c' ),
                slc( letters, 1, -1 )
        );

        //>>> letters[2:]
        //['c', 'd']
        //>>> letters[-2:]
        //['c', 'd']

        assertArrayEquals(
                array( ( byte ) 'c', ( byte ) 'd' ),
                slc( letters, -2 )
        );


        assertArrayEquals(
                array( ( byte ) 'c', ( byte ) 'd' ),
                slc( letters, 2 )
        );


        //>>> letters[:-2]
        //     ['a', 'b']
        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b' ),
                slcEnd( letters, -2 )
        );


        //>>> letters[:-2]
        //     ['a', 'b']
        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b' ),
                slcEnd( letters, 2 )
        );

    }


    @Test
    public void outOfBounds() {

        byte[] fruit =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' );

        slcEnd( fruit, 100 );
        slcEnd( fruit, -100 );

        slc( fruit, 100 );
        slc( fruit, -100 );
        idx( fruit, 100 );
        idx( fruit, -100 );


        idx( fruit, 100, ( byte ) 'x' );


        idx( fruit, -100, ( byte ) 'z' );


        assertEquals(
                ( byte ) 'x',
                idx( fruit, -1 )
        );


        assertEquals(
                ( byte ) 'z',
                idx( fruit, 0 )
        );

    }


    @Test
    public void growTest() {
        byte[] letters =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' );

        letters = grow( letters, 21 );


        assertEquals(
                'e',
                idx( letters, 4 )
        );


        assertEquals(
                'a',
                idx( letters, 0 )
        );


        assertEquals(
                len( letters ),
                26
        );


        assertEquals(
                '\0',
                idx( letters, 20 )
        );


        letters = shrink( letters, 23 );

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c' ),
                letters

        );

    }


    @Test
    public void growFast() {
        byte[] letters =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' );

        letters = grow( letters );


        assertEquals(
                'e',
                idx( letters, 4 )
        );


        assertEquals(
                'a',
                idx( letters, 0 )
        );


        assertEquals(
                len( letters ),
                10
        );


        assertEquals(
                '\0',
                idx( letters, 9 )
        );


    }


    @Test
    public void compactTest() {
        byte[] letters =
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) '\0', ( byte ) '\0',
                        ( byte ) '\0', ( byte ) '\0', ( byte ) '\0', ( byte ) 'c', ( byte ) '\0', ( byte ) 'd', ( byte ) 'e' );

        letters = compact( letters );

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' ),
                letters

        );


    }


    @Test
    public void copyTest() {

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' ),
                copy( array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' ) )

        );


    }

    @Test
    public void addBasic() {

        boolean works = true;

        byte[] bytes = bytes( new byte[]{ 0x01, 0x02, 0x03 } );


        bytes = add( bytes, ( byte ) 0x04 );


        works |=
                bytes[ 3 ] == 0x04 || die( "byte 3 not 0x04" );


        /* Add an int and read it back. */

        bytes = addInt( bytes, 1 );

        works |=
                len( bytes ) == 8 || die( "length should be 8" );


        works |=
                idxInt( bytes, 4 ) == 1 || die( "read int back as 1" );




        /* Write and read in a Long. */
        bytes = addLong( bytes, 0xFFFEFAFBFCL );

        works |=
                len( bytes ) == 16 || die( "length should be 16" );


        works |=
                idxLong( bytes, 8 ) == 0xFFFEFAFBFCL || die( "read int back as  0xFFFEFAFBFCL" );




                /* Write and read in a Short. */
        bytes = addShort( bytes, ( short ) 0x0FED );

        works |=
                len( bytes ) == 18 || die( "length should be 18" );


        works |=
                idxShort( bytes, 16 ) == 0x0FED || die( "read shor back as 0x0FED" );



        /* Write and read in a char. */
        bytes = addChar( bytes, 'a' );

        works |=
                len( bytes ) == 20 || die( "length should be 20" );


        works |=
                idxChar( bytes, 18 ) == 'a' || die( "read char back as 'a'" );




        /* Write and read in a float. */
        bytes = addFloat( bytes, 99.00f );

        works |=
                len( bytes ) == 24 || die( "length should be 24" );


        works |=
                idxFloat( bytes, 20 ) == 99.00f || die( "read float back as 99.00f" );


        System.out.println( "it all worked " + works );


    }


    @Test
    public void addTest() {

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f' ),
                add( array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e' ), ( byte ) 'f' ) );


    }

    @Test
    public void addTestArray() {

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f' ),
                add( array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd' ), array( ( byte ) 'e', ( byte ) 'f' ) )

        );


    }

    void foo( byte a ) {

    }

    @Test
    public void addInsertSingle() {

        byte f = 0;

        foo( f = 0xa );

        foo( f = 'a' );

        foo( f = 1 );


        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f', ( byte ) 'g' ),
                insert( array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f', ( byte ) 'g' ), 2, ( byte ) 'c' )

        );

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f', ( byte ) 'g' ),
                insert( array( ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f', ( byte ) 'g' ), 0, ( byte ) 'a' )

        );

        assertArrayEquals(
                array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'f', ( byte ) 'g' ),
                insert( array( ( byte ) 'a', ( byte ) 'b', ( byte ) 'c', ( byte ) 'd', ( byte ) 'e', ( byte ) 'g' ), 5, ( byte ) 'f' )

        );


    }


    @Test
    public void addInsertEdge() {
        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f', 'g' } ),
                insert( array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f' } ), 6, ( byte ) 'g' )

        );

        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f', 'g' } ),
                insert( array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f' } ), 7, ( byte ) 'g' )

        );

    }

    @Test
    public void addInsertArray() {

        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f', 'g' } ),
                insert( array( new byte[]{ 'a', 'b', 'e', 'f', 'g' } ), 2, array( new byte[]{ 'c', 'd' } ) )

        );

        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f', 'g' } ),
                insert( array( new byte[]{ 'c', 'd', 'e', 'f', 'g' } ), 0, array( new byte[]{ 'a', 'b' } ) )

        );


    }


    @Test
    public void addInsertArrayEnd() {

        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i' } ),
                insert( array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'h', 'i' } ), 5, array( new byte[]{ 'f', 'g' } ) )

        );


    }


    @Test
    public void addInsertArrayEnd2() {


        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'h', 'f', 'g', 'i' } ),
                insert( array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'h', 'i' } ), 6, array( new byte[]{ 'f', 'g' } ) )

        );

    }


    @Test
    public void addInsertArrayEnd3() {


        assertArrayEquals(
                array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'h', 'i', 'f', 'g' } ),
                insert( array( new byte[]{ 'a', 'b', 'c', 'd', 'e', 'h', 'i' } ), 7, array( new byte[]{ 'f', 'g' } ) )

        );

    }


}
