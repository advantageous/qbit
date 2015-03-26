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

import io.advantageous.boon.IO;
import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import io.advantageous.boon.Sets;
import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.core.Dates;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.json.serializers.CustomFieldSerializer;
import io.advantageous.boon.json.serializers.FieldFilter;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.json.serializers.impl.JsonSerializerImpl;
import io.advantageous.boon.json.test.AllTypes;
import io.advantageous.boon.json.test.Dog;
import io.advantageous.boon.json.test.FooEnum;
import io.advantageous.boon.primitive.CharBuf;
import io.advantageous.boon.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static io.advantageous.boon.Boon.putl;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Maps.idx;
import static io.advantageous.boon.Maps.map;
import static io.advantageous.boon.Str.lines;
import static org.junit.Assert.assertEquals;

public class JsonParserAndMapperBaseTest {


    JsonParserAndMapper jsonParserAndMapper;


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


    public JsonParserFactory factory () {
        return new JsonParserFactory ();
    }

    public JsonParserAndMapper parser () {
        return new JsonParserFactory().setLazyChop( true ).create();
    }

    public JsonParserAndMapper objectParser () {
        return factory ().create ();
    }


    @Before
    public void setup () {

        jsonParserAndMapper = parser ();



    }




    @Test
    public void classic() {

        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"nums\": [12, 12345678, 999.999, 123456789.99],\n " +
                                "    \"nums2\": [12, 12345678, 999.999, 123456789.99],\n" +
                                "    \"nums3\": [12, 12345678, 999.999, 123456789.99]\n" +
                                "}"
                )
        );

    }


    @Test
    public void bugReport179() {

        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"nums\": [-0, 12345678, 999.999, 123456789.99],\n " +
                                "    \"nums2\": [12, 12345678, 999.999, 123456789.99],\n" +
                                "    \"nums3\": [12, 12345678, 999.999, 123456789.99]\n" +
                                "}"
                )
        );

        final Object nums = map.get("nums");
        puts(nums);

        int i = -0;
        int b = 0;

        if (i == b) {
            puts("What?");
        }
    }


    @Test
    public void parseNegativeLong () {
        int i = jsonParserAndMapper.parseInt ( "123" );
        boolean ok = i == 123 || die ( "" + i );

        long l =  jsonParserAndMapper.parseLong ( "-123456789099" );
        ok = l == -123456789099L || die ( "" + l );

        puts ( ok );
    }



    @Test
    public void tesParseSmallNum() {
        long num = jsonParserAndMapper.parseLong( "" + Long.MIN_VALUE );
        long num2 = Long.MIN_VALUE;
        boolean ok = num == num2 || die ( "" + num);

    }



    @Test
    public void testWithSpaces() {
        int num = ((Number)jsonParserAndMapper.parse( "           123")).intValue();
        int num2 = 123;
        boolean ok = num == num2 || die ( "" + num);

    }

    @Test
    public void parseInt () {
        int i;
        boolean ok;

        i = jsonParserAndMapper.parseInt ("1");



        ok = i == 1 || die ( "i" + i  );
    }


    @Test
    public void parseDouble() {
        double v;
        boolean ok;

        v = jsonParserAndMapper.parseDouble ("1");



        ok = v == 1 || die ( "v=" + v  );
    }



    @Test
    public void parseMaxDouble() {
        double v;
        boolean ok;

        v = jsonParserAndMapper.parseDouble (""+Double.MAX_VALUE);



        ok = v == Double.MAX_VALUE || die ( "v=" + v  );
    }

    @Test
    public void simpleFloat2 () {


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
    public void subclass() {
        AllTypes foo = new AllTypes ();
        foo.pet = new Dog ();
        foo.pet.name = "Mooney";
        ((Dog)foo.pet).barks = true;

        final JsonSerializer serializer = new JsonSerializerFactory().useAnnotations().create ();

        //puts ( serializer.serialize ( foo ).toString () );

        Map <String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( serializer.serialize ( foo ).toString () );

        Map <String, Object> petMap = ( Map<String, Object> ) map.get ( "pet" );

        String className = (String)petMap.get("class");

        boolean ok = className.endsWith ( ".Dog" )  || die(className);

        AllTypes foo2 = jsonParserAndMapper.parse ( AllTypes.class, serializer.serialize (  foo ).toCharArray () );

        Dog dog = (Dog)foo2.pet;
        ok = dog.name.equals ( "Mooney" )  || die( dog.name );


    }



    @Test
    public void interfaceTest() {
        AllTypes foo = new AllTypes ();
        foo.pet2 = new Dog();
        foo.pet2.name("Mooney");
        ((Dog)foo.pet2).barks = true;

        final JsonSerializer serializer = new JsonSerializerFactory ().useAnnotations().create ();

        //puts ( serializer.serialize ( foo ).toString () );

        Map <String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( serializer.serialize ( foo ).toString () );

        Map <String, Object> petMap = ( Map<String, Object> ) map.get ( "pet2" );

        String className = (String)petMap.get("class");

        boolean ok = className.endsWith ( ".Dog" )  || die(className);

        AllTypes foo2 = jsonParserAndMapper.parse ( AllTypes.class, serializer.serialize (  foo ).toCharArray () );

        Dog dog = (Dog)foo2.pet2;
        ok = dog.name.equals ( "Mooney" )  || die( dog.name );


    }



    @Test
    public void interfaceTestSimple() {
        AllTypes foo = new AllTypes ();
        foo.pet2 = new Dog();
        foo.pet2.name("Mooney");
        ((Dog)foo.pet2).barks = true;

        final JsonSerializer serializer = new JsonSerializerFactory ().create ();

        //puts ( serializer.serialize ( foo ).toString () );

        Map <String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( serializer.serialize ( foo ).toString () );

        Map <String, Object> petMap = ( Map<String, Object> ) map.get ( "pet2" );

        String className = (String)petMap.get("class");

        boolean ok = className.endsWith ( ".Dog" )  || die(className);

        AllTypes foo2 = jsonParserAndMapper.parse ( AllTypes.class, serializer.serialize (  foo ).toCharArray () );

        Dog dog = (Dog)foo2.pet2;
        ok = dog.name.equals ( "Mooney" )  || die( dog.name );


    }
    @Test
    public void roundTrip() {
        AllTypes foo = new AllTypes ();
        foo.ignoreMe = "THIS WILL NOT PASS";
        foo.ignoreMe2 = "THIS WILL NOT PASS EITHER";
        foo.ignoreMe3 = "THIS WILL NOT PASS TOO";

        foo.setDate ( new Date() );
        foo.setBar ( FooEnum.BAR );
        foo.setFoo ( FooEnum.FOO );
        foo.setString ( "Hi Mom" );
        AllTypes foo2 = BeanUtils.copy( foo );
        foo.setAllType ( foo2 );
        foo2.setString ( "Hi Dad" );
        foo.setAllTypeList( Lists.list( BeanUtils.copy( foo2 ), BeanUtils.copy( foo2 ) ) );

        foo2.setString ( "HELLO DERE" );
        foo.setAllTypesSet ( Sets.set ( BeanUtils.copy ( foo2 ), BeanUtils.copy ( foo2 ) ) );

        final JsonSerializer serializer = new JsonSerializerFactory ()
                .useAnnotations ()
                .addFilter ( new FieldFilter () {
                    @Override
                    public boolean include ( Object parent, FieldAccess fieldAccess ) {
                        if ( fieldAccess.name().equals( "ignoreMe3" ) ) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                } ).addPropertySerializer ( new CustomFieldSerializer () {

                    @Override
                    public boolean serializeField ( JsonSerializerInternal serializer, Object parent,
                                                    FieldAccess fieldAccess, CharBuf builder ) {
                        if ( fieldAccess.type().equals ( long.class ) &&
                                fieldAccess.name().endsWith ( "Date" ) ) {

                            builder.addJsonFieldName ( fieldAccess.name() );
                            Date date = Conversions.toDate ( fieldAccess.getLong ( parent ) );

                            final String jsonDateString = Dates.jsonDate ( date );

                            builder.add ( jsonDateString );
                            return true;
                        } else {
                            return false;
                        }
                    }
                } )
                .create ();
        String json = serializer.serialize ( foo ).toString ();
         //puts (json);


        boolean ok = true;

        //puts (json);
        AllTypes testMe = jsonParserAndMapper.parse( AllTypes.class, new StringReader ( json));

         ok |= testMe.equals ( foo ) || die();




        ok |= testMe.ignoreMe == null || die();

        //puts (testMe.ignoreMe2);
        ok |= testMe.ignoreMe2 == null || die();

        //puts (testMe.ignoreMe3);
        ok |= testMe.ignoreMe3 == null || die();

        ok |= testMe.someTimeStamp > 0 || die();


        ok |= testMe.getAllTypesSet().size () > 0 || die();

    }




    @Test
    public void roundTrip2() {
        AllTypes foo = new AllTypes ();
        foo.ignoreMe = "THIS WILL NOT PASS";
        foo.ignoreMe2 = "THIS WILL NOT PASS EITHER";
        foo.ignoreMe3 = "THIS WILL NOT PASS TOO";

        foo.setDate ( new Date() );
        foo.setBar ( FooEnum.BAR );
        foo.setFoo ( FooEnum.FOO );
        foo.setString ( "Hi Mom" );
        AllTypes foo2 = BeanUtils.copy( foo );
        foo.setAllType ( foo2 );
        foo2.setString ( "Hi Dad" );
        foo.setAllTypeList( Lists.list( BeanUtils.copy( foo2 ), BeanUtils.copy( foo2 ) ) );

        final JsonSerializer serializer = new JsonSerializerFactory ().create ();

        String json = serializer.serialize ( foo ).toString ();

        boolean ok = true;

        //puts (json);
        AllTypes testMe = jsonParserAndMapper.parse( AllTypes.class, new StringReader ( json ));

        ok |= testMe.equals ( foo ) || die();


    }


    @Test
    public void roundTrip3() {
        AllTypes foo = new AllTypes ();
        foo.ignoreMe = "THIS WILL NOT PASS";
        foo.ignoreMe2 = "THIS WILL NOT PASS EITHER";
        foo.ignoreMe3 = "THIS WILL NOT PASS TOO";

        foo.setDate ( new Date() );
        foo.setBar ( FooEnum.BAR );
        foo.setFoo ( FooEnum.FOO );
        foo.setString ( "Hi Mom" );
        AllTypes foo2 = BeanUtils.copy( foo );
        foo.setAllType ( foo2 );
        foo2.setString ( "Hi Dad" );
        foo.setAllTypeList( Lists.list( BeanUtils.copy( foo2 ), BeanUtils.copy( foo2 ) ) );

        final JsonSerializer serializer = new JsonSerializerImpl (  );

        String json = serializer.serialize ( foo ).toString ();

        boolean ok = true;

        //puts (json);
        AllTypes testMe = jsonParserAndMapper.parse( AllTypes.class, new StringReader(json));

        ok |= testMe.equals ( foo ) || die();


    }

    @Test
    public void testParserSimpleMapWithNumber () {

        Object obj = jsonParserAndMapper.parse ( Map.class,
                new StringReader ( " { 'foo': 1 }  ".replace ( '\'', '"' ) )
        );

        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        inspectMap ( map );

        System.out.println ( obj );

        //puts ( idx ( map, "foo" ).getClass () );

        ok &= idx ( map, "foo" ).equals ( 1 ) || die ( "I did not find 1 " + idx ( map, "foo" ) );
    }


    @Test
    public void objectSerialization () {


        String fileContents = IO.read ( "files/AllTypes.json" );

        AllTypes types = objectParser ().parse ( AllTypes.class, new StringReader ( fileContents ) );



        //puts ( types );
        validateAllTypes ( types );

        validateAllTypes ( types.getAllType () );

        boolean ok = true;


        ok |= types.getBigDecimal ().equals ( new BigDecimal ( "99" ) ) || die();

        ok |= types.getBigInteger ().equals ( new BigInteger ( "101" ) ) || die();

        String gmtString = DateUtils.getGMTString(types.getDate());
        ok |= gmtString.equals("14/12/13 01:55") || die("" + gmtString);
        ok |= types.getFoo ().toString().equals ( "FOO" ) || die();
        ok |= types.getBar ().toString().equals ( "BAR" ) || die();

        ok |= types.getAllTypeList().size () == 3 || die ( "" + types.getAllTypeList().size () );

        for ( AllTypes allType : types.getAllTypeList() ) {
            validateAllTypes ( allType );
        }

    }

    @Test
    public void objectSerializationList () {


        String fileContents = IO.read ( "files/arrayOfAllType.json" );

        List<AllTypes> types = objectParser ().parseList ( AllTypes.class, fileContents );

        //puts (types);

    }



    @Test
    public void testFilesFromReader() throws Exception {

        boolean fail = false;

        final List<String> list = IO.listByExt ( "files", ".json" );

        for ( String file : list ) {


            //puts ( "testing", file );

            try {
                Object object =  jsonParserAndMapper.parse ( Files.newInputStream ( IO.path ( file) ) );


                //puts ( "FILE _________\n\n\n", file, object.getClass (), object);


            } catch ( Exception ex ) {
                //puts (ex);
                puts ("FAIL....................", file, "FAILED");
                fail=true;
                ex.printStackTrace(System.out);
            }

        }
        if (fail) die();
        //puts ("done");

    }

    @Test
    public void testFiles () {


        final List<String> list = IO.listByExt ( "files", ".json" );

        for ( String file : list ) {


            puts ( "testing", file );

            Object object =  jsonParserAndMapper.parse ( new String ( IO.read ( file ) ) );
            //puts ( "FILE _________\n\n\n", file, object.getClass (), object);



        }
        //puts ("done");

    }

    private void validateAllTypes ( AllTypes types ) {
        boolean ok = true;
        ok |= types.getMyInt () == 1 || die ( "" + types.getMyInt () );

        ok |= types.getMyFloat () == 1.1f || die ( "" + types.getMyFloat () );

        ok |= types.getMyDouble () == 1.2 || die ( "" + types.getMyDouble () );

        ok |= types.isMyBoolean () == true || die ( "" + types.isMyBoolean () );

        ok |= types.getMyShort () == 2 || die ( "" + types.getMyShort () );

        ok |= types.getMyByte () == 3 || die ( "" + types.getMyByte () );

        ok |= types.getString ().equals ( "test" ) || die ( "" + types.getString () );
    }


    @Test
    public void testParseFalse () {

        Object obj = jsonParserAndMapper.parseMap(
               new String( " { 'foo': false }  ".replace( '\'', '"' ) )
        );

        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        System.out.println ( obj );

        ok &= idx ( map, "foo" ).equals ( false ) || die ( "I did not find  false" );
    }

    @Test
    public void testParseNull () {

        Object obj = jsonParserAndMapper.parse (
                " { 'foo': null }  ".replace ( '\'', '"' )
        );

        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        System.out.println ( obj );

        ok &= idx ( map, "foo" ) == ( null ) || die ( "I did not find null" );
    }

    @Test
    public void testParserSimpleMapWithBoolean () {

        Object obj = jsonParserAndMapper.parse (
                " { 'foo': true }  ".replace ( '\'', '"' )
        );

        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        System.out.println ( obj );

        ok &= idx ( map, "foo" ).equals ( true ) || die ( "I did not find true" );
    }


    @Test
    public void testParserSimpleMapWithList () {

        Object obj = jsonParserAndMapper.parse (
               new String( " { 'foo': [0,1,2 ] }  ".replace ( '\'', '"' ) )
        );

        boolean ok = true;

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        System.out.println ( obj );

        ok &= idx ( map, "foo" ).equals ( list ( 0, 1, 2 ) ) || die ( "I did not find (0,1,2) " + map.get("foo") );
    }

    @Test
    public void testParserList () {

        Object obj = jsonParserAndMapper.parse (
                new StringReader( "[0,1,2]".replace ( '\'', '"' ) )
        );

        List list = (List)obj;
        boolean ok = list.equals ( list ( 0, 1, 2 ) ) || die ( "I did not find (0,1,2) " + list );
    }

    @Test
    public void testParserSimpleMapWithString () {

        Object obj = jsonParserAndMapper.parse (
                " { 'foo': 'str ' }  ".replace ( '\'', '"' )
        );

        boolean ok = true;

        System.out.println ( "%%%%%%" + obj );

        ok &= obj instanceof Map || die ( "Object was not a map" );

        Map<String, Object> map = ( Map<String, Object> ) obj;

        System.out.println ( obj );

        final Object foo = idx ( map, "foo" );
        ok &= foo.equals ( "str " ) || die ( "I did not find 'str'" + foo );
    }


    @Test
    public void testLists () {
        String[][] testLists = {
                { "emptyList", "[]" },                  //0
                { "emptyList", " [ ]" },                  //1  fails
                { "oddly spaced", "[ 0 , 1 ,2, 3, '99' ]" },   //2
                { "nums and strings", "[ 0 , 1 ,'bar', 'foo', 'baz' ]" }, //3
                { "nums stings map", "[ 0 , 1 ,'bar', 'foo', {'baz':1} ]" }, //4
                { "nums strings map with listStream", "[ 0 , 1 ,'bar', 'foo', {'baz':1, 'lst':[1,2,3]} ]" },//5
                { "nums strings map with listStream", "[ {'bar': {'zed': 1}} , 1 ,'bar', 'foo', {'baz':1, 'lst':[1,2,3]} ]" },//6
                { "tightly spaced", "[0,1,2,3,99]" },

        };

        List<?>[] lists = {
                Collections.EMPTY_LIST,    //0
                Collections.EMPTY_LIST,    //1
                Lists.list ( 0, 1, 2, 3, "99" ),  //2
                Lists.list ( 0, 1, "bar", "foo", "baz" ),//3
                Lists.list ( 0, 1, "bar", "foo", map ( "baz", 1 ) ),//4
                Lists.list ( 0, 1,
                        "bar",
                        "foo",
                        map ( "baz", 1,
                                "lst", list ( 1, 2, 3 )
                        )
                ),//5
                Lists.list ( map ( "bar", map ( "zed", 1 ) ), 1, "bar", "foo", map ( "baz", 1, "lst", list ( 1, 2, 3 ) ) ),//6
                Lists.list ( 0, 1, 2, 3, 99 )
        };

        for ( int index = 0; index < testLists.length; index++ ) {
            String name = testLists[ index ][ 0 ];
            String json = testLists[ index ][ 1 ];

            helper ( name, json, lists[ index ] );
        }
    }


    public void helper ( String name, String json, Object compareTo ) {

        System.out.printf ( "%s, %s, %s", name, json, compareTo );

        Object obj = jsonParserAndMapper.parse (
                json.replace ( '\'', '"' )
        );

        boolean ok = true;


        System.out.printf ( "\nNAME=%s \n \t parsed obj=%s\n \t json=%s\n \t compareTo=%s\n", name, obj, json, compareTo );
        ok &= compareTo.equals ( obj ) || die ( name + " :: List has items " + json );


    }


    @Test
    public void testNumber () {

        Object obj = jsonParserAndMapper.parse (Integer.class,
                "1".replace ( '\'', '"' )
        );

        boolean ok = true;

        ok &= obj instanceof Integer || die ( "Object was not an Integer " + obj + " " + obj.getClass () );

        int i = ( Integer ) obj;

        ok &= i == 1 || die ( "I did see i equal to 1" );

        System.out.println ( obj.getClass () );
    }

    @Test
    public void testBoolean () {

        Object obj = jsonParserAndMapper.parse (boolean.class,
                "  true  ".replace ( '\'', '"' )
        );

        boolean ok = true;

        ok &= obj instanceof Boolean || die ( "Object was not a Boolean" );

        boolean value = ( Boolean ) obj;

        ok &= value == true || die ( "I did see value equal to true" );

        System.out.println ( obj.getClass () );
    }

    @Test ( expected = JsonException.class )
    public void testBooleanParseError () {

        Object obj = jsonParserAndMapper.parse ( Map.class,
                "  tbone  ".replace ( '\'', '"' )
        );

        boolean ok = true;

        ok &= obj instanceof Boolean || die ( "Object was not a Boolean" );

        boolean value = ( Boolean ) obj;

        ok &= value == true || die ( "I did see value equal to true" );

        System.out.println ( obj.getClass () );
    }

    @Test
    public void testString () {

        String testString =
                ( "  'this is all sort of text, " +
                        "   do you think it is \\'cool\\' '" ).replace ( '\'', '"' );


        Object obj = jsonParserAndMapper.parse ( String.class, testString );

        System.out.println ( "here is what I got " + obj );

        boolean ok = true;

        ok &= obj instanceof String || die ( "Object was not a String" + obj.getClass());

        String value = ( String ) obj;

        assertEquals ( "this is all sort of text,    do you think it is \"cool\" ", obj );

        System.out.println ( obj.getClass () );
    }


    @Test
    public void testStringInsideOfList () {

        String testString = (
                "  [ 'this is all sort of text, " +
                        "   do you think it is \\'cool\\' '] " ).replace ( '\'', '"' );


        Object obj = jsonParserAndMapper.parse ( testString );


        System.out.println ( "here is what I got " + obj );

        boolean ok = true;

        ok &= obj instanceof List || die ( "Object was not a List" );

        List<String> value = ( List<String> ) obj;


        assertEquals ( "this is all sort of text,    do you think it is \"cool\" ",
                Lists.idx ( value, 0 ) );

        System.out.println ( obj.getClass () );
    }

    @Test
    public void testStringInsideOfList2 () {

        String testString =
                "[ 'abc','def' ]".replace ( '\'', '"' );


        Object obj = jsonParserAndMapper.parse ( testString );
        System.out.println ( "here is what I got " + obj );

        boolean ok = true;

        ok &= obj instanceof List || die ( "Object was not a List" );

        List<String> value = ( List<String> ) obj;


        assertEquals ( "abc",
                Lists.idx ( value, 0 ) );

        System.out.println ( obj.getClass () );
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
        die ( "The above should cause an exception" );

    }

    @Test
    public void oddlySpaced2 () {

        Object obj = jsonParserAndMapper.parse (
                lines ( "[   2   ,    1, 0]"
                ).replace ( '\'', '"' )
        );

        boolean ok = true;

        System.out.println ( obj );

    }


    @Test
    public void parseArray () {

        String testString = "[0, 2, 4, 8, 16]";
        int [] compareArray = {0, 2, 4, 8, 16};
        long [] compareLongArray = {0L, 2L, 4L, 8L, 16L};
        byte [] compareByteArray = {0, 2, 4, 8, 16};
        short [] compareShortArray = {0, 2, 4, 8, 16};
        float [] compareFloatArray = {0, 2, 4, 8, 16};
        double [] compareDoubleArray = {0, 2, 4, 8, 16};

        final int[] array = jsonParserAndMapper.parseIntArray ( testString );

        boolean ok = Arrays.equals (compareArray, array) || die( sputs(array));


        final long[] larray = jsonParserAndMapper.parseLongArray ( testString );

        ok = Arrays.equals (compareLongArray, larray) || die( sputs(larray));


        final byte[] barray = jsonParserAndMapper.parseByteArray ( testString );
        ok |= Arrays.equals (compareByteArray, barray) || die( sputs(barray));

        final short[] sarray = jsonParserAndMapper.parseShortArray ( testString );
        ok |= Arrays.equals (compareShortArray, sarray) || die( sputs(sarray));

        final float[] farray = jsonParserAndMapper.parseFloatArray ( testString );
        ok |= Arrays.equals (compareFloatArray, farray) || die( sputs(farray));

        final double[] darray = jsonParserAndMapper.parseDoubleArray ( testString );
        ok |= Arrays.equals (compareDoubleArray, darray) || die( sputs(darray));

        puts ("parseArray", ok);

    }

    @Test
    public void parseNumber () {
        int i = jsonParserAndMapper.parseInt ( "123" );
        boolean ok = i == 123 || die ( "" + i );

        i = jsonParserAndMapper.parseInt ( "123".getBytes ( StandardCharsets.UTF_8 ) );
        ok = i == 123 || die ( "" + i );

        i = jsonParserAndMapper.parseByte ( "123" );
        ok = i == 123 || die ( "" + i );



        i = jsonParserAndMapper.parseShort ( "123" );
        ok = i == 123 || die ( "" + i );


        i = (int) jsonParserAndMapper.parseDouble ( "123" );
        ok = i == 123 || die ( "" + i );


        i = (int) jsonParserAndMapper.parseFloat ( "123" );
        ok = i == 123 || die ( "" + i );

        i =  (int)jsonParserAndMapper.parseLong ( "123" );
        ok = i == 123 || die ( "" + i );

        i = (int) jsonParserAndMapper.parseLong ( "-123" );
        ok = i == -123 || die ( "" + i );

        long l = jsonParserAndMapper.parseLong ( "-123456789099" );
        ok = l == -123456789099L || die ( "" + l );


//        double d = jsonParserAndMapper.parseDouble ( "-123456789099.1" );
//        ok = d == -1.23455932109009E13 || die ( "" + d );

        puts ( ok );
    }


    @Test
    public void complex () {


        Object obj = jsonParserAndMapper.parse (
                lines (

                        "{    'num' : 1   , ",
                        "     'bar' : { 'foo': 1  },  ",
                        "     'nums': [0  ,1,2,3,4,5,'abc'] } "
                ).replace ( '\'', '"' ).getBytes ( StandardCharsets.UTF_8 )
        );

        boolean ok = true;

        System.out.println ( obj );
        //die();
    }

    @Test
    public void bug2 () {


        Object obj = jsonParserAndMapper.parse (
                lines (

                        "    [ {'bar': {'zed': 1}} , 1]\n "
                ).replace ( '\'', '"' ).getBytes ( StandardCharsets.UTF_8 )
        );

        boolean ok = true;

        System.out.println ( obj );
        //die();
    }

    //{ "PI":3.141E-10}


    @Test
    public void complianceFromJsonSmartForPI () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"PI\":3.141E-10} "
                )
        );

        Object o = map.get("PI");
        if (o instanceof BigDecimal) {
            o = ((BigDecimal) o).doubleValue();
        }
        boolean ok = o.equals ( 3.141E-10 ) || die ( "map " + map.get ( "PI" ) );
    }


    @Test
    public void complianceForLowerCaseNumber () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"v\":\"\\u00c1\\u00e1\"}"
                )
        );

        //puts ( map );
        boolean ok = map.get ( "v" ).equals ( "Áá" ) || die ( "map " + map.get ( "v" ) );
    }

    @Test
    public void complianceForUpperCaseNumber () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"v\":\"\\u00C1\\u00E1\"}"
                )
        );


        //puts ( map );
        boolean ok = map.get ( "v" ).equals ( "Áá" ) || die ( "map " + map.get ( "v" ) );
    }


    @Test
    public void doublePrecisionFloatingPoint () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"v\":1.7976931348623157E308}"
                )
        );


        Object o = map.get ( "v" );

        if (o instanceof BigDecimal) {
            o = ((BigDecimal) o).doubleValue();
        }
        boolean ok = o.equals ( 1.7976931348623157E308 ) || die ( "map " + map.get ( "v" ) );
    }

    //


    @Test ( expected = JsonException.class )
    public void doubleQuoteInsideOfSingleQuote () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":'ab\"c'}"
                )
        );

    }

    @Test ( expected = JsonException.class )
    public void supportSimpleQuoteInNonProtectedStringValue () {

        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":It's'Work}"
                )
        );
    }

    @Test ( expected = JsonException.class )
    public void supportNonProtectedStrings () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ a:1234}"
                )
        );

    }

    @Test ( expected = JsonException.class )
    public void crapInAnArray () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "[ a,bc]"
                )
        );

    }


    @Test ( expected = JsonException.class )
    public void randomStringAsValuesWithSpaces () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":s1 s2}"
                )
        );

    }


    @Test ( expected = JsonException.class )
    public void randomStringAsValuesWithSpaceAndMoreSpaces () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse ( Map.class,
                lines (

                        "{ \"v\":s1 s2 }"
                )
        );

    }


    @Test ()
    public void garbageAtEndOfString () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"a\":\"foo.bar\"}#toto"
                )
        );
        //puts ( map );
    }


    @Test ( expected = JsonException.class )
    public void singleQuotes () {
        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ 'value':'string'}"
                )
        );

    }



    @Test
    public void simpleFloat () {


        Map<String, Object> map = ( Map<String, Object> ) jsonParserAndMapper.parse (
                lines (

                        "{ \"v\":1.1}"
                )
        );


        Object o = map.get ( "v" );

        if (o instanceof BigDecimal) {
            o = ((BigDecimal) o).doubleValue();
        }

        boolean ok = o.equals ( 1.1 ) || die ( "map " + map.get ( "v" ) );
    }




    @Test
    public void testArrayOfArrayWithSimpleValues() {
        boolean ok = jsonParserAndMapper.parse ("[1, 2, 3, [\"a\", \"b\", \"c\", [true, false], \"d\"], 4]").equals (
        list(1, 2, 3, list("a", "b", "c", list(true, false), "d"), 4)) || die();

//        shouldFail(JsonException) { parser.parseText('[') }
//        shouldFail(JsonException) { parser.parseText('[,]') }
//        shouldFail(JsonException) { parser.parseText('[1') }
//        shouldFail(JsonException) { parser.parseText('[1,') }
//        shouldFail(JsonException) { parser.parseText('[1, 2') }
//        shouldFail(JsonException) { parser.parseText('[1, 2, [3, 4]') }
    }

    @Test
    public void testArrayOfArrayWithSimpleValuesValue1() {
        try {
            List list = (List) jsonParserAndMapper.parse("[,]");
            die();
        } catch (JsonException jsonException) {

        }
    }


    @Test
    public void testArrayOfArrayWithSimpleValuesValue2() {
        try {
            List list = (List) jsonParserAndMapper.parse("[");
            die();
        } catch (JsonException jsonException) {
             jsonException.printStackTrace();
        }
    }


    @Test
    public void testArrayOfArrayWithSimpleValuesValue3() {
        try {
            List list = (List) jsonParserAndMapper.parse("[1");
            die();
        } catch (JsonException jsonException) {

        }
    }


    @Test
    public void testArrayOfArrayWithSimpleValuesValue5() {
        try {
            List list = (List) jsonParserAndMapper.parse("[1");
            die();
        } catch (JsonException jsonException) {
             jsonException.printStackTrace ();
        }

    }





    @Test
    public void testArrayOfArrayWithSimpleValuesValue6() {
        try {
            List list = (List) jsonParserAndMapper.parse("[1,");
            die();
        } catch (JsonException jsonException) {
            jsonException.printStackTrace ();
        }

    }
    @Test
    public void testArrayOfArrayWithSimpleValuesValue7() {
        try {
            List list = (List) jsonParserAndMapper.parse("[1, [2]");
            die();
        } catch (JsonException jsonException) {

        }
    }



    @Test
    public void testBackSlashEscaping() {
        Object obj =  parser().parse("{\"a\":\"\\\\a\\\\b\" }");

        boolean ok = obj.equals ( Maps.map ( "a", "\\a\\b") ) || die ("" + obj);


        obj =  parser().parse("{\"a\":\" \\\\\\\\ \" }");

        ok = obj.equals ( Maps.map ( "a", " \\\\ ") ) || die ("" + obj);


    }

    @Test
    public void testBackSlashEscaping2() {
        Object obj = null;

        boolean ok = false;

        //obj =  parser().parse("{\"a\":\"\\\\\\\\\" }");

        //ok = obj.equals ( Maps.map ( "a", "\\\\") ) || die ("" + obj);


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

        try {
            parser().parse( str );
            die();
        } catch ( JsonException ex ) {
              ex.printStackTrace();
        }


    }


    @Test
    public void testNullEmptyMalformedPayloads() {

        List<String> list = Lists.list ( "[", "[a", "{\"key\"", "{\"key\":1", "[\"a\"", "[\"a\", ", "[\"a\", true" );

        for (String str : list) {
            try {
                parser().parse( str );
                die(str);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }



    @Test
    public void parseNumber2 () {
        int i = jsonParserAndMapper.parseInt ( "123" );
        boolean ok = i == 123 || die ( "" + i );

        i = jsonParserAndMapper.parseInt ( "123".getBytes ( StandardCharsets.UTF_8 ) );
        ok = i == 123 || die ( "" + i );

        i = jsonParserAndMapper.parseByte ( "123" );
        ok = i == 123 || die ( "" + i );



        i = jsonParserAndMapper.parseShort ( "123" );
        ok = i == 123 || die ( "" + i );


        i = (int) jsonParserAndMapper.parseDouble ( "123" );
        ok = i == 123 || die ( "" + i );


        i = (int) jsonParserAndMapper.parseFloat ( "123" );
        ok = i == 123 || die ( "" + i );

        i =  (int)jsonParserAndMapper.parseLong ( "123" );
        ok = i == 123 || die ( "" + i );


        puts ( ok );
    }



}