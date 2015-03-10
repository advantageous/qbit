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

import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import io.advantageous.boon.utils.DateUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Maps.idx;
import static io.advantageous.boon.Str.lines;

/**
 * Created by rick on 12/12/13.
 *
 * Make sure it can handle these
 * https://code.google.com/p/json-smart/wiki/FeaturesTests
 */
public class JsonLaxTest extends JsonParserAndMapperBaseTest {


    public JsonParserAndMapper parser () {
        return new JsonParserFactory().setLazyChop( true ).createLaxParser();
    }

    public JsonParserAndMapper objectParser () {
        return parser();
    }



    @Test
    public void simpleFloat () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"v\":1.1}"
                )
        );


        Object o = map.get ( "v" );

        if (o instanceof BigDecimal ) {
            o = ((BigDecimal) o).doubleValue();
        }

        boolean ok = o.equals ( 1.1 ) || die ( "map " + map.get ( "v" ) );
    }

    @Test
    public void testLax () {

        Object obj = jsonParserAndMapper.parse ( Map.class,
                " {foo: hi mom hi dad how are you? }  "
        );

        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" + obj );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        System.out.println ( obj );

        System.out.println ( idx ( map, "foo" ) );

        inspectMap ( map );

        ok &= idx ( map, "foo" ).equals ( "hi mom hi dad how are you?" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );


    }

    @Test
    public void testComment () {

        String testString = " {foo:\"bar\", //hi mom \n" +
                " foo2:baz }  ";

        Map<String, Object> map = jsonParserAndMapper.parse ( Map.class, testString );

        //uts ( "map = " + map );

        inspectMap ( map );

        boolean ok = idx ( map, "foo" ).equals ( "bar" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );
        ok = idx ( map, "foo2" ).equals ( "baz" ) || die ( "I did not find:" + idx ( map, "foo2" ) + "#" );


    }

    @Test
    public void testComment2 () {

        String testString = " {foo:bar, #hi mom \n" +
                " foo2:baz }  ";

        Map<String, Object> map = jsonParserAndMapper.parse ( Map.class, testString );

        //uts ( "map = " + map );
        inspectMap ( map );

        boolean ok = idx ( map, "foo" ).equals ( "bar" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );
        ok = idx ( map, "foo2" ).equals ( "baz" ) || die ( "I did not find:" + idx ( map, "foo2" ) + "#" );


    }

    @Test
    public void testComment3 () {

        String testString = " {foo:bar, /* hi mom */" +
                " foo2:baz }  ";

        Map<String, Object> map = jsonParserAndMapper.parse ( Map.class, testString );

        //uts ( "map = " + map );

        boolean ok = idx ( map, "foo" ).equals ( "bar" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );
        ok = idx ( map, "foo2" ).equals ( "baz" ) || die ( "I did not find:" + idx ( map, "foo2" ) + "#" );


    }

    @Test
    public void testLax2 () {

        String testString = " {foo: hi mom hi dad how are you?,\n" +
                "thanks:I am good thanks for asking,\t\n" +
                "list:[love, rocket, fire],\t" +
                " num:1, " +
                "mix: [ true, false, 1, 2, blue, true\n,\t,false\t,foo\n,], }  ";
        Object obj = jsonParserAndMapper.parse ( Map.class, testString
        );

        //uts ( testString );


        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        //uts ( map );

        inspectMap ( map );


        ok &= idx ( map, "foo" ).equals ( "hi mom hi dad how are you?" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );
        ok &= idx ( map, "thanks" ).equals ( "I am good thanks for asking" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );


        List<Object> list = ( List<Object> ) idx ( map, "list" );


        ok &= Lists.idx ( list, 0 ).equals ( "love" ) || die ( "I did not find love:" + Lists.idx ( list, 0 ) );


        ok &= Lists.idx ( list, 1 ).equals ( "rocket" ) || die ( "I did not find rocket:" + Lists.idx ( list, 1 ) );

        ok &= Lists.idx ( list, 2 ).equals ( "fire" ) || die ( "I did not find fire:" + Lists.idx ( list, 2 ) );

        ok &= idx ( map, "num" ).equals ( 1 ) || die ( "I did not find 1:" + idx ( map, "num" ) + "#" );


        List<Object> mix = ( List<Object> ) idx ( map, "mix" );


        ok &= Lists.idx ( mix, 0 ).equals ( true ) || die ( "I did not find true:" + Lists.idx ( mix, 0 ) );
        ok &= Lists.idx ( mix, 1 ).equals ( false ) || die ( "I did not find false:" + Lists.idx ( mix, 1 ) );


        ok &= Lists.idx ( mix, 2 ).equals ( 1 ) || die ( "I did not find 1:" + Lists.idx ( mix, 2 ) );


        ok &= Lists.idx ( mix, 4 ).equals ( "blue" ) || die ( "I did not find blue:" + Lists.idx ( mix, 3 ) );

        //uts ( "testLax2?", ok );

    }

    @Test
    public void testLax3 () {

        String testString = "/* in theory you can put a comment here. */ " +
                " {foo: hi mom hi dad how are you?, //here too\n" +
                "thanks:I am good thanks for asking, #I hear you can do it here\t\n" +
                "list:[love, rocket, fire],\t" +
                " num:1, " +
                "mix: [ true, false, 1, 2, blue, true\n,\t,false\t,foo\n,], " +
                "date: \"1994-11-05T08:15:30Z\" } ";
        Object obj = jsonParserAndMapper.parse ( Map.class, testString
        );

        //uts ( testString );


        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        //uts ( map );


        String dateGMTString = DateUtils.getGMTString((Date) idx(map, "date"));
        ok &= dateGMTString.equals ( "05/11/94 08:15" ) || die ( "I did not find:" + dateGMTString + "#" );

        ok &= idx ( map, "foo" ).equals ( "hi mom hi dad how are you?" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );
        ok &= idx ( map, "thanks" ).equals ( "I am good thanks for asking" ) || die ( "I did not find:" + idx ( map, "foo" ) + "#" );


        List<Object> list = ( List<Object> ) idx ( map, "list" );


        ok &= Lists.idx ( list, 0 ).equals ( "love" ) || die ( "I did not find love:" + Lists.idx ( list, 0 ) );


        ok &= Lists.idx ( list, 1 ).equals ( "rocket" ) || die ( "I did not find rocket:" + Lists.idx ( list, 1 ) );

        ok &= Lists.idx ( list, 2 ).equals ( "fire" ) || die ( "I did not find fire:" + Lists.idx ( list, 2 ) );

        ok &= idx ( map, "num" ).equals ( 1 ) || die ( "I did not find 1:" + idx ( map, "num" ) + "#" );


        List<Object> mix = ( List<Object> ) idx ( map, "mix" );


        ok &= Lists.idx ( mix, 0 ).equals ( true ) || die ( "I did not find true:" + Lists.idx ( mix, 0 ) );
        ok &= Lists.idx ( mix, 1 ).equals ( false ) || die ( "I did not find false:" + Lists.idx ( mix, 1 ) );


        ok &= Lists.idx ( mix, 2 ).equals ( 1 ) || die ( "I did not find 1:" + Lists.idx ( mix, 2 ) );


        ok &= Lists.idx ( mix, 4 ).equals ( "blue" ) || die ( "I did not find blue:" + Lists.idx ( mix, 3 ) );

        //uts ( "testLax2?", ok );

    }

    @Test
    public void textInMiddleOfArray () {

        try {
            Object obj = jsonParserAndMapper.parse ( Map.class,
                    lines ( "[A, 0]"
                    ).replace ( '\'', '"' ).getBytes ( StandardCharsets.UTF_8 )
            );

        } catch ( Exception ex ) {
            //success
            return;
        }

    }


    @Test ()
    public void testBooleanParseError () {

        Object obj = jsonParserAndMapper.parse ( Map.class,
                "  tbone  "
        );

    }


    @Test ()
    public void doubleQuoteInsideOfSingleQuote () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":'ab\"c'}"
                )
        );

    }

    @Test ()
    public void supportSimpleQuoteInNonProtectedStringValue () {

        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":It's'Work}"
                )
        );
    }

    @Test ()
    public void supportNonProtectedStrings () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ a:1234}"
                )
        );

    }

    @Test ()
    public void crapInAnArray () {
        jsonParserAndMapper.parse (
                lines (

                        "[ a,bc]"
                )
        );

    }


    @Test ()
    public void randomStringAsValuesWithSpaces () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":s1 s2}"
                )
        );

    }


    @Test ()
    public void randomStringAsValuesWithSpaceAndMoreSpaces () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":s1 s2 }"
                )
        );

    }


    @Test ()
    public void singleQuotes () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ 'value':'string'}"
                )
        );

        boolean ok = idx ( map, "value" ).equals ( "string" ) || die ();


    }


    @Test
    public void testArrayOfArrayWithSimpleValuesValue1() {
            List list = (List) jsonParserAndMapper.parse("[,]");
    }



    @Test
    public void testArrayOfArrayWithSimpleValuesValue6() {
            List list = (List) jsonParserAndMapper.parse("[1, 2, [3, 4]");
    }

    @Test
    public void testArrayOfArrayWithSimpleValuesValue7() {
            List list = (List) jsonParserAndMapper.parse("[1, [2]");
    }


    @Test
    public void testArrayOfArrayWithSimpleValuesValue4() {
            List list = (List) jsonParserAndMapper.parse("[1,");
    }


    @Test
    public void testBackSlashEscaping2() {
        Object obj = null;

        boolean ok = false;

        obj =  parser().parse("{\"a\":\"\\\\\\\\\" }");

        ok = obj.equals ( Maps.map ( "a", "\\\\" ) ) || die ("" + obj);


        obj =  parser().parse("{\"a\":\"C:\\\\\\\"Documents and Settings\\\"\\\\\"}");

        Object obj2 = Maps.map("a", "C:\\\"Documents and Settings\"\\");

        ok = obj.equals ( obj2 ) || die ("" + obj);

        String str = "{\"a\":\"c:\\\\\\\\GROOVY5144\\\\\\\\\",\"y\":\"z\"}";

        obj =  parser().parse(str);

        obj2 = Maps.map("a","c:\\\\GROOVY5144\\\\", "y", "z");

        ok = obj.equals ( obj2 ) || die ("" + obj);

        str = "[\"c:\\\\\\\\GROOVY5144\\\\\\\\\",\"d\"]";

        obj = parser().parse( str );

        obj2 = Lists.list("c:\\\\GROOVY5144\\\\", "d");

        ok = obj.equals ( obj2 ) || die ("" + obj);

        str = "{\"a\":\"c:\\\\\\\"}";

        parser().parse( str );


    }



}
