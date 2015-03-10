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

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Richard on 3/11/14.
 */
public final class InMemoryInputStream extends ByteArrayInputStream {

    private static final byte BUFFER_FOR_YOU[] = new byte[1];

    private byte buffer[];

    private int position;



    public  InMemoryInputStream (byte[] buffer) {
        super(BUFFER_FOR_YOU);

        this.buffer = buffer;

    }


    public final int read() {

        int value;
        if (position >= buffer.length) {
            return -1;
        }
        value  = buffer [position];
        position++;
        return value & 0xff;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }


    public final int read(byte destination[], int destinationOffset, int destinationLength)  {


        final int available = buffer.length - position;

        int readAmount=0;



        /*There is more in the buffer than they asked for so give them what we have an increment the position. */
        if ( available >=  destinationLength ) {
            System.arraycopy(this.buffer, position, destination,
                    destinationOffset, destinationLength);
            position += destinationLength;
            readAmount = destinationLength;
        /* There is something in the buffer. just not enough */
        } else if (available>0) {
                //puts (available, destinationLength, destinationOffset);

                System.arraycopy(this.buffer, position, destination,
                        destinationOffset, available);
                position+=available;
                readAmount = available;

        }


        return readAmount <= 0 ? -1 : readAmount;


    }


    public final long skip(long n)  {
       return Exceptions.die(Long.class, "Skip not supported");
    }

    public final int available()  {
        return    buffer.length - position;

    }

    public final void mark(int limit) {
    }

    public final void reset()  {
        Exceptions.die("Resetting not supported");
    }

    public final boolean markSupported() {
        return false;
    }

    public final void close() throws IOException {

        position = 0;
        buffer = null;

    }
}
