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

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.primitive.ByteBuf;
import io.advantageous.boon.primitive.Input;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static junit.framework.Assert.assertTrue;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.primitive.Byt.*;
import static io.advantageous.boon.primitive.Shrt.*;
import static io.advantageous.boon.primitive.Int.*;
import static io.advantageous.boon.primitive.Lng.*;
import static io.advantageous.boon.primitive.Flt.*;
import static io.advantageous.boon.primitive.Dbl.*;

import static org.junit.Assert.assertEquals;

public class BytBufTest {


    @Test
    public void test4() {
        ByteBuf buf = new ByteBuf();
        buf.writeUnsignedShort( Short.MAX_VALUE + 1 );
        buf.writeUnsignedInt( ( ( long ) Integer.MAX_VALUE ) + 1L );
        buf.writeUnsignedByte( ( short ) ( Byte.MAX_VALUE + 1 ) );
        buf.writeInt( 1 );
        buf.writeBoolean( true );
        buf.writeBoolean( false );
        buf.writeFloat( 1.0f );
        buf.writeDouble( 1.0d );
        buf.writeChar( 'a' );
        buf.writeLong( 1L );
        buf.writeSmallString( "hi mom" );
        buf.writeMediumString( "hi mom" );
        buf.writeLargeString( "AAA" );
        buf.writeLargeString( "BBB" );
        buf.writeByte( ( byte ) 7 );
        buf.writeShort( ( short ) 11 );
        buf.writeSmallByteArray( new byte[]{ 1, 2, 3 } );
        buf.writeMediumByteArray( new byte[]{ 1, 2, 3 } );
        buf.writeLargeByteArray( new byte[]{ 1, 2, 3 } );
        buf.writeSmallShortArray( new short[]{ 1, 2, 3 } );
        buf.writeSmallShortArray( new short[]{ 1, 2, 3 } );
        buf.writeLargeShortArray( new short[]{ 1, 2, 3 } );
        buf.writeMediumShortArray( new short[]{ 2, 2, 2 } );
        buf.writeSmallIntArray( new int[]{ 1, 2, 3 } );
        buf.writeLargeIntArray( new int[]{ 1, 2, 3 } );
        buf.writeMediumIntArray( new int[]{ 2, 2, 2 } );
        buf.writeSmallLongArray( new long[]{ 1, 2, 3 } );
        buf.writeLargeLongArray( new long[]{ 1, 2, 3 } );
        buf.writeMediumLongArray( new long[]{ 2, 2, 2 } );
        buf.writeSmallFloatArray( new float[]{ 1.0f, 2.0f, 3.0f } );
        buf.writeLargeFloatArray( new float[]{ 1.0f, 2.0f, 3.0f } );
        buf.writeMediumFloatArray( new float[]{ 2.0f, 2.0f, 2.0f } );

        buf.writeSmallDoubleArray( new double[]{ 1.0, 2.0, 3.0 } );
        buf.writeLargeDoubleArray( new double[]{ 1.0, 2.0, 3.0 } );
        buf.writeMediumDoubleArray( new double[]{ 2.0, 2.0, 2.0 } );

        boolean ok = true;

        final Input input = buf.input();

        int unsignedShort = input.readUnsignedShort();
        ok |= unsignedShort == Short.MAX_VALUE + 1 || die( "" + unsignedShort );

        long unsignedInt = input.readUnsignedInt();
        ok |= unsignedInt == ( ( long ) Integer.MAX_VALUE ) + 1L || die( "" + unsignedInt );

        short unsignedByte = input.readUnsignedByte();
        ok |= unsignedByte == Byte.MAX_VALUE + 1 || die( "" + unsignedByte );

        int myint = input.readInt();
        ok |= myint == 1 || die( "" + myint );

        boolean btrue = input.readBoolean();
        ok |= btrue || die( "" + btrue );

        boolean bfalse = input.readBoolean();
        ok |= !bfalse || die( "" + bfalse );

        float myfloat = input.readFloat();
        ok |= myfloat == 1 || die( "" + myfloat );

        double mydouble = input.readDouble();
        ok |= mydouble == 1 || die( "" + mydouble );

        char mychar = input.readChar();
        ok |= mychar == 'a' || die( "" + mychar );

        long mylong = input.readLong();
        ok |= mylong == 1L || die( "" + mylong );

        String str = input.readSmallString();
        ok |= str.equals( "hi mom" ) || die( str );


        str = input.readMediumString();
        ok |= str.equals( "hi mom" ) || die( str );


        str = input.readLargeString();
        ok |= str.equals( "AAA" ) || die( str );

        str = input.readLargeString();
        ok |= str.equals( "BBB" ) || die( str );


        short byt = input.readByte();
        ok |= byt == 7 || die( "" + byt );

        short shrt = input.readShort();
        ok |= shrt == 11 || die( "" + shrt );


        byte[] bytes = null;

        bytes = input.readSmallByteArray();
        ok |= bytes.length == 3 || die( "" + bytes.length );
        ok |= idx( bytes, 0 ) == 1 || die( "" + idx( bytes, 0 ) );
        ok |= idx( bytes, 1 ) == 2 || die( "" + idx( bytes, 1 ) );
        ok |= idx( bytes, 2 ) == 3 || die( "" + idx( bytes, 2 ) );

        bytes = input.readMediumByteArray();
        ok |= bytes.length == 3 || die( "" + bytes.length );
        ok |= idx( bytes, 0 ) == 1 || die( "" + idx( bytes, 0 ) );
        ok |= idx( bytes, 1 ) == 2 || die( "" + idx( bytes, 1 ) );
        ok |= idx( bytes, 2 ) == 3 || die( "" + idx( bytes, 2 ) );

        bytes = input.readLargeByteArray();
        ok |= bytes.length == 3 || die( "" + bytes.length );
        ok |= idx( bytes, 0 ) == 1 || die( "" + idx( bytes, 0 ) );
        ok |= idx( bytes, 1 ) == 2 || die( "" + idx( bytes, 1 ) );
        ok |= idx( bytes, 2 ) == 3 || die( "" + idx( bytes, 2 ) );


        short[] shorts = null;

        shorts = input.readSmallShortArray();
        ok |= shorts.length == 3 || die( "" + shorts.length );
        ok |= idx( shorts, 0 ) == 1 || die( "" + idx( shorts, 0 ) );
        ok |= idx( shorts, 1 ) == 2 || die( "" + idx( shorts, 1 ) );
        ok |= idx( shorts, 2 ) == 3 || die( "" + idx( shorts, 2 ) );

        shorts = input.readSmallShortArray();
        ok |= shorts.length == 3 || die( "" + shorts.length );
        ok |= idx( shorts, 0 ) == 1 || die( "" + idx( shorts, 0 ) );
        ok |= idx( shorts, 1 ) == 2 || die( "" + idx( shorts, 1 ) );
        ok |= idx( shorts, 2 ) == 3 || die( "" + idx( shorts, 2 ) );

        shorts = input.readLargeShortArray();
        ok |= shorts.length == 3 || die( "" + shorts.length );
        ok |= idx( shorts, 0 ) == 1 || die( "" + idx( shorts, 0 ) );
        ok |= idx( shorts, 1 ) == 2 || die( "" + idx( shorts, 1 ) );
        ok |= idx( shorts, 2 ) == 3 || die( "" + idx( shorts, 2 ) );

        shorts = input.readMediumShortArray();
        ok |= shorts.length == 3 || die( "" + shorts.length );
        ok |= idx( shorts, 0 ) == 2 || die( "" + idx( shorts, 0 ) );
        ok |= idx( shorts, 1 ) == 2 || die( "" + idx( shorts, 1 ) );
        ok |= idx( shorts, 2 ) == 2 || die( "" + idx( shorts, 2 ) );


        int[] ints = input.readSmallIntArray();
        ok |= ints.length == 3 || die( "" + ints.length );
        ok |= idx( ints, 0 ) == 1 || die( "" + idx( ints, 0 ) );
        ok |= idx( ints, 1 ) == 2 || die( "" + idx( ints, 1 ) );
        ok |= idx( ints, 2 ) == 3 || die( "" + idx( ints, 2 ) );

        ints = input.readLargeIntArray();
        ok |= ints.length == 3 || die( "" + ints.length );
        ok |= idx( ints, 0 ) == 1 || die( "" + idx( ints, 0 ) );
        ok |= idx( ints, 1 ) == 2 || die( "" + idx( ints, 1 ) );
        ok |= idx( ints, 2 ) == 3 || die( "" + idx( ints, 2 ) );

        ints = input.readMediumIntArray();
        ok |= ints.length == 3 || die( "" + ints.length );
        ok |= idx( ints, 0 ) == 2 || die( "" + idx( ints, 0 ) );
        ok |= idx( ints, 1 ) == 2 || die( "" + idx( ints, 1 ) );
        ok |= idx( ints, 2 ) == 2 || die( "" + idx( ints, 2 ) );


        long[] longs = input.readSmallLongArray();
        ok |= longs.length == 3 || die( "" + longs.length );
        ok |= idx( longs, 0 ) == 1 || die( "" + idx( longs, 0 ) );
        ok |= idx( longs, 1 ) == 2 || die( "" + idx( longs, 1 ) );
        ok |= idx( longs, 2 ) == 3 || die( "" + idx( longs, 2 ) );

        longs = input.readLargeLongArray();
        ok |= longs.length == 3 || die( "" + longs.length );
        ok |= idx( longs, 0 ) == 1 || die( "" + idx( longs, 0 ) );
        ok |= idx( longs, 1 ) == 2 || die( "" + idx( longs, 1 ) );
        ok |= idx( longs, 2 ) == 3 || die( "" + idx( longs, 2 ) );

        longs = input.readMediumLongArray();
        ok |= longs.length == 3 || die( "" + longs.length );
        ok |= idx( longs, 0 ) == 2 || die( "" + idx( longs, 0 ) );
        ok |= idx( longs, 1 ) == 2 || die( "" + idx( longs, 1 ) );
        ok |= idx( longs, 2 ) == 2 || die( "" + idx( longs, 2 ) );


        float[] floats = input.readSmallFloatArray();
        ok |= floats.length == 3 || die( "" + floats.length );
        ok |= idx( floats, 0 ) == 1 || die( "" + idx( floats, 0 ) );
        ok |= idx( floats, 1 ) == 2 || die( "" + idx( floats, 1 ) );
        ok |= idx( floats, 2 ) == 3 || die( "" + idx( floats, 2 ) );

        floats = input.readLargeFloatArray();
        ok |= floats.length == 3 || die( "" + floats.length );
        ok |= idx( floats, 0 ) == 1 || die( "" + idx( floats, 0 ) );
        ok |= idx( floats, 1 ) == 2 || die( "" + idx( floats, 1 ) );
        ok |= idx( floats, 2 ) == 3 || die( "" + idx( floats, 2 ) );

        floats = input.readMediumFloatArray();
        ok |= floats.length == 3 || die( "" + floats.length );
        ok |= idx( floats, 0 ) == 2 || die( "" + idx( floats, 0 ) );
        ok |= idx( floats, 1 ) == 2 || die( "" + idx( floats, 1 ) );
        ok |= idx( floats, 2 ) == 2 || die( "" + idx( floats, 2 ) );


        double[] doubles = input.readSmallDoubleArray();
        ok |= doubles.length == 3 || die( "" + doubles.length );
        ok |= idx( doubles, 0 ) == 1 || die( "" + idx( doubles, 0 ) );
        ok |= idx( doubles, 1 ) == 2 || die( "" + idx( doubles, 1 ) );
        ok |= idx( doubles, 2 ) == 3 || die( "" + idx( doubles, 2 ) );

        doubles = input.readLargeDoubleArray();
        ok |= doubles.length == 3 || die( "" + doubles.length );
        ok |= idx( doubles, 0 ) == 1 || die( "" + idx( doubles, 0 ) );
        ok |= idx( doubles, 1 ) == 2 || die( "" + idx( doubles, 1 ) );
        ok |= idx( doubles, 2 ) == 3 || die( "" + idx( doubles, 2 ) );

        doubles = input.readMediumDoubleArray();
        ok |= doubles.length == 3 || die( "" + doubles.length );
        ok |= idx( doubles, 0 ) == 2 || die( "" + idx( doubles, 0 ) );
        ok |= idx( doubles, 1 ) == 2 || die( "" + idx( doubles, 1 ) );
        ok |= idx( doubles, 2 ) == 2 || die( "" + idx( doubles, 2 ) );

    }


    @Test
    public void test2() {
        ByteBuf buf = new ByteBuf();
        buf.add( "abc" );
        buf.add( "def" );
        boolean ok = true;
        final byte[] bytes = buf.readForRecycle();
        ok |= bytes[ 3 ] == 'd' || die();
    }

    @Test
    public void test3() throws UnsupportedEncodingException {
        ByteBuf buf = new ByteBuf();
        buf.add( URLEncoder.encode( "abc", "UTF-8" ) );
        buf.addUrlEncodedByteArray( new byte[]{ ( byte ) 1, 2, 3 } );
        buf.add( URLEncoder.encode( "def", "UTF-8" ) );
        boolean ok = true;
        final byte[] bytes = buf.readForRecycle();
        ok |= bytes[ 3 ] == '%' || die();
        ok |= bytes[ 4 ] == '0' || die();
        ok |= bytes[ 5 ] == '1' || die();
        ok |= bytes[ 6 ] == '%' || die( "" + bytes[ 5 ] );
        ok |= bytes[ 7 ] == '0' || die();
        ok |= bytes[ 8 ] == '2' || die();
        ok |= bytes[ 9 ] == '%' || die();
        ok |= bytes[ 10 ] == '0' || die();
        ok |= bytes[ 11 ] == '3' || die();
        ok |= bytes[ 12 ] == 'd' || die();

    }

    @Test
    public void testMe() {
        ByteBuf buf = new ByteBuf();
        buf.add( bytes( "0123456789\n" ) );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456END\n" );


        String out = new String( buf.readAndReset(), 0, buf.len() );
        assertEquals( 66, buf.len() );
        assertTrue( out.endsWith( "END\n" ) );

    }

    @Test
    public void testExact() {
        ByteBuf buf = ByteBuf.createExact( 66 );
        buf.add( bytes( "0123456789\n" ) );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456END\n" );


        String out = new String( buf.readAndReset() );
        assertEquals( 66, buf.len() );
        assertTrue( out.endsWith( "END\n" ) );

    }

    @Test ( expected = Exceptions.SoftenedException.class )
    public void testExact2TooSmall() {
        ByteBuf buf = ByteBuf.createExact( 22 );
        buf.add( bytes( "0123456789\n" ) );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456789\n" );
        buf.add( "0123456END\n" );


    }


    @Test
    public void doubleTest() {
        ByteBuf buf = ByteBuf.createExact( 8 );

        //addObject the double
        buf.add( 10.0000000000001 );

        byte[] bytes = buf.readAndReset();
        boolean worked = true;

        worked |= idxDouble( bytes, 0 ) == 10.0000000000001 || die( "Double worked" );

    }


    @Test
    public void floatTest() {
        ByteBuf buf = ByteBuf.createExact( 8 );

        //addObject the float
        buf.add( 10.001f );

        byte[] bytes = buf.readAndReset();
        boolean worked = true;

        worked |= buf.len() == 4 || die( "Float worked" );


        //read the float
        float flt = idxFloat( bytes, 0 );

        worked |= flt == 10.001f || die( "Float worked" );

    }

    @Test
    public void intTest() {
        ByteBuf buf = ByteBuf.createExact( 8 );

        //Add the int to the array
        buf.add( 99 );

        byte[] bytes = buf.readAndReset();
        boolean worked = true;


        //Read the int back
        int value = idxInt( bytes, 0 );

        worked |= buf.len() == 4 || die( "Int worked length = 4" );
        worked |= value == 99 || die( "Int worked value was 99" );

    }

    @Test
    public void charTest() {
        ByteBuf buf = ByteBuf.createExact( 8 );

        //Add the char to the array
        buf.add( 'c' );

        byte[] bytes = buf.readAndReset();
        boolean worked = true;


        //Read the char back
        int value = idxChar( bytes, 0 );

        worked |= buf.len() == 2 || die( "char worked length = 4" );
        worked |= value == 'c' || die( "char worked value was 'c'" );

    }


    @Test
    public void shortTest() {
        ByteBuf buf = ByteBuf.createExact( 8 );

        //Add the short to the array
        buf.add( ( short ) 77 );

        byte[] bytes = buf.readAndReset();
        boolean worked = true;


        //Read the short back
        int value = idxShort( bytes, 0 );

        worked |= buf.len() == 2 || die( "short worked length = 2" );
        worked |= value == 77 || die( "short worked value was 77" );

    }


    @Test
    public void byteTest() {
        ByteBuf buf = ByteBuf.createExact( 8 );

        //Add the byte to the array
        buf.add( ( byte ) 33 );

        byte[] bytes = buf.readAndReset();
        boolean worked = true;


        //Read the byte back
        int value = idx( bytes, 0 );

        worked |= buf.len() == 1 || die( "byte worked length = 1" );
        worked |= value == 33 || die( "byte worked value was 33" );

    }


    @Test
    public void addRockSockEmRobotEm() {
        boolean worked = true;
        ByteBuf buf = ByteBuf.create( 1 );

        //Add the various to the array
        buf.add( ( byte ) 1 );
        buf.add( ( short ) 2 );
        buf.add( ( char ) 3 );
        buf.add( 4 );
        buf.add( ( float ) 5 );
        buf.add( ( long ) 6 );
        buf.add( ( double ) 7 );

        worked |= buf.len() == 29 || die( "length = 29" );


        byte[] bytes = buf.readAndReset();

        byte myByte;
        short myShort;
        char myChar;
        int myInt;
        float myFloat;
        long myLong;
        double myDouble;

        myByte = idx( bytes, 0 );
        myShort = idxShort( bytes, 1 );
        myChar = idxChar( bytes, 3 );
        myInt = idxInt( bytes, 5 );
        myFloat = idxFloat( bytes, 9 );
        myLong = idxLong( bytes, 13 );
        myDouble = idxDouble( bytes, 21 );

        worked |= myByte == 1 || die( "value was 1" );
        worked |= myShort == 2 || die( "value was 2" );
        worked |= myChar == 3 || die( "value was 3" );
        worked |= myInt == 4 || die( "value was 4" );
        worked |= myFloat == 5 || die( "value was 5" );
        worked |= myLong == 6 || die( "value was 6" );
        worked |= myDouble == 7 || die( "value was 7" );

        ByteBuf buf2 = ByteBuf.create( bytes );

        bytes = buf2.readAndReset();

        myDouble = idxDouble( bytes, 21 );
        worked |= myDouble == 7 || die( "value was 7" );


    }

}
