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

import io.advantageous.boon.core.reflection.FastStringUtils;

import java.util.Arrays;

public class CharArrayCharacterSource implements CharacterSource {

    private char[] chars;
    private int index=0;
    private boolean foundEscape;
    private int ch;





    public CharArrayCharacterSource ( char[] chars ) {
        this.chars = chars;
    }


    public CharArrayCharacterSource ( String string ) {
        this.chars = FastStringUtils.toCharArray(string);
    }


    @Override
    public final int nextChar() {
        return ch = chars[index++];
    }

    public final int safeNextChar() {
        return ch = (index + 1 < chars.length ? chars[index++] : -1);
    }


    private final char[] EMPTY_CHARS = new char[0];

    @Override
    public final char[] findNextChar( final int match, final int esc ) {
        int idx = index;
        int startIndex = idx;
        foundEscape = false;
        char[] _chars = chars;
        int ch = 0;
        for (; idx < _chars.length; idx++) {
             ch  = _chars[idx];
             if ( ch == match || ch == esc ) {
                 if ( ch == match ) {
                     /** If you have found the next char, then return a copy of the buffer range.*/
                     index = idx+1;
                     this.ch = ch;
                     return  Arrays.copyOfRange ( _chars, startIndex, idx );
                 } else if ( ch == esc ) {
                     foundEscape = true;
                     /** if we are dealing with an escape then see if the escaped char is a match
                      *  if so, skip it.
                       */
                    if ( idx + 1 < _chars.length) {
                         idx++;
                    }
                 }
             }
        }

       index = idx;
       this.ch = ch;
       return EMPTY_CHARS;
    }


    @Override
    public boolean hadEscape() {
        return foundEscape;
    }

    public char[] readNumber(  ) {
        char [] results =  CharScanner.readNumber( chars, index);
        index += results.length;
        return results;
    }

    @Override
    public final int currentChar() {
        return chars[index];
    }

    @Override
    public final boolean hasChar() {
        return  index + 1 < chars.length;
    }


    @Override
    public final boolean consumeIfMatch( char[] match ) {

        int idx = index;
        char[] _chars = chars;

        boolean ok = true;

        for (int i=0; i < match.length; i++, idx++) {
           ok &=  ( match[i] == _chars[idx] );
           if (!ok) break;
        }

        if ( ok ) {
            index = idx;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final int location() {
        return index;
    }




    @Override
    public void skipWhiteSpace() {
        index = CharScanner.skipWhiteSpace( chars, index );
    }



    @Override
    public String errorDetails( String message ) {
        if (index < chars.length) {
            ch = chars[index];
        } else {
            ch = chars[chars.length-1];
        }
        return CharScanner.errorDetails ( message, chars, index, ch );
    }

}
