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

import java.nio.charset.StandardCharsets;

public class InputByteArray implements Input {

    private final byte[] array;

    private int location;

    public InputByteArray( byte[] array ) {

        this.array = array;

    }

    @Override
    public void readFully( byte[] readToThis ) {
        Byt._idx( readToThis, 0, array, location, readToThis.length );
        location += readToThis.length;
    }

    @Override
    public void readFully( byte[] readToThis, int off, int len ) {
        Byt._idx( readToThis, off, array, location, len );
        location += readToThis.length;
    }

    @Override
    public int skipBytes( int n ) {
        return location += n;
    }

    @Override
    public void location( int n ) {
        location = n;
    }

    @Override
    public int location() {
        return location;
    }

    @Override
    public void reset() {
        location = 0;
    }

    @Override
    public boolean readBoolean() {
        final byte val = Byt.idx( array, location );

        location += 1;

        if ( val == 0 ) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public byte readByte() {

        byte value = Byt.idx( array, location );
        location += 1;
        return value;

    }

    @Override
    public short readUnsignedByte() {

        short value = Byt.idxUnsignedByte( array, location );
        location += 1;
        return value;

    }

    @Override
    public short readShort() {

        short value = Byt.idxShort( array, location );
        location += 2;

        return value;
    }

    @Override
    public int readUnsignedShort() {

        int value = Byt.idxUnsignedShort( array, location );
        location += 2;

        return value;
    }

    @Override
    public char readChar() {

        char value = Byt.idxChar( array, location );
        location += 2;

        return value;
    }

    @Override
    public int readInt() {

        int value = Byt.idxInt( array, location );
        location += 4;

        return value;
    }

    @Override
    public long readUnsignedInt() {

        long value = Byt.idxUnsignedInt( array, location );
        location += 4;

        return value;
    }

    @Override
    public long readLong() {

        long value = Byt.idxLong( array, location );
        location += 8;

        return value;
    }

    @Override
    public float readFloat() {

        float value = Byt.idxFloat( array, location );
        location += 4;
        return value;

    }

    @Override
    public double readDouble() {

        double value = Byt.idxDouble( array, location );
        location += 8;
        return value;

    }


    @Override
    public String readSmallString() {
        short size = this.readUnsignedByte();

        byte[] bytes = this.readBytes( size );
        return new String( bytes, StandardCharsets.UTF_8 );
    }


    @Override
    public String readMediumString() {
        int size = this.readUnsignedShort();

        byte[] bytes = this.readBytes( size );

        return new String( bytes, StandardCharsets.UTF_8 );
    }


    @Override
    public String readLargeString() {
        int size = this.readInt();


        byte[] bytes = this.readBytes( size );

        return new String( bytes, StandardCharsets.UTF_8 );
    }


    @Override
    public byte[] readSmallByteArray() {
        short size = this.readUnsignedByte();

        byte[] bytes = this.readBytes( size );
        return bytes;
    }


    @Override
    public byte[] readMediumByteArray() {
        int size = this.readUnsignedShort();

        byte[] bytes = this.readBytes( size );

        return bytes;
    }

    @Override
    public short[] readSmallShortArray() {

        short size = this.readUnsignedByte();

        return doReadShortArray( size );


    }

    private short[] doReadShortArray( int size ) {
        short[] values = new short[ size ];

        for ( int index = 0; index < values.length; index++ ) {
            values[ index ] = this.readShort();
        }
        return values;
    }

    @Override
    public short[] readLargeShortArray() {

        int size = this.readInt();

        return doReadShortArray( size );

    }

    @Override
    public short[] readMediumShortArray() {

        int size = this.readUnsignedShort();

        return doReadShortArray( size );

    }


    @Override
    public byte[] readLargeByteArray() {
        int size = this.readInt();


        byte[] bytes = this.readBytes( size );

        return bytes;
    }

    @Override
    public byte[] readBytes( int size ) {
        byte[] bytes = new byte[ size ];
        this.readFully( bytes );
        return bytes;
    }


    @Override
    public int[] readSmallIntArray() {

        short size = this.readUnsignedByte();

        return doReadIntArray( size );


    }

    private int[] doReadIntArray( int size ) {
        int[] values = new int[ size ];

        for ( int index = 0; index < values.length; index++ ) {
            values[ index ] = this.readInt();
        }
        return values;
    }

    @Override
    public int[] readLargeIntArray() {

        int size = this.readInt();

        return doReadIntArray( size );

    }

    @Override
    public int[] readMediumIntArray() {

        int size = this.readUnsignedShort();

        return doReadIntArray( size );

    }


    ///


    @Override
    public long[] readSmallLongArray() {

        short size = this.readUnsignedByte();

        return doReadLongArray( size );


    }

    private long[] doReadLongArray( int size ) {
        long[] values = new long[ size ];

        for ( int index = 0; index < values.length; index++ ) {
            values[ index ] = this.readLong();
        }
        return values;
    }

    @Override
    public long[] readLargeLongArray() {

        int size = this.readInt();

        return doReadLongArray( size );

    }

    @Override
    public long[] readMediumLongArray() {

        int size = this.readUnsignedShort();

        return doReadLongArray( size );

    }

    @Override
    public float[] readSmallFloatArray() {
        short size = this.readUnsignedByte();
        return doReadFloatArray( size );
    }

    @Override
    public float[] readLargeFloatArray() {
        int size = this.readInt();
        return doReadFloatArray( size );
    }

    @Override
    public float[] readMediumFloatArray() {
        int size = this.readUnsignedShort();
        return doReadFloatArray( size );
    }

    private float[] doReadFloatArray( int size ) {
        float[] values = new float[ size ];
        for ( int index = 0; index < values.length; index++ ) {
            values[ index ] = this.readFloat();
        }
        return values;
    }


    @Override
    public double[] readSmallDoubleArray() {
        short size = this.readUnsignedByte();
        return doReadDoubleArray( size );
    }

    @Override
    public double[] readLargeDoubleArray() {
        int size = this.readInt();
        return doReadDoubleArray( size );
    }

    @Override
    public double[] readMediumDoubleArray() {
        int size = this.readUnsignedShort();
        return doReadDoubleArray( size );
    }


    private double[] doReadDoubleArray( int size ) {
        double[] values = new double[ size ];
        for ( int index = 0; index < values.length; index++ ) {
            values[ index ] = this.readDouble();
        }
        return values;
    }

    //TODO addObject unsigned int, unsigned byte, unsigned short

}
