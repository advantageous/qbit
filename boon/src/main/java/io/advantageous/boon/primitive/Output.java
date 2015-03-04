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


public interface Output {

    void write( int b );

    void write( byte[] b );

    void write( byte[] b, int off, int len );

    void writeBoolean( boolean v );

    void writeByte( byte v );

    void writeUnsignedByte( short v );

    void writeShort( short v );

    void writeUnsignedShort( int v );

    void writeChar( char v );

    void writeInt( int v );

    void writeUnsignedInt( long v );

    void writeLong( long v );

    void writeFloat( float v );

    void writeDouble( double v );


    void writeLargeString( String s );

    void writeSmallString( String s );

    void writeMediumString( String s );

    void writeLargeByteArray( byte[] bytes );

    void writeSmallByteArray( byte[] bytes );

    void writeMediumByteArray( byte[] bytes );


    void writeLargeShortArray( short[] values );

    void writeSmallShortArray( short[] values );

    void writeMediumShortArray( short[] values );


    void writeLargeIntArray( int[] values );

    void writeSmallIntArray( int[] values );

    void writeMediumIntArray( int[] values );


    void writeLargeLongArray( long[] values );

    void writeSmallLongArray( long[] values );

    void writeMediumLongArray( long[] values );

    void writeLargeFloatArray( float[] values );

    void writeSmallFloatArray( float[] values );

    void writeMediumFloatArray( float[] values );


    void writeLargeDoubleArray( double[] values );

    void writeSmallDoubleArray( double[] values );

    void writeMediumDoubleArray( double[] values );

    //TODO addObject unsigned int, unsigned byte, unsigned short array


}
