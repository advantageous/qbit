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

package io.advantageous.boon.core.value;

import io.advantageous.boon.IO;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;

public class LazyValueMapTest {

    int leafCount;
    int mapCount;
    int collectionCount;
    int integerCount;
    int longCount;
    int doubleCount;
    int stringCount;
    int dateCount;
    int nullCount;
    int listCount;
    int booleanCount;

    @Before
    public void setUp() throws Exception {


        leafCount = 0;
        mapCount = 0;
        collectionCount = 0;
        integerCount = 0;
        longCount = 0;
        doubleCount = 0;
        stringCount = 0;
        dateCount = 0;
        nullCount = 0;
        listCount = 0;
        booleanCount = 0;
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void test() {

        List<String> files  = IO.listByFileExtension ( "./files/", "json" );

        for ( String file : files) {
            //outputs ( file );

            Object object  =  new JsonParserFactory().createFastParser().parseFile ( Map.class, file.toString () );


            walkObject( object, null, null );

        }

        //outputs ( "leaf", leafCount, "map", mapCount, "collection", collectionCount );
        //outputs ( "integer", integerCount, "long", longCount, "double", doubleCount, "boolean", booleanCount );
        //outputs ( "string", stringCount, "date", dateCount, "null", nullCount );

    }


    @Test
    public void testGetWalk() {



        List<String> files  = IO.listByFileExtension ( "./files/", "json" );

        for ( String file : files) {
            //outputs ( file );

            JsonParserAndMapper parser = new  JsonParserFactory().createFastParser();

            Object object  = parser.parseFile ( Map.class, file.toString () );


            walkGetObject( object, null, null );

        }

        //outputs ( "leaf", leafCount, "map", mapCount, "list", listCount );
        //outputs ( "integer", integerCount, "long", longCount, "double", doubleCount );
        //outputs ( "string", stringCount, "date", dateCount, "null", nullCount );

    }

    private void walkMap( Map map ) {
        mapCount++;
        Set<Map.Entry<String, Object>> entries = map.entrySet ();

        for ( Map.Entry<String, Object> entry : entries ) {
            Object object = entry.getValue ();
            walkObject ( object, map, null );
        }

    }


    private void walkGetMap( Map map ) {
        mapCount++;
        Set<Map.Entry<String, Object>> entries = map.entrySet ();

        for ( Map.Entry<String, Object> entry : entries ) {
            //outputs (entry.getKey ());
            walkGetObject ( map.get ( entry.getKey () ), map, null );
        }

        map.size ();

    }
    private void walkObject( Object object, Map map, Object c ) {
        leafCount++;
        if ( object instanceof Value ) {
            die ( "Found a value" );
        } else if ( object instanceof Map ) {
            walkMap ( ( Map ) object );
        } else if ( object instanceof Collection ) {
            walkCollection ( ( Collection ) object );
        } else if ( object instanceof Long ) {
            longCount++;
        } else if ( object instanceof Integer ) {
            integerCount++;
        } else if ( object instanceof Double ) {
            doubleCount++;
        } else if ( object instanceof Boolean ) {
            booleanCount++;
        } else if ( object instanceof String ) {
            stringCount++;
        } else if ( object instanceof Date ) {
            dateCount++;
        } else if ( object == null ) {
            nullCount++;
        } else {
            die ( sputs ( object, object.getClass ().getName (), map, c ) );
        }
    }

    private void walkGetObject( Object object, Map map, List list ) {
        leafCount++;
        if ( object instanceof Value ) {
            die ( "Found a value" );
        } else if ( object instanceof Map ) {
            walkGetMap ( ( Map ) object );
        } else if ( object instanceof List ) {
            walkGetList ( ( List ) object );
        } else if ( object instanceof Long ) {
            longCount++;
        } else if ( object instanceof Integer ) {
            integerCount++;
        } else if ( object instanceof Boolean ) {
            booleanCount++;
        }  else if ( object instanceof Double ) {
            doubleCount++;
        } else if ( object instanceof String ) {
            stringCount++;
        } else if ( object instanceof Date ) {
            dateCount++;
        } else if ( object == null ) {
            nullCount++;
        } else {
            die ( sputs ( object, object.getClass ().getName (), map, list ) );
        }
    }



    private void walkGetList( List c ) {
        listCount++;
        for ( int index = 0; index < c.size (); index++ ) {
            walkGetObject ( c.get ( index ), null, c );
        }

        c.size();
    }

    private void walkCollection( Collection c ) {
        collectionCount++;
        for ( Object o : c ) {
            walkObject ( o, null, c );
        }
    }
}
