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


public interface Input {


    void readFully( byte[] b );

    void readFully( byte[] b, int off, int len );

    int skipBytes( int n );

    void location( int n );

    int location();

    void reset();

    boolean readBoolean();

    byte readByte();

    short readUnsignedByte();

    short readShort();

    int readUnsignedShort();

    char readChar();

    int readInt();

    long readUnsignedInt();

    long readLong();

    float readFloat();

    double readDouble();

    String readSmallString();

    String readLargeString();

    String readMediumString();


    byte[] readSmallByteArray();

    byte[] readLargeByteArray();

    byte[] readMediumByteArray();

    short[] readSmallShortArray();

    short[] readLargeShortArray();

    short[] readMediumShortArray();


    int[] readSmallIntArray();

    int[] readLargeIntArray();

    int[] readMediumIntArray();

    byte[] readBytes( int size );


    long[] readSmallLongArray();

    long[] readLargeLongArray();

    long[] readMediumLongArray();


    float[] readSmallFloatArray();

    float[] readLargeFloatArray();

    float[] readMediumFloatArray();


    double[] readSmallDoubleArray();

    double[] readLargeDoubleArray();

    double[] readMediumDoubleArray();

    //TODO addObject unsigned int, unsigned byte, unsigned short array


}
