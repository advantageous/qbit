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

import org.junit.After;
import org.junit.Test;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Str.lines;

public class JsonParserUsingCharacterSourceTest extends JsonParserAndMapperBaseTest {

    JsonParserAndMapper jsonCharSrcParser;

    public JsonParserAndMapper parser () {
        jsonCharSrcParser =  new JsonParserFactory( ).createCharacterSourceParser();
        return  jsonCharSrcParser;
    }


    public JsonParserAndMapper objectParser () {
        return parser();
    }

    @After
    public void after () {
        jsonCharSrcParser.close ();
    }



    @Test
    public void simpleStringReader () {

        String str  = (String) jsonParserAndMapper.parse (
                new StringReader ("\"file\"")
        );

        boolean ok = str.equals ( "file" ) || die ( str );


    }


    @Test
    public void simpleFalseFromReader () {

        Boolean v  = (Boolean) jsonParserAndMapper.parse (
                new StringReader ("false")
        );

        boolean ok = v.equals ( Boolean.FALSE ) || die ( "" + v );


    }


    @Test
    public void simpleNumber () {

        int i  = (int) jsonParserAndMapper.parse (
                new StringReader ("123")
        );

        boolean ok = i == 123 || die ( "" + i );


    }

    @Test
    public void simpleFloat2 () {

        double f  = (double) jsonParserAndMapper.parse (
                new StringReader ("1.1")
        );

        boolean ok = f == 1.1d || die ( "" + f );


    }


    @Test
    public void simpleListFromReader () {

        List v  = (List ) jsonParserAndMapper.parse (
                new StringReader ("[1,2,3]")
        );

        boolean ok = list(1,2,3).equals ( v ) || die ( "" + v );


    }

    @Test
    public void simpleStringListFromReader () {

        List v  = (List ) jsonParserAndMapper.parse (
                new StringReader ("[\"abc\",\"\",3]")
        );

        String empty = ( String ) v.get(1);
        v.remove ( 1 );

        boolean ok = list("abc",3).equals ( v ) || die ( "" + v );

        ok = "".equals ( empty ) || die ( "" + empty);

    }

    @Test
    public void simpleFloat () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                new StringReader (lines (

                        "{ \"v\":1.1}"
                ))
        );


        outputs( "map", map );
        Object o = map.get ( "v" );

        if (o instanceof BigDecimal ) {
            o = ((BigDecimal) o).doubleValue();
        }

        boolean ok = o.equals ( 1.1 ) || die ( "map " + map.get ( "v" ) );
    }

    private void outputs( Object... args ) {
    }


    @Test
    public void readBug() {

        String test = "{" +
                "        \"138586365\": {\n" +
                "            \"description\": null,\n" +
                "            \"id\": 138586365,\n" +
                "            \"logo\": \"/images/UE0AAAAACEKo/QAAAAVDSVRN\",\n" +
                "            \"name\": \"Alessandro - G.F. Haendel\",\n" +
                "            \"subTopicIds\": [\n" +
                "                123456789,\n" +
                "                987654321,\n" +
                "                333333333,\n" +
                "                444444444,\n" +
                "                555555555\n" +
                "            ],\n" +
                "            \"subjectCode\": null,\n" +
                "            \"subtitle\": null,\n" +
                "            \"topicIds\": [\n" +
                "                324846099,\n" +
                "                107888604,\n" +
                "                324846100\n" +
                "            ]\n" +
                "        }" +
                "}";

        jsonParserAndMapper.parse (
                new StringReader (test)
        );



    }



    @Test (expected = JsonException.class)
    public void readBug2() {

        try {

        String test = "{" +
                "        \"138586365\": {\n" +
                "            \"description\": null,\n" +
                "            \"id\": 138586365,\n" +
                "            \"logo\": \"/images/UE0AAAAACEKo/QAAAAVDSVRN\",\n" +
                "            \"name\": \"Alessandro - G.F. Haendel\",\n" +
                "            \"subTopicIds\": [\n" +
                "                123456789,\n" +
                "                987654321,\n" +
                "                333333333,\n" +
                "                444444444,\n" +
                "                555555555\n" +
                "            ],\n" +
                "            \"subjectCode\": null,\n" +
                "            \"subjectCode\": trau,\n" +
                "            \"subtitle\": null,\n" +
                "            \"topicIds\": [\n" +
                "                324846099,\n" +
                "                107888604,\n" +
                "                324846100\n" +
                "            ]\n" +
                "        }" +
                "}";

        jsonParserAndMapper.parse (
                new StringReader (test)
        );

        } catch (Exception ex) {
            //uts (ex.getMessage ());
            throw ex;
        }



    }


}