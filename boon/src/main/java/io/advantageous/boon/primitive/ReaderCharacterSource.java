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

import java.io.*;
import java.util.Arrays;

import static io.advantageous.boon.Exceptions.die;

public class ReaderCharacterSource implements CharacterSource {


    private static final int MAX_TOKEN_SIZE=5;

    private final Reader reader;
    private int readAheadSize;

    private int ch = -2;

    private boolean foundEscape;


    private char[] readBuf;


    private int index;

    private int length;


    boolean more = true;
    private boolean done = false;



    public ReaderCharacterSource( final Reader reader, final int readAheadSize) {
        this.reader = reader;
        this.readBuf =  new char[readAheadSize + MAX_TOKEN_SIZE];
        this.readAheadSize = readAheadSize;
    }


    public ReaderCharacterSource( final Reader reader ) {
        this.reader = reader;
        this.readAheadSize = 10_000;
        this.readBuf =  new char[ readAheadSize + MAX_TOKEN_SIZE ];
    }

    public ReaderCharacterSource( final String string ) {
        this(new StringReader ( string ));
    }


    private void readForToken() {
        try {
            length += reader.read ( readBuf, readBuf.length-MAX_TOKEN_SIZE, MAX_TOKEN_SIZE );
        } catch ( IOException e ) {
            Exceptions.handle(e);
        }
    }

    private void ensureBuffer() {

        try {
            if (index >= length && !done) {
                readNextBuffer ();
            } else if (done && index >=length) {
                more = false;
            }else {
                more = true;
            }
        } catch ( Exception ex ) {
            String str = CharScanner.errorDetails ( "ensureBuffer issue", readBuf, index, ch );
            Exceptions.handle (  str, ex );
        }
    }

    private void readNextBuffer() throws IOException {


        length = reader.read ( readBuf, 0, readAheadSize );


        index = 0;
        if (length == -1) {
             ch = -1;
             length = 0;
             more = false;
             done = true;
        } else {
             more = true;
        }
    }

    @Override
    public final int nextChar() {
        ensureBuffer();
        return ch = readBuf[index++];
    }

    @Override
    public  final int currentChar() {
        ensureBuffer();
        return readBuf[index];
    }

    @Override
    public  final boolean hasChar() {
        ensureBuffer();
        return more;
    }

    @Override
    public  final boolean consumeIfMatch( char[] match ) {
        try {

            char [] _chars = readBuf;
            int i=0;
            int idx = index;
            boolean ok = true;

            if ( idx + match.length > length ) {
                readForToken ();
            }

            for (; i < match.length; i++, idx++) {
                    ok &=  ( match[i] == _chars[idx] );
                    if (!ok) break;
            }

            if ( ok ) {
                index = idx;
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails ( "consumeIfMatch issue", readBuf, index, ch );
            return Exceptions.handle ( boolean.class, str, ex );
        }

    }

    @Override
    public final  int location() {
        return index;
    }

    public final int safeNextChar() {
        try {
            ensureBuffer();
            return index + 1 < readBuf.length ? readBuf[index++] : -1;
        } catch (Exception ex) {
            String str = CharScanner.errorDetails ( "safeNextChar issue", readBuf, index, ch );
            return Exceptions.handle ( int.class, str, ex );
        }
    }


    private final char[] EMPTY_CHARS = new char[0];


    @Override
    public final char[] findNextChar( int match, int esc ) {
        return findNextChar(false, false, match, esc);
    }

    /**
     * Remember that this must work past buffer reader boundaries so we need to keep
     * track where we were in the nested run.
     *
     * If we start with match then we skip to the the next match.
     *
     * @param inMiddleOfString In the middle of a string.
     * @param wasEscapeChar    If we were called before (recursion), where we in the escape character state.
     * @param match  The match char is usually '"'
     * @param esc    The escape char is usually '\'
     * @return the string from the next char.
     */
    public final char[] findNextChar( boolean inMiddleOfString, boolean wasEscapeChar, int match, int esc ) {
        try{
            ensureBuffer(); //grow the buffer and read in if needed


            int idx = index;
            char[] _chars = readBuf;

            int length = this.length;


            int ch = this.ch;

            if ( !inMiddleOfString ) {
                foundEscape=false;

                if ( ch == match ) { //we can start with a match but we
                                     // ignore it if we are not in the middle of a string.

                } else if ( idx < length -1 ) {
                    ch = _chars[idx];


                    if (ch == match ) {
                        idx++;
                    }
                }
            }
            if ( idx < length ) {
                ch = _chars[idx];
            }

            /* Detect an empty string and then leave unless we came into this method in the escape state.. */
            if (ch == '"' && !wasEscapeChar) {

                index = idx;
                index++;
                return EMPTY_CHARS;
            }
            int start = idx;

            if (wasEscapeChar) {
                idx++;
            }

            boolean foundEnd = false; //Have we actually found the end of the string?
            char [] results ; //The results so far might be the whole thing if we found the end.

            boolean _foundEscape = false;

            /* Iterate through the buffer looking for the match which is the close quote most likely. */
            while  (true) {
                    ch  = _chars[idx];


                    /* If we found the close quote " or the escape character \ */
                    if ( ch == match || ch == esc ) {
                        if ( ch == match ) {
                            foundEnd = true;
                            break;
                        } else if ( ch == esc ) {
                            wasEscapeChar = true;
                            _foundEscape = true;

                            /** if we are dealing with an escape then see if the escaped char is a match
                             *  if so, skip it.
                             */
                            if ( idx + 1 < length) {

                                wasEscapeChar=false; //this denotes if we were able to skip because
                                // if we were not then the next method in the recursion has to if we don't find the end.
                                idx++;
                            }
                        }
                    }


                if ( idx >= length) break;
                idx++;
            }


            foundEscape = _foundEscape;

            /* After all that, we still might have an empty string! */
            if (idx == 0 ) {
                     results = EMPTY_CHARS;
            } else {
                    results =  Arrays.copyOfRange ( _chars, start, idx );
            }

            // At this point we have some results but it might not be all of the results if we did not
            // find the close '"' (match)
            index = idx;


            /* We found everthing so make like a tree and leave. */
            if (foundEnd) {
                    index++;
                    if (index < length) {
                        ch = _chars[index ];
                        this.ch = ch;
                    }
                    return results;
            /* We did not find everything so prepare for the next buffer read. */
            } else {
                    /* Detect if we have more buffers to read. */
                    if (index >= length && !done) {

                        /*If we have more to read then read it. */
                        ensureBuffer();
                        /* Recursively call this method. */
                        char results2[] = findNextChar(true, wasEscapeChar, match, esc);
                        return Chr.add(results, results2);
                    } else {
                        return Exceptions.die(char[].class, "Unable to find close char " + (char) match + " " + new String(results));
                    }
            }
        } catch (Exception ex ) {
            String str = CharScanner.errorDetails ( "findNextChar issue", readBuf, index, ch );
            return Exceptions.handle ( char[].class, str, ex );
        }


    }

    @Override
    public boolean hadEscape() {
        return foundEscape;
    }


    @Override
    public void skipWhiteSpace() {
        try {
            index = CharScanner.skipWhiteSpace( readBuf, index, length );
            if (index >= length && more) {

                ensureBuffer();

                skipWhiteSpace();
            }
        } catch ( Exception ex ) {


             ex.printStackTrace();
             String str = CharScanner.errorDetails ( "skipWhiteSpaceIfNeeded issue", readBuf, index, ch );
             Exceptions.handle (  ex, str, "\n\nLENGTH", length, "INDEX", index  );
        }
    }







    public char[] readNumber(  ) {
        try {
            ensureBuffer();

            char [] results =  CharScanner.readNumber( readBuf, index, length);
            index += results.length;

            if (index >= length && more) {
                ensureBuffer();
                if (length!=0) {
                    char results2[] = readNumber();
                    return Chr.add(results, results2);
                } else  {
                    return results;
                }
            } else {
                return results;
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails ( "readNumber issue", readBuf, index, ch );
            return Exceptions.handle ( char[].class, str, ex );
        }

    }

    @Override
    public String errorDetails( String message ) {

        return CharScanner.errorDetails ( message, readBuf, index, ch );
    }

}
