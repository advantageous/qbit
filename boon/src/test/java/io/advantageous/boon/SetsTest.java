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


import io.advantageous.boon.primitive.Arry;
import org.junit.Test;


import java.util.Collection;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Sets.*;
import static io.advantageous.boon.Sets.safeSet;
import static io.advantageous.boon.Sets.safeSortedSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings ( "unchecked" )
public class SetsTest {


    @Test
    public void arrayConversion() {

        String[] arry = Arry.array("0", "1", "2", "3", "4");

        Set<String> set = set( 3, arry);

        puts(set("0", "1", "2").equals(set));
    }


    @Test
    public void simple() {
        Set<String> set =
                set( "apple", "oranges", "pears", "grapes", "kiwi" );

        assertEquals( 5, len( set ) );
        assertTrue( in( "apple", set ) );

    }


    @Test
    public void sorted() {
        NavigableSet<String> set =
                sortedSet( "apple", "kiwi", "oranges", "pears", "pineapple" );

        assertEquals(
                5,
                len( set )
        );

        assertTrue(
                in( "apple", set )
        );

        assertEquals(

                "oranges", idx( set, "ora" )

        );

        assertEquals(

                "oranges", idx( set, "o" )

        );

        assertEquals(

                "pears",
                idx( set, "p" )

        );

        assertEquals(

                "pineapple",
                idx( set, "pi" )

        );

        assertEquals(

                "pineapple",
                after( set, "pi" )

        );

        assertEquals(

                "pears",
                before( set, "pi" )

        );


        assertEquals(

                sortedSet( "apple", "kiwi" ),
                slc( set, "ap", "o" )

        );

        assertEquals(

                sortedSet( "apple", "kiwi" ),
                slc( set, "o" )

        );

        assertEquals(

                sortedSet( "oranges", "pears", "pineapple" ),
                slcEnd( set, "o" )
        );

    }


    @Test
    public void copyTest() {
        Set<String> set = set( "apple", "pear", "orange" );

        Set<String> set2;

        set2 = set( copy( set ) );
        assertEquals(
                set, set2
        );

        set2 = set( copy( sortedSet( set ) ) );
        assertEquals(
                set, set2
        );


        set2 = set( copy( safeSet( set ) ) );
        assertEquals(
                set, set2
        );


        set2 = set( copy( safeSortedSet( set ) ) );
        assertEquals(
                set, set2
        );


    }

    @Test
    public void creation() {

        Set<String> set = set( "apple", "pear", "orange" );

        Set<String> set2 = set( enumeration( set ) );
        assertEquals(
                set, set2
        );

        set2 = sortedSet( enumeration( set ) );
        assertEquals(
                set, set2
        );


        set2 = safeSet( enumeration( set ) );
        assertEquals(
                set, set2
        );

        set2 = safeSortedSet( enumeration( set ) );
        assertEquals(
                set, set2
        );

        Set<String> set3 = set( ( Iterable ) set2 );
        assertEquals(
                set2, set3
        );

        set3 = sortedSet( ( Iterable ) set2 );
        assertEquals(
                set2, set3
        );


        set3 = safeSortedSet( ( Iterable ) set2 );
        assertEquals(
                set2, set3
        );


        set3 = safeSet( ( Iterable ) set2 );
        assertEquals(
                set2, set3
        );


        Set<String> set4 = set( ( Collection ) set3 );
        assertEquals(
                set3, set4
        );


        set4 = safeSet( ( Collection ) set3 );
        assertEquals(
                set3, set4
        );


        set4 = safeSortedSet( ( Collection ) set3 );
        assertEquals(
                set3, set4
        );

        set4 = sortedSet( ( Collection ) set3 );
        assertEquals(
                set3, set4
        );

        Set<String> set5 = set( set4.iterator() );
        assertEquals(
                set4, set5
        );


        set5 = sortedSet( set4.iterator() );
        assertEquals(
                set4, set5
        );


        set5 = safeSortedSet( set4.iterator() );
        assertEquals(
                set4, set5
        );


        set5 = safeSet( set4.iterator() );
        assertEquals(
                set4, set5
        );

    }

    @Test
    public void creationalEquals() {

        assertTrue(

                sortedSet( "apple", "pear", "orange" ).equals(
                        set( "apple", "pear", "orange" ) ) &&
                        safeSet( "apple", "pear", "orange" ).equals(
                                safeSortedSet( "apple", "pear", "orange" ) ) &&
                        sortedSet( "apple", "pear", "orange" ).equals(
                                safeSortedSet( "apple", "pear", "orange" ) )

        );

    }

    @Test
    public void enumerationTest() {
        Set<String> set = set( "apple", "grape", "pears" );
        Set<String> set2 = set( enumeration( set ) );
        assertEquals(
                set, set2
        );
    }

    Class<String> string = String.class;


    private void simpleOperations( Set<String> set ) {

        add( set, "apple" );

        assertTrue(

                len( set ) == 1

        );


        assertTrue(

                !( set instanceof SortedSet ) || idx( set, "a" ).equals( "apple" )

        );


        Set<String> set2 = copy( set );
        assertTrue(

                !( set2 instanceof SortedSet ) || idx( set2, "a" ).equals( "apple" )

        );

        assertTrue(

                len( set2 ) == 1

        );


    }


    @Test
    public void simpleOperationsSortedSet() {

        simpleOperations( sortedSet( string ) );
    }

    @Test
    public void simpleOperationsSet() {

        simpleOperations( set( string ) );

    }

    @Test
    public void simpleOperationsSafeSet() {

        simpleOperations( safeSet( string ) );

    }

    @Test
    public void simpleOperationsSafeSortedSet() {

        simpleOperations( safeSortedSet( string ) );

    }

}