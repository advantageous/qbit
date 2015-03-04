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

import io.advantageous.boon.Str;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

public class ReaderCharacterSourceTest {


    ReaderCharacterSource source;
    String testString = "abc";

    @Before
    public void setup() {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));

        //IOInputStream stream = IOInputStream.input(null, 10).input(inputStream);

         source = new ReaderCharacterSource (
                new InputStreamReader(inputStream)
          );
    }


    private void initSourceWithString(String testString) {

        ByteArrayInputStream inputStream = new InMemoryInputStream(testString.getBytes(StandardCharsets.UTF_8));


        source = new ReaderCharacterSource (
                new InputStreamReader(inputStream)
        );
    }


    private void initSourceWithString(String testString, int size) {

        ByteArrayInputStream inputStream = new InMemoryInputStream(testString.getBytes(StandardCharsets.UTF_8));


        source = new ReaderCharacterSource (
                new InputStreamReader(inputStream), size
        );
    }
    @Test
    public void reader2() {

    }



    @Test
    public void basicCurrentChar() {
        int i = source.nextChar();
        boolean ok = i  == 'a' || die( "" + (char) i);

        i = source.currentChar();
        ok = i  == 'b' || die( "" + (char) i);

    }

    @Test
    public void safeNext() {
        int i = source.nextChar();
        boolean ok = i  == 'a' || die( "" + (char) i);

        i = source.nextChar();
        ok = i  == 'b' || die( "" + (char) i);

        i = source.nextChar();
        ok = i  == 'c' || die( "" + (char) i);


        try {
            i = source.nextChar();
            die();
        } catch ( RuntimeException aiobe ) {

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
        initSourceWithString(testString);

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
        ok |= source.safeNextChar () == ' ' || die("" + (char)source.safeNextChar ());

    }

    @Test public void consumeIfMatchNotMatchingTest() {

        String testString = "abc train abc";
        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case 't':
                    found = true;
                    break loop;
            }
            source.nextChar ();
        }

        boolean ok = found || die("not found");
        ok |= !source.consumeIfMatch ( "true".toCharArray() ) || die();
        ok |= source.currentChar() == 't' || die("" + (char)source.currentChar());

    }

    @Test public void findStringWithFindNextChar() {

        String testString = "abc \"train\" abc";

        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            outputs( "current char", ( char ) i );
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        outputs( new String( chars ) );
        ok &= Chr.equals(chars, "train".toCharArray()) || die(new String(chars));

        ok |= source.currentChar () == ' ' || die("" + (char)source.currentChar());

    }


    @Test public void testEscapeOnBorder() {
        String hellString = "'1\\'a\\'b\\'c\\'\\'' '123' ".replace('\'', '\"');

        initSourceWithString(hellString, 10);

        puts(hellString);
        String output;


        output = new String(source.findNextChar('\"', '\\'));
        puts("output", '\n', output, '\n', "1\\\"a\\\"b\\\"c\\\"\\\"");

        Str.equalsOrDie(output, "1\\\"a\\\"b\\\"c\\\"\\\"");


        initSourceWithString(hellString, 3);

        output = new String(source.findNextChar('\"', '\\'));
        puts("output", '\n', output, '\n', "1\\\"a\\\"b\\\"c\\\"\\\"");

        Str.equalsOrDie(output, "1\\\"a\\\"b\\\"c\\\"\\\"");
     }

    @Test public void findStringWithFindNextCharWithEscape() {

        String testString = "abc \"train  a\\b\" abc";


        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "train  a\\b".toCharArray ()) || die(new String(chars));

        ok |= source.currentChar () == ' ' || die("" + (char)source.currentChar());

    }

    @Test public void stringBug() {

        String testString = " \"file\"";


        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "file".toCharArray ()) || die(new String(chars));


    }



    @Test public void stringBug2() {

        String testString = "\"\"";



        initSourceWithString(testString);

        boolean found = false;



        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "".toCharArray ()) || die(new String(chars));


    }

    @Test public void findStringWithFindNextCharWithEscape2() {

        String testString = "0123\"dog    bog\" z";



        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "dog    bog".toCharArray ()) || die(new String(chars));

        ok |= source.currentChar () == ' ' || die("" + (char)source.currentChar());

    }
    @Test public void findStringWithFindNextCharWithEscapeOfQuote() {

        String testString = "abc \"train\\\"\" abc0123456789";



        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "train\\\"".toCharArray ()) || die(new String(chars));
        ok |= source.currentChar () == ' ' || die("" + (char)source.currentChar());

    }

    @Test public void findString2() {

        String testString = "\"train\"";



        initSourceWithString(testString);

        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "train".toCharArray ()) || die(new String(chars));

    }

    @Test public void findString3() {

        String testString = "12345 \"train brain stain fain\" 678910";


        initSourceWithString(testString);


        boolean found = false;

        loop:
        while (source.hasChar()) {
            int i = source.currentChar();
            switch ( i ) {
                case '"':
                    found = true;
                    break loop;
            }
            source.nextChar();
        }

        boolean ok = found || die("not found");
        source.nextChar();
        char [] chars = source.findNextChar ( '"', '\\' ) ;

        ok &= Chr.equals( chars, "train brain stain fain".toCharArray ()) || die(new String(chars));

    }


    @Test public void skipWhiteSpace() {

        String testString = "a   b c";


        initSourceWithString(testString);


        boolean ok = source.nextChar() == 'a' || die("" + (char)source.currentChar());

        source.nextChar();
        source.skipWhiteSpace();

        ok &= source.nextChar() == 'b' || die("$$" + (char)source.currentChar() + "$$");
        source.nextChar();
        source.skipWhiteSpace();

        ok &= source.nextChar() == 'c' || die("" + (char)source.currentChar());

        source.skipWhiteSpace();


    }


    @Test public void readNumberTest() {

        String testString = "123";



        initSourceWithString(testString);

        char [] numberChars = source.readNumber();
        boolean ok = Chr.equals ( "123".toCharArray (), numberChars ) || die( new String(numberChars) ) ;

    }

    @Test public void readNumberTest3() {

        String testString = "123456789";


        initSourceWithString(testString);

        char [] numberChars = source.readNumber();
        boolean ok = Chr.equals ( "123456789".toCharArray (), numberChars ) || die( new String(numberChars) ) ;

    }


    @Test public void readNumberTest2() {

        String testString = "123 456";


        initSourceWithString(testString);

        char [] numberChars = source.readNumber();
        boolean ok = Chr.equals ( "123".toCharArray (), numberChars ) || die( new String(numberChars) ) ;


        source.skipWhiteSpace();

        numberChars = source.readNumber();
        ok = Chr.equals ( "456".toCharArray (), numberChars ) || die( new String(numberChars) ) ;

    }


    @Test public void someErrorTest() {

        String testString = "0123456789\n" +
                "0123456789\n" +
                "abcdefghijklmnopqrstuvwxyz";


        initSourceWithString(testString);

        for (int index =0; index<28; index++) {
            source.nextChar();
        }

        String str = source.errorDetails ( "some error" );

        outputs( str );
    }




    public static void outputs( Object... messages ) {


    }

}
