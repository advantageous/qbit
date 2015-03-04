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


import io.advantageous.boon.*;
import org.junit.Test;

import java.util.Arrays;


import java.util.*;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Maps.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings ( "unchecked" )
public class MapsTest {


    @Test
    public void prettyPrintMap() {
        Map<String, Object> map = Maps.map(
                "name", (Object) "Rick",
                "age", 45,
                "wife", Maps.map("name", "Diana"),
                "children", Lists.list(
                        Maps.map("name", "Whitney"),
                        Maps.map("name", "Maya"),
                        Maps.map("name", "Lucas"),
                        Maps.map("name", "Ryan"),
                        Maps.map("name", "Noah")
                ),
                "fruit", Lists.list("apple", "orange", "strawberry")
        );


        puts(Maps.asPrettyJsonString(map));
        puts(Boon.toPrettyJson(map));

        final Object o = Boon.fromJson(Boon.toPrettyJson(map));

        Boon.equalsOrDie("Values are equal", map, o);


    }
    class Dog {
        String name = "dog";

        Dog( String name ) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Dog{\"name\":\"" + name + "\"}";
        }
    }


    Dog dog1 = new Dog( "Spot" );
    Dog dog2 = new Dog( "Fido" );

    final List<Dog> dogs = Lists.list( dog1, dog2 );



    @Test
    public void testToMap() {


        final Map<String, Dog> dogMap = Maps.toMap( "name", dogs );

        validateForToMap( dogMap );

    }

    @Test
    public void testToSafeMap() {


        final Map<String, Dog> dogMap = Maps.toSafeMap( "name", dogs );

        validateForToMap( dogMap );

    }

    @Test
    public void testToSortedMap() {


        final Map<String, Dog> dogMap = Maps.toSortedMap( "name", dogs );

        validateForToMap( dogMap );

    }

    @Test
    public void testToSafeSortedMap() {


        final Map<String, Dog> dogMap = Maps.toSafeSortedMap( "name", dogs );

        validateForToMap( dogMap );

    }

    private void validateForToMap( Map<String, Dog> dogMap ) {
        boolean ok = true;

        ok &= dogMap.size() == 2 || die( "should be 2 and was " + len( dogMap ) );

        ok &= dogMap.get( "Fido" ).name.equals( "Fido" ) || die( "No Fido" );

        ok &= dogMap.get( "Spot" ).name.equals( "Spot" ) || die( "No Spot" );

        System.out.println( ok );

    }


    @Test
    public void testEntry() {

        Dog dog = new Dog( "dog" );
        Entry<String, Dog> entry = entry( "dog", dog );
        assertEquals( "dog", entry.key() );
        assertEquals( dog, entry.value() );

        assertTrue( entry.equals( entry ) );

        assertTrue( entry.equals( ( Object ) entry ) );


        Entry<String, Dog> entry4 = entry( "dog4", new Dog( "dog4" ) );
        assertFalse( entry.equals( ( Object ) entry4 ) );


        assertTrue( entry.hashCode()
                == ( new Pair( entry ).hashCode() ) );

        assertEquals( "{\"k\":dog, \"v\":Dog{\"name\":\"dog\"}}",
                entry.toString() );

        new Pair();
    }

    @Test
    public void testUniversal() {

        Dog dog = new Dog( "dog" );
        Map<String, Dog> dogMap = map( "dog", dog );
        assertEquals( "dog", dogMap.get( "dog" ).name );

        assertEquals( true, in( "dog", dogMap ) );
        assertEquals( 1, len( dogMap ) );
        assertEquals( dog, idx( dogMap, "dog" ) );


        Dog fido = new Dog( "fido" );
        add( dogMap, entry( "fido", fido ) );
        assertEquals( 2, len( dogMap ) );
        assertEquals( true, valueIn( fido, dogMap ) );

        Map<String, Dog> dogMap2 = copy( dogMap );

        assertEquals( dogMap.hashCode(), dogMap2.hashCode() );


        SortedMap<String, Dog> dogMapT = sortedMap( "dog", new Dog( "dog" ) );
        SortedMap<String, Dog> dogMapT2 = copy( dogMapT );
        assertEquals( dogMapT.hashCode(), dogMapT2.hashCode() );

        idx( dogMap, "foo", new Dog( "foo" ) );
        assertEquals( "foo", idx( dogMap, "foo" ).name );


    }


    @Test
    public void testHashMap() {
        Map<String, Dog> dogMap = Maps.map( "dog", new Dog( "dog" ) );
        assertEquals( "dog", dogMap.get( "dog" ).name );


        dogMap = Maps.map(
                new String[]{ "dog0", "dog1", "dog2" },
                new Dog[]{ new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) }
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = Maps.map(
                Lists.list( new String[]{ "dog0", "dog1", "dog2" } ),
                Lists.list( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.map(
                ( Iterable ) Lists.list( "dog0", "dog1", "dog2" ),
                ( Iterable ) Lists.list( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = Maps.map( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" )
        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );

        dogMap = Maps.map( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.map(
                entry( "dog0", new Dog( "dog0" ) ),
                entry( "dog1", new Dog( "dog1" ) ),
                entry( "dog2", new Dog( "dog2" ) )

        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.map( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );


        dogMap = Maps.map(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );


        dogMap = Maps.map(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );


        dogMap = Maps.map(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );

        dogMap = Maps.map(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );


        dogMap = Maps.map(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );


        dogMap = Maps.map(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" ),
                "dog9", new Dog( "dog9" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );
        assertEquals( "dog9", dogMap.get( "dog9" ).name );

        assertEquals( 10, len( dogMap ) );

    }


    @Test
    public void testTreeMap() {
        Map<String, Dog> dogMap = sortedMap( "dog", new Dog( "dog" ) );
        assertEquals( "dog", dogMap.get( "dog" ).name );


        dogMap = sortedMap(
                java.util.Arrays.asList( new String[]{ "dog0", "dog1", "dog2" } ),
                Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap(
                ( Iterable ) Arrays.asList( "dog0", "dog1", "dog2" ),
                ( Iterable ) Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap( list(
                entry( "dog0", new Dog( "dog0" ) ),
                entry( "dog1", new Dog( "dog1" ) ),
                entry( "dog2", new Dog( "dog2" ) ) )

        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = sortedMap(
                new String[]{ "dog0", "dog1", "dog2" },
                new Dog[]{ new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) }
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" )
        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );

        dogMap = sortedMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );


        dogMap = sortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );


        dogMap = sortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );


        dogMap = sortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );

        dogMap = sortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );


        dogMap = sortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );


        dogMap = sortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" ),
                "dog9", new Dog( "dog9" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );
        assertEquals( "dog9", dogMap.get( "dog9" ).name );

    }

    @Test
    public void testComparator() {

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare( String o1, String o2 ) {
                return o1.toString().compareTo( o2.toString() );
            }
        };


        Map<String, Dog> dogMap = sortedMap( comparator, "dog", new Dog( "dog" ) );
        assertEquals( "dog", dogMap.get( "dog" ).name );


        dogMap = sortedMap( comparator,
                Arrays.asList( new String[]{ "dog0", "dog1", "dog2" } ),
                Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap( comparator, list(
                entry( "dog0", new Dog( "dog0" ) ),
                entry( "dog1", new Dog( "dog1" ) ),
                entry( "dog2", new Dog( "dog2" ) ) )

        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = sortedMap( comparator,
                new String[]{ "dog0", "dog1", "dog2" },
                new Dog[]{ new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) }
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap( comparator, "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" )
        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );

        dogMap = sortedMap( comparator, "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = sortedMap( comparator, "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );


        dogMap = sortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );


        dogMap = sortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );


        dogMap = sortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );

        dogMap = sortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );


        dogMap = sortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );


        dogMap = sortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" ),
                "dog9", new Dog( "dog9" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );
        assertEquals( "dog9", dogMap.get( "dog9" ).name );

    }


    @Test
    public void testSafeMap() {

        Map<String, Dog> dogMap = Maps.safeMap( "dog", new Dog( "dog" ) );
        assertEquals( "dog", dogMap.get( "dog" ).name );


        dogMap = Maps.safeMap(
                new String[]{ "dog0", "dog1", "dog2" },
                new Dog[]{ new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) }
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = Maps.safeMap(
                Arrays.asList( new String[]{ "dog0", "dog1", "dog2" } ),
                Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.safeMap(
                ( Iterable ) Arrays.asList( "dog0", "dog1", "dog2" ),
                ( Iterable ) Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = Maps.safeMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" )
        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );

        dogMap = Maps.safeMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.safeMap(
                entry( "dog0", new Dog( "dog0" ) ),
                entry( "dog1", new Dog( "dog1" ) ),
                entry( "dog2", new Dog( "dog2" ) )

        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.safeMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );


        dogMap = Maps.safeMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );


        dogMap = Maps.safeMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );


        dogMap = Maps.safeMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );

        dogMap = Maps.safeMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );


        dogMap = Maps.safeMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );


        dogMap = Maps.safeMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" ),
                "dog9", new Dog( "dog9" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );
        assertEquals( "dog9", dogMap.get( "dog9" ).name );

        assertEquals( 10, len( dogMap ) );

    }


    @Test
    public void testComparatorSkipMap() {

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare( String o1, String o2 ) {
                return o1.toString().compareTo( o2.toString() );
            }
        };


        Map<String, Dog> dogMap = safeSortedMap( comparator, "dog", new Dog( "dog" ) );
        assertEquals( "dog", dogMap.get( "dog" ).name );


        dogMap = safeSortedMap( comparator,
                Arrays.asList( new String[]{ "dog0", "dog1", "dog2" } ),
                Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = safeSortedMap( comparator, list(
                entry( "dog0", new Dog( "dog0" ) ),
                entry( "dog1", new Dog( "dog1" ) ),
                entry( "dog2", new Dog( "dog2" ) ) )

        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = safeSortedMap( comparator,
                new String[]{ "dog0", "dog1", "dog2" },
                new Dog[]{ new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) }
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = safeSortedMap( comparator, "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" )
        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );

        dogMap = safeSortedMap( comparator, "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = safeSortedMap( comparator, "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );


        dogMap = safeSortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );


        dogMap = safeSortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );


        dogMap = safeSortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );

        dogMap = safeSortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );


        dogMap = safeSortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );


        dogMap = safeSortedMap( comparator,
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" ),
                "dog9", new Dog( "dog9" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );
        assertEquals( "dog9", dogMap.get( "dog9" ).name );

    }


    @Test
    public void testSkipMap() {

        Map<String, Dog> dogMap = Maps.safeSortedMap( "dog", new Dog( "dog" ) );
        assertEquals( "dog", dogMap.get( "dog" ).name );


        dogMap = Maps.safeSortedMap(
                new String[]{ "dog0", "dog1", "dog2" },
                new Dog[]{ new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) }
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = Maps.safeSortedMap(
                Arrays.asList( new String[]{ "dog0", "dog1", "dog2" } ),
                Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.safeSortedMap(
                ( Iterable ) Arrays.asList( "dog0", "dog1", "dog2" ),
                ( Iterable ) Arrays.asList( new Dog( "dog0" ),
                        new Dog( "dog1" ), new Dog( "dog2" ) )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );

        dogMap = Maps.safeSortedMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" )
        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );

        dogMap = Maps.safeSortedMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.safeSortedMap(
                entry( "dog0", new Dog( "dog0" ) ),
                entry( "dog1", new Dog( "dog1" ) ),
                entry( "dog2", new Dog( "dog2" ) )

        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );


        dogMap = Maps.safeSortedMap( "dog", new Dog( "dog" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" )

        );
        assertEquals( "dog", dogMap.get( "dog" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );


        dogMap = Maps.safeSortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );


        dogMap = Maps.safeSortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );


        dogMap = Maps.safeSortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );

        dogMap = Maps.safeSortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );


        dogMap = Maps.safeSortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );


        dogMap = Maps.safeSortedMap(
                "dog0", new Dog( "dog0" ),
                "dog1", new Dog( "dog1" ),
                "dog2", new Dog( "dog2" ),
                "dog3", new Dog( "dog3" ),
                "dog4", new Dog( "dog4" ),
                "dog5", new Dog( "dog5" ),
                "dog6", new Dog( "dog6" ),
                "dog7", new Dog( "dog7" ),
                "dog8", new Dog( "dog8" ),
                "dog9", new Dog( "dog9" )
        );
        assertEquals( "dog0", dogMap.get( "dog0" ).name );
        assertEquals( "dog1", dogMap.get( "dog1" ).name );
        assertEquals( "dog2", dogMap.get( "dog2" ).name );
        assertEquals( "dog3", dogMap.get( "dog3" ).name );
        assertEquals( "dog4", dogMap.get( "dog4" ).name );
        assertEquals( "dog5", dogMap.get( "dog5" ).name );
        assertEquals( "dog6", dogMap.get( "dog6" ).name );
        assertEquals( "dog7", dogMap.get( "dog7" ).name );
        assertEquals( "dog8", dogMap.get( "dog8" ).name );
        assertEquals( "dog9", dogMap.get( "dog9" ).name );

        assertEquals( 10, len( dogMap ) );

    }




    @Test
    public void testMapEquals() {


        final Map<String, Dog> dogMap = Maps.toMap( "name", dogs );

        HashMap<String, Dog> hashMap = new HashMap<>( dogMap );

        boolean ok = dogMap.equals( hashMap ) || die();

    }


}
