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


import org.junit.Test;

import static io.advantageous.boon.primitive.Arry.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArryTest {


    @Test
    public void allocate() {

        String[] fruit =
                array( String.class, 500 );

        assertEquals(
                500,
                len( fruit )
        );

    }

    @Test
    public void addArrayTest() {

        String[] fruit =
                array( "apple", "oranges" );


        String[] veggies =
                array( "green beans", "broccoli" );

        String[] food = add( fruit, veggies );


        assertEquals(
                4,
                len( food )
        );


        assertEquals(
                "apple",
                idx( food, 0 )
        );

        assertEquals(
                "oranges",
                idx( food, 1 )
        );

        assertEquals(
                "green beans",
                idx( food, 2 )
        );


        assertEquals(
                "broccoli",
                idx( food, 3 )
        );

    }

    @Test
    public void shrinkTest() {

        String[] fruit =
                array( "apple", "oranges", null, "grapes", "kiwi" );

        fruit = shrink( fruit, 3 );

        assertEquals(
                2,
                len( fruit )
        );


        assertEquals(
                "apple",
                idx( fruit, 0 )
        );

        assertEquals(
                "oranges",
                idx( fruit, 1 )
        );
    }

    @Test
    public void growTest() {

        String[] fruit =
                array( "apple", "oranges", null, "grapes", "kiwi" );

        fruit = grow( fruit, 2 * fruit.length );

        assertEquals(
                15,
                len( fruit )
        );


        assertEquals(
                "apple",
                idx( fruit, 0 )
        );

        assertEquals(
                "oranges",
                idx( fruit, 1 )
        );


        assertEquals(
                "grapes",
                idx( fruit, 3 )
        );


        assertEquals(
                "kiwi",
                idx( fruit, 4 )
        );


        assertEquals(
                null,
                idx( fruit, 5 )
        );


        assertEquals(
                null,
                idx( fruit, 14 )
        );

    }


    @Test
    public void compactTest() {

        String[] fruit =
                array( "apple", "oranges", null, "grapes", "kiwi" );

        fruit = compact( fruit );

        assertEquals(
                4,
                len( fruit )
        );


        assertEquals(
                "apple",
                idx( fruit, 0 )
        );

        assertEquals(
                "oranges",
                idx( fruit, 1 )
        );


        assertEquals(
                "grapes",
                idx( fruit, 2 )
        );


        assertEquals(
                "kiwi",
                idx( fruit, 3 )
        );


    }

    @Test
    public void insertTest() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );

        fruit = insert( fruit, 1, "bacon" );


        assertEquals(
                6,
                len( fruit )
        );


        assertEquals(
                "bacon",
                idx( fruit, 1 )
        );

        assertEquals(
                "apple",
                idx( fruit, 0 )
        );


        assertEquals(
                "oranges",
                idx( fruit, 2 )
        );


    }

    @Test
    public void insertTest2() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );

        fruit = insert( fruit, 0, "bacon" );


        assertEquals(
                6,
                len( fruit )
        );


        assertEquals(
                "bacon",
                idx( fruit, 0 )
        );

        assertEquals(
                "apple",
                idx( fruit, 1 )
        );


        assertEquals(
                "oranges",
                idx( fruit, 2 )
        );


    }

    @Test
    public void insertTest3() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );

        fruit = insert( fruit, fruit.length - 1, "bacon" );


        assertEquals(
                6,
                len( fruit )
        );


        assertEquals(
                "apple",
                idx( fruit, 0 )
        );

        assertEquals(
                "oranges",
                idx( fruit, 1 )
        );


        assertEquals(
                "pears",
                idx( fruit, 2 )
        );

        assertEquals(
                "grapes",
                idx( fruit, 3 )
        );

        assertEquals(
                "bacon",
                idx( fruit, 4 )
        );


        assertEquals(
                "kiwi",
                idx( fruit, 5 )
        );


    }

    @Test
    public void addTest() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );

        fruit = add( fruit, "bacon" );


        assertEquals(
                6,
                len( fruit )
        );

        assertEquals(
                "kiwi",
                idx( fruit, -2 )
        );


        assertEquals(
                "bacon",
                idx( fruit, 5 )
        );

        assertEquals(
                "apple",
                idx( fruit, 0 )
        );


        assertEquals(
                "pears",
                idx( fruit, 2 )
        );


    }

    @Test
    public void basicTest() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );


        assertArrayEquals(
                fruit,
                copy( fruit )
        );

        assertTrue(
                in( "apple", fruit )
        );


        assertEquals(
                5,
                len( fruit )
        );


        assertEquals(
                "pears",
                idx( fruit, 2 )
        );

        idx( fruit, 2, "bacon" );

        assertEquals(
                "bacon",
                idx( fruit, 2 )
        );


    }

    @Test
    public void sliceTest() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );

        String[] array2 = slc( fruit, 0, 2 );

        assertArrayEquals(
                array( "apple", "oranges" ),
                array2
        );


        array2 = slc( fruit, 1 );

        assertArrayEquals(
                array( "oranges", "pears", "grapes", "kiwi" ),
                array2
        );

        String[] array3 = slc( fruit, -3, -1 );

        assertArrayEquals(
                array( "pears", "grapes" ),
                array3
        );

        String[] array4 = slc( fruit, -3 );

        assertArrayEquals(
                array( "pears", "grapes", "kiwi" ),
                array4
        );


        String[] array5 = slcEnd( fruit, -3 );

        assertArrayEquals(
                array( "apple", "oranges" ),
                array5
        );

    }


    @Test
    public void outOfBounds() {

        String[] fruit =
                array( "apple", "oranges", "pears", "grapes", "kiwi" );

        slcEnd( fruit, 100 );
        slcEnd( fruit, -100 );

        slc( fruit, 100 );
        slc( fruit, -100 );
        idx( fruit, 100 );
        idx( fruit, -100 );


        idx( fruit, 100, "bar" );


        assertEquals(
                "bar",
                idx( fruit, -1 )
        );

    }


    @Test
    public void addInsertSingle() {

        assertArrayEquals(
                array( 'a', 'b', 'c', 'd', 'e', 'f', 'g' ),
                insert( array( 'a', 'b', 'd', 'e', 'f', 'g' ), 2, 'c' )

        );

        assertArrayEquals(
                array( 'a', 'b', 'c', 'd', 'e', 'f', 'g' ),
                insert( array( 'b', 'c', 'd', 'e', 'f', 'g' ), 0, 'a' )

        );

        assertArrayEquals(
                array( 'a', 'b', 'c', 'd', 'e', 'f', 'g' ),
                insert( array( 'a', 'b', 'c', 'd', 'e', 'f' ), 6, 'g' )

        );

    }


}