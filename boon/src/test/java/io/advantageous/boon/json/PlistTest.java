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

package io.advantageous.boon.json;

import io.advantageous.boon.utils.DateUtils;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.advantageous.boon.Boon.putl;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Maps.idx;

/**
 * Created by rick on 12/13/13.
 */
public class PlistTest {


    protected void inspectMap ( Map<String, Object> map ) {
        final Set<Map.Entry<String, Object>> entrySet = map.entrySet ();
        putl ( "map", map, "size", map.size (), "keys", map.keySet (), "values", map.values () );

        for ( String key : map.keySet () ) {
            puts ( "key", "#" + key + "#" );
        }

        for ( Object value : map.values () ) {
            puts ( "value", "#" + value + "#" );
        }

    }


    public JsonParserAndMapper parser () {
        return new JsonParserFactory().createPlistParser();
    }

    public JsonParserAndMapper objectParser () {

        return new JsonParserFactory().createPlistParser();
    }

    @Test
    public void basic () {
        String testString = "{\n" +
                "                date=\"1994-11-05T08:15:30Z\";\n" +
                "                \"foo\" = \"bar\";\n" +
                "        Applications = {\n" +
                "                isSymLink = 1;\n" +
                "                symLink   = \"/var/stash/Application/\";\n" +
                "                owner     = root;\n" +
                "                permissions = {\n" +
                "                        root      = (read, write, execute); \n" +
                "                        \"other\" = (read, execute);\n" +
                "                };\n" +
                "                numberOfFilesIncluded = 31;\n" +
                "                date=\"1994-11-05T08:15:30Z\";//lovebucket\n" +
                "        };\n" +
                "        anotherComment=bar;\n" +
                "        Library = {\n" +
                "                isSymLink = 0;\n" +
                "                owner     = root;\n" +
                "                permissions = {\n" +
                "                        root      = (read, //read this \n" +
                "                                     write, # write this\n" +
                "                                    /* hi */ execute); \n" +
                "                        admin     = (read, write, execute); \n" +
                "                        \"<other>\" = (read, execute);\n" +
                "                };\n" +
                "                numberOfFilesIncluded = 23;\n" +
                "        };\n" +
                "        /* etc. */\n" +
                "}";

        Map<String, Object> map = parser ().parse ( Map.class, testString );

        boolean ok = map.size () == 5 || die ( "" + map.size () );

        Map<String, Object> applications = ( Map<String, Object> ) map.get ( "Applications" );

        String dateGMTString = DateUtils.getGMTString((Date) idx(map, "date"));
        ok &= dateGMTString.equals ( "05/11/94 08:15" ) || die ( "I did not find:" + dateGMTString + "#" );


        inspectMap ( map );
        int symlink = ( Integer ) applications.get ( "isSymLink" );
        ok = symlink == 1 || die ();

        Map<String, Object> library = ( Map<String, Object> ) map.get ( "Library" );
        symlink = ( Integer ) library.get ( "isSymLink" );
        ok = symlink == 0 || die ();

        int numberOfFilesIncluded = ( Integer ) library.get ( "numberOfFilesIncluded" );
        ok = numberOfFilesIncluded == 23 || die ();

        Map<String, Object> permissions2 = ( Map<String, Object> ) library.get ( "permissions" );
        ok = permissions2.get ( "root" ).toString ().equals ( "[read, write, execute]" ) || die ( "" + permissions2 );

    }


    @Test
    public void basic2 () {

        String testString = "{\n" +
                " a = {\n" +
                "    b = { b1=foo; b2=1; b3={}; b4=();};" +
                "    c = 31;\n" +
                "    d =\"1994-11-05T08:15:30Z\";" +
                " };" +
                " map2={" +


                "};\n" +
                "}";


        Map<String, Object> map = parser ().parse ( Map.class, testString );

        inspectMap ( map );
        inspectMap ( map );
        boolean ok = map.size () == 2 || die ();
        ok = map.containsKey ( "a" ) || die ();
        ok = !map.containsKey ( "b" ) || die ();
        ok = !map.containsKey ( "c" ) || die ();
        ok = !map.containsKey ( "d" ) || die ();

        Map<String, Object> a = ( Map<String, Object> ) map.get ( "a" );

        int c = ( int ) a.get ( "c" );
        ok = c == 31 || die ();

        Date d = ( Date ) a.get ( "d" );

        String dateGMTString = DateUtils.getGMTString(d);
        ok &= dateGMTString.equals ( "05/11/94 08:15" ) || die ( "I did not find:" + dateGMTString + "#" );


        Map<String, Object> b = ( Map<String, Object> ) a.get ( "b" );
        String b1 = ( String ) b.get ( "b1" );
        int b2 = ( int ) b.get ( "b2" );

        ok = b1.equals ( "foo" ) || die ( "" + b1 );

        Map<String, Object> b3 = ( Map<String, Object> ) b.get ( "b3" );


        ok = b3.toString ().equals ( "{}" ) || die ( "" + b3 );


        List<Object> b4 = ( List<Object> ) b.get ( "b4" );

        ok = b4.toString ().equals ( "[]" ) || die ( "" + b4 );

        Map<String, Object> map2 = ( Map<String, Object> ) map.get ( "map2" );


    }


    @Test
    public void basic3 () {
//                "  b = { b1 = (read, write); \n b2 = (execute);\n };\n" +

        String testString = "{\n" +
                " a = {\n" +
                "    b = {      b1=foo; \n" +
                "               b2=1; \n" +
                "               b3={};\n " +
                "               b4=();\n" +
                "    };" +
                "    c = 31;\n" +
                "    d =\"1994-11-05T08:15:30Z\";" +
                " };\n" +
                "}";


        Map<String, Object> map = parser ().parse ( Map.class, testString );
        inspectMap ( map );
        boolean ok = map.size () == 1 || die ();
        ok = map.containsKey ( "a" ) || die ();
        ok = !map.containsKey ( "b" ) || die ();
        ok = !map.containsKey ( "c" ) || die ();
        ok = !map.containsKey ( "d" ) || die ();

        Map<String, Object> a = ( Map<String, Object> ) map.get ( "a" );

        int c = ( int ) a.get ( "c" );
        ok = c == 31 || die (""+c);

        Date d = ( Date ) a.get ( "d" );
        String dateGMTString = DateUtils.getGMTString(d);
        ok &= dateGMTString.equals ( "05/11/94 08:15" ) || die ( "I did not find:" + dateGMTString + "#" );


        Map<String, Object> b = ( Map<String, Object> ) a.get ( "b" );
        String b1 = ( String ) b.get ( "b1" );
        int b2 = ( int ) b.get ( "b2" );

        ok = b1.equals ( "foo" ) || die ( "" + b1 );

        Map<String, Object> b3 = ( Map<String, Object> ) b.get ( "b3" );


        ok = b3.toString ().equals ( "{}" ) || die ( "" + b3 );


        List<Object> b4 = ( List<Object> ) b.get ( "b4" );

        ok = b4.toString ().equals ( "[]" ) || die ( "" + b4 );

    }

}
