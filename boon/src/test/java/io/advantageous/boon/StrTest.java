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

package io.advantageous.boon;


import io.advantageous.boon.Str;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Str.*;
import static org.junit.Assert.*;

public class StrTest {


    boolean ok;



    @Test
    public void sliceOfTest() {

        String test = "01234567890";

        String sliceOf = sliceOf(test, 0, 100);

        Str.equalsOrDie(test, sliceOf);
    }

    @Test
    public void insideOfString() {

        String test = "bacon HotDog Onion Pickle Donut";

        ok &= insideOf("bacon", test, "Donut") || die();
    }

    @Test
    public void insideOfString2() {

        String test = "bacon HotDog Onion Pickle bacon";

        ok &= insideOf("bacon", test, "bacon") || die();
    }

    @Test
    public void insideOfString3() {

        String test = "bacon HotDog Onion Pickle bacon";

        ok &= !insideOf("bacon", test, "Donut") || die();
    }

    @Test
    public void insideOfString4() {

        String test = "bacon HotDog Onion Pickle ";

        ok &= insideOf("bacon", test, "") || die();
    }

    @Test
    public void insideOfString5() {

        String test = "1234567890";

        ok &= insideOf("123", test, "890") || die();
    }


    @Test
    public void insideOfString6() {

        String test = "";

        ok &= !insideOf("123", test, "890") || die();
    }

    @Test
    public void insideOfString7() {

        String test = "123";

        ok &= insideOf("", test, "") || die();
    }

    @Test
    public void index() {

        String letters = "abcd";

        assertEquals(
                'a',
                idx( letters, 0 )
        );


        assertEquals(
                'd',
                idx( letters, -1 )
        );


        assertEquals(
                'd',
                idx( letters, letters.length() - 1 )
        );


        letters = idx( letters, 1, 'z' );

        assertEquals(
                'z',
                idx( letters, 1 )
        );
    }


    @Test
    public void isIn() {

        String letters = "abcd";


        assertTrue(
                in( 'a', letters )
        );

        assertFalse(
                in( 'z', letters )
        );

    }


    @Test
    public void isInAtOffset() {

        String letters = "abcd";


        assertFalse(
                in( 'a', 1, letters )
        );

        assertTrue(
                in( 'c', 1, letters )
        );

    }

    @Test
    public void isInAtRange() {

        String letters = "abcd";


        assertFalse(
                in( 'a', 1, 2, letters )
        );

        assertTrue(
                in( 'c', 1, 3, letters )
        );

    }

    @Test
    public void slice() {

        String letters = "abcd";


        assertEquals(
                "ab",
                slc( letters, 0, 2 )
        );

        assertEquals(
                "bc",
                slc( letters, 1, -1 )
        );

        //>>> letters[2:]
        //['c', 'd']
        //>>> letters[-2:]
        //['c', 'd']

        assertEquals(
                "cd",
                slc( letters, -2 )
        );


        assertEquals(
                "cd",
                slc( letters, 2 )
        );


        //>>> letters[:-2]
        //     ['a', 'b']
        assertEquals(
                "ab",
                slcEnd( letters, -2 )
        );


        //>>> letters[:-2]
        //     ['a', 'b']
        assertEquals(
                "ab",
                slcEnd( letters, 2 )
        );

    }


    @Test
    public void outOfBounds() {

        String letters = "abcde";

        slcEnd( letters, 100 );
        slcEnd( letters, -100 );

        slc( letters, 100 );
        slc( letters, -100 );
        idx( letters, 100 );
        idx( letters, -100 );


        letters = idx( letters, 100, 'x' );


        letters = idx( letters, -100, 'z' );


        assertEquals(
                'x',
                idx( letters, -1 )
        );


        assertEquals(
                'z',
                idx( letters, 0 )
        );

    }


    @Test
    public void compactTest() {
        String letters =
                "ab\0\0\0\0\0\0c\0d\0\0e";

        letters = compact( letters );

        assertEquals(
                "abcde",
                letters

        );


    }


    @Test
    public void joinByTest() {
        String foo = Str.join( ',', "foo", "bar", "baz" );
        boolean ok = true;

        ok |= foo.equals( "foo,bar,baz" ) || die( "Foo was not equal to foo,bar,baz" );


    }


    @Test
    public void lines() {
        String foo = Str.lines(
                "Line 1",
                "Line 2",
                "Line 3" );

        boolean ok = true;


        ok |= foo.startsWith( "Line 1" ) || die( "foo string started with Line 1" );

        ok |= foo.endsWith( "Line 3" ) || die( "foo string ends with Line 3 \n" + foo );

    }


}
