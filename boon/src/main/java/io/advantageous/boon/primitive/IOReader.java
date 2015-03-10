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


import java.io.IOException;
import java.io.Reader;

/**
 * Created by Richard on 3/13/14.
 */
public class IOReader extends Reader{


    public static int defaultBufferSize = 100_000;

    private char buffer[];

    private Reader reader;



    private int length;

    private int position;



    public IOReader( int size) {

        this.reader = reader;
        buffer = new char[size];

    }

    public IOReader() {
        this.reader = reader;
        buffer = new char[defaultBufferSize];
    }



    public  static IOReader reader(IOReader reader,   int size) {

        if (reader == null) {
            return new IOReader(size);
        } else {
            return reader;
        }

    }



    public  IOReader reader(Reader in) {

        try {
            close();
        } catch (IOException e) {
            //Boon.logger("IO").warn(e.getMessage(), e);
        }

        this.reader = in;
        return this;
    }




    public  int read() throws IOException {

        if (position >= length) {
            position = 0;
            int countRead = reader.read(buffer);
            length = countRead;
            if (length == -1) {
                return -1;
            }

        }

        int value = buffer [position];

        position++;
        return value & 0xff;
    }

    @Override
    public int read(char[] destination, int destinationOffset, int destinationLength) throws IOException {

        if (reader == null) {
            throw new IOException("Stream is closed");
        }


        final int available = length - position;



        /*There is more in the buffer than they asked for so give them what we have an increment the position. */
        if ( available >=  destinationLength ) {
            System.arraycopy(this.buffer, position, destination,
                    destinationOffset, destinationLength);
            position += destinationLength;
            return destinationLength;
        /* There is something in the buffer. just not enough */
        } else  {

            if (available>0) {
                //puts (available, destinationLength, destinationOffset);

                System.arraycopy(this.buffer, position, destination,
                        destinationOffset, available);

                /* YOu read some so increment the destination. */
                destinationLength -= available;
                destinationOffset += available;
            }


            //puts (available, destinationLength, destinationOffset);

            /* Read the buffer and server them the rest out of the buffer. */
            //grow(destination);


            position = 0;
            int countRead = reader.read(buffer);
            length = countRead;
            if (length == -1) {
                return available == 0 ? -1 : available;
            }

            //puts (available, destinationLength, destinationOffset);

            /* We read a new buffer in but we need the lesser of the two. */
            int amountToRead = destinationLength < length ? destinationLength : length;
            System.arraycopy(this.buffer, position, destination,
                    destinationOffset, amountToRead);
            position = amountToRead;

            destinationLength -= amountToRead;
            destinationOffset += amountToRead;

            if (destinationLength==0) {

                return amountToRead + available;

            } else {

                countRead = read(destination, destinationOffset, destinationLength);
                if (countRead == -1) {
                    return amountToRead + available;
                } else {
                    return amountToRead + available + countRead;
                }
            }


        }



    }

    public  long skip(long n) throws IOException {
        throw new IOException("Skip not supported");
    }

    public  void close() throws IOException {

        if (reader!=null) {
            reader.close();
        }
        reader = null;
        this.position = 0;
        this.length = 0;
    }


}
