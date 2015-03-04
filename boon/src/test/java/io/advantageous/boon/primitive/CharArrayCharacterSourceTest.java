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

import io.advantageous.boon.primitive.CharArrayCharacterSource;
import io.advantageous.boon.primitive.Chr;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

public class CharArrayCharacterSourceTest {

    CharArrayCharacterSource source;
    String testString = "abc";

    @Before
    public void setup() {
        source = new CharArrayCharacterSource ( testString.toCharArray ()  );
    }


    @Test
    public void safeNext() {
        int i = source.safeNextChar();
        boolean ok = i  == 'a' || die( "" + (char) i);

        i = source.nextChar();
        ok = i  == 'b' || die( "" + (char) i);

        i = source.nextChar();
        ok = i  == 'c' || die( "" + (char) i);


        i = source.safeNextChar ();
        ok = i  == -1 || die( "" +  i);

        try {
            i = source.nextChar();
            die();
        } catch ( ArrayIndexOutOfBoundsException aiobe ) {

        }
    }


    @Test
    public void hasNextTest() {
        StringBuilder builder = new StringBuilder(  );
        while (source.hasChar()) {
            builder.append ( source.nextChar () );
        }

        boolean ok = builder.toString().equals( testString );
    }

    @Test public void consumeIfMatchTest() {

        String testString = "abc true abc";
        source = new CharArrayCharacterSource ( testString );
        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case 't':
                   found = true;
                   break loop;
            }
            source.nextChar();
        }

       boolean ok = found || die("not found");
       ok |= source.consumeIfMatch ( "true".toCharArray() ) || die();
       ok |= source.location() == 8 || die("" + source.location());
       ok |= source.currentChar() == ' ' || die("" + (char)source.currentChar());

    }

    @Test public void consumeIfMatchNotMatchingTest() {

        String testString = "abc train abc";
        source = new CharArrayCharacterSource ( testString );
        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case 't':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        ok |= !source.consumeIfMatch ( "true".toCharArray() ) || die();
        ok |= source.location() == 4 || die("" + source.location());
        ok |= source.currentChar() == 't' || die("" + (char)source.currentChar());

    }

    @Test public void findStringWithFindNextChar() {

        String testString = "abc \"train\" abc";
        source = new CharArrayCharacterSource ( testString );
        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.nextChar ();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
        }

        boolean ok = found || die("not found");
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals(chars, "train".toCharArray()) || die(new String(chars));
        ok |= source.currentChar() == ' ' || die("" + (char)source.currentChar());
        ok |= source.location() == 11 || die("" + source.location());

    }


    @Test public void findStringWithFindNextCharWithEscape() {

        String testString = "abc \"train \\b\" abc";
        source = new CharArrayCharacterSource ( testString );
        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.nextChar ();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
        }

        boolean ok = found || die("not found");
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "train \\b".toCharArray ()) || die(new String(chars));

        ok |= source.currentChar() == ' ' || die("" + (char)source.currentChar());
        ok |= source.location() == 14 || die("" + source.location());

    }

    @Test public void findStringWithFindNextCharWithEscapeOfQuote() {

        String testString = "abc \"train \\\"\" abc0123456789";
        source = new CharArrayCharacterSource ( testString );
        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.nextChar ();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
        }

        boolean ok = found || die("not found");
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "train \\\"".toCharArray ()) || die(new String(chars));
        ok |= source.currentChar() == ' ' || die("" + (char)source.currentChar());
        ok |= source.location() == 14 || die("" + source.location());

    }


    @Test public void skipWhiteSpace() {

        String testString = "a   b c";
        source = new CharArrayCharacterSource ( testString );

        boolean ok = source.nextChar() == 'a' || die("" + (char)source.currentChar());


        source.skipWhiteSpace();

        ok &= source.nextChar() == 'b' || die("" + (char)source.currentChar());

        source.skipWhiteSpace();

        ok &= source.nextChar() == 'c' || die("" + (char)source.currentChar());

        source.skipWhiteSpace();


    }


    @Test public void readNumberTest() {

        String testString = "123";
        source = new CharArrayCharacterSource ( testString );

        char [] numberChars = source.readNumber();
        boolean ok = Chr.equals ( "123".toCharArray (), numberChars ) || die( new String(numberChars) ) ;

    }




    @Test public void readNumberTest2() {

        String testString = "123 456";
        source = new CharArrayCharacterSource ( testString );

        char [] numberChars = source.readNumber();
        boolean ok = Chr.equals ( "123".toCharArray (), numberChars ) || die( new String(numberChars) ) ;


        source.skipWhiteSpace();

        numberChars = source.readNumber();
        ok = Chr.equals ( "456".toCharArray (), numberChars ) || die( new String(numberChars) ) ;

    }



}