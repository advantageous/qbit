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
import io.advantageous.boon.core.Dates;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.json.annotations.Expose;
import io.advantageous.boon.json.annotations.JsonInclude;
import io.advantageous.boon.json.annotations.JsonViews;
import io.advantageous.boon.json.test.Dog;
import io.advantageous.boon.json.test.Person;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.advantageous.boon.Boon.atIndex;
import static io.advantageous.boon.Boon.fromJson;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

/**
 * Created by rick on 1/4/14.
 */
public class JsonTutorial {


    public static class MyBean {
        String name = "Rick";

        @Override
        public String toString() {
            return "MyBean{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }


    public static class User {


        @JsonViews( ignoreWithViews = {"public"},
                includeWithViews = {"internal"})
        private String empId = "555-55-5555";

        //@JsonIgnore
        @Expose(serialize = false)
        private String ssn = "555-55-5555";

        @JsonInclude
        private String status = null;

        public enum Gender {MALE, FEMALE}



        public static class Name {
            private String first, last;

            public Name( String _first, String _last ) {
                this.first = _first;
                this.last = _last;
            }

            public String getFirst() {
                return first;
            }

            public String getLast() {
                return last;
            }

            public void setFirst( String s ) {
                first = s;
            }

            public void setLast( String s ) {
                last = s;
            }

            @Override
            public String toString() {
                return "Name{" +
                        "first='" + first + '\'' +
                        ", last='" + last + '\'' +
                        '}';
            }


            @Override
            public boolean equals( Object o ) {
                if ( this == o ) return true;
                if ( o == null || getClass () != o.getClass () ) return false;

                Name name = ( Name ) o;

                if ( first != null ? !first.equals ( name.first ) : name.first != null ) return false;
                if ( last != null ? !last.equals ( name.last ) : name.last != null ) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = first != null ? first.hashCode () : 0;
                result = 31 * result + ( last != null ? last.hashCode () : 0 );
                return result;
            }
        }

        private Gender gender;
        private Name name;
        private boolean verified;
        private Date birthDate;

        public Name getName() {
            return name;
        }

        public boolean isVerified() {
            return verified;
        }

        public Gender getGender() {
            return gender;
        }

        public void setName( Name n ) {
            name = n;
        }

        public void setVerified( boolean b ) {
            verified = b;
        }

        public void setGender( Gender g ) {
            gender = g;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate( Date birthDate ) {
            this.birthDate = birthDate;
        }

        @Override
        public String toString() {
            return "Watcher{" +
                    "gender=" + gender +
                    ", name=" + name +
                    ", verified=" + verified +
                    ", birthDate=" + birthDate +
                    '}';
        }


        @Override
        public boolean equals( Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass () != o.getClass () ) return false;

            User user = ( User ) o;

            if ( verified != user.verified ) return false;
            if ( birthDate != null ? !birthDate.equals ( user.birthDate ) : user.birthDate != null ) return false;
            if ( empId != null ? !empId.equals ( user.empId ) : user.empId != null ) return false;
            if ( gender != user.gender ) return false;
            if ( name != null ? !name.equals ( user.name ) : user.name != null ) return false;
            if ( ssn != null ? !ssn.equals ( user.ssn ) : user.ssn != null ) return false;
            if ( status != null ? !status.equals ( user.status ) : user.status != null ) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = empId != null ? empId.hashCode () : 0;
            result = 31 * result + ( ssn != null ? ssn.hashCode () : 0 );
            result = 31 * result + ( status != null ? status.hashCode () : 0 );
            result = 31 * result + ( gender != null ? gender.hashCode () : 0 );
            result = 31 * result + ( name != null ? name.hashCode () : 0 );
            result = 31 * result + ( verified ? 1 : 0 );
            result = 31 * result + ( birthDate != null ? birthDate.hashCode () : 0 );
            return result;
        }
    }


    static User user = new User();

    static {
        user.setGender( User.Gender.MALE );
        user.setName( new User.Name( "Richard", "Hightower" ) );
        user.setVerified( true );
        user.setBirthDate( Dates.getUSDate( 5, 25, 1980 ) );
    }

    public static void part1ReadAndWriteMyBeanToAFile() throws Exception {


        MyBean myBean = new MyBean();
        File dst = File.createTempFile( "emp", ".json" );

        ObjectMapper mapper = JsonFactory.create();

        puts( "json string", mapper.writeValueAsString( myBean ) );

        String str = mapper.writeValueAsString ( myBean );
        boolean ok = str.contains ( "{\"name\":\"Rick\"" ) || die( str );

        mapper.writeValue( dst, myBean ); // where 'dst' can be File, OutputStream or Writer


        File src = dst;
        MyBean value = mapper.readValue( src, MyBean.class ); // 'src' can be File, InputStream, Reader, String

        ok |= value.name.contains ( "Rick" );

        puts( "mybean", value );


        Object root = mapper.readValue( src, Object.class );
        Map<String, Object> rootAsMap = mapper.readValue( src, Map.class );

        puts( "root", root );
        puts( "rootAsMap", rootAsMap );


        MyBean myBean1 = new MyBean();
        myBean1.name = "Diana";
        MyBean myBean2 = new MyBean();
        myBean2.name = "Rick";

        dst = File.createTempFile( "empList", ".json" );

        final List<MyBean> list = Lists.list( myBean1, myBean2 );

        str = mapper.writeValueAsString ( list );

        puts ( "json string", mapper.writeValueAsString ( list ) );

        ok |= str.contains ( "[{\"name\":\"Diana\"},{\"name\":\"Rick\"}]" ) || die (str);

        mapper.writeValue( dst, list );

        src = dst;

        List<MyBean> beans = mapper.readValue( src, List.class, MyBean.class );

        puts( "mybeans", beans );


    }

    public static void part2WorkingWithInputStreamsReaders() throws Exception {

        ObjectMapper mapper = JsonFactory.create();


        puts( mapper.writeValueAsString( user ) );


        //Now to write and then read this as a file.

        File file = File.createTempFile( "user", ".json" );

        mapper.writeValue( file, user );

        User userFromFile = mapper.readValue( file, User.class );

        puts( "userFromFile", userFromFile );


        Path path = Paths.get( file.toString() );
        InputStream inputStream = Files.newInputStream( path );

        User userFromInput = mapper.readValue( inputStream, User.class );
        puts( "userFromInput", userFromInput );


        Reader reader = Files.newBufferedReader( path, StandardCharsets.UTF_8 );
        User userFromReader = mapper.readValue( reader, User.class );

        puts( "userFromReader", userFromReader );

        //This test only works on the PACIFC COAST.. on EST coast I get Watcher{gender=MALE, name=Name{first='Richard', last='Hightower'}, verified=true, birthDate=Sun May 25 20:00:00 EDT 1980}
//        boolean ok = userFromReader.toString ().equals ( "Watcher{gender=MALE, name=Name{first='Richard', " +
//                "last='Hightower'}, verified=true, birthDate=Sun May 25 17:00:00 PDT 1980}"  ) ||
//                die (userFromReader.toString ());
    }

    public static void part3WorkingWithDates () throws Exception {
        part3_1();
        part3_2();
    }

    public static void part3_1() throws Exception {

        ObjectMapper mapper = JsonFactory.create();
        puts( mapper.writeValueAsString( user ) );

        User user2 = mapper.readValue( mapper.writeValueAsString( user ), User.class );

        puts( user2 );

        boolean ok = user.equals ( user2 ) || die (user.toString ());
    }


    public static void part3_2() throws Exception {

        ObjectMapper mapper = JsonFactory.createUseJSONDates();
        puts( mapper.writeValueAsString( user ) );

        User user2 = mapper.readValue( mapper.writeValueAsString( user ), User.class );

        puts( user2 );
        boolean ok = user.equals ( user2 ) || die (user.toString ());

    }

    public static void part5WorkingWithLists() throws Exception {

        puts ("\n\n\n", "\npart5 WorkingWithLists");

        ObjectMapper mapper = JsonFactory.createUseJSONDates();


        final User diana = BeanUtils.copy( user );
        final User rick = BeanUtils.copy( user );
        diana.getName().setFirst( "Diana" );
        rick.getName().setFirst( "Rick" );
        diana.setBirthDate( Dates.getUSDate( 8, 21, 1984 ) );


        File file = File.createTempFile( "userList", ".json" );

        List<User> users = Lists.list( diana, rick );

        mapper.writeValue( file, users  );


        List<User> userList = mapper.readValue( file, List.class, User.class  );


        puts (userList);

        puts ( mapper.writeValueAsString( userList ) );


        boolean ok = users.toString().equals ( userList.toString () ) || die (userList.toString ());



    }

    public static void part4IntoAMapFirst() throws Exception {

        ObjectMapper mapper = JsonFactory.createUseJSONDates();


        puts( mapper.writeValueAsString( user ) );


        //Now to write and then read this as a file.

        File file = File.createTempFile( "user", ".json" );

        mapper.writeValue( file, user );

        Object userFromFile = mapper.readValue( file, Object.class );

        puts( "userFromFile", "type", userFromFile.getClass(), "value", userFromFile );

        Map<String, Object> map = (Map<String, Object>) mapper.readValue( file, Map.class );

        puts( "userFromFile", "type", map.getClass(), "value", map );


        puts( "userFromFile.name", "type", map.get("name").getClass(),
                "value", map.get("name") );


        puts( "userFromFile.birthDate", "type", map.get("birthDate").getClass(),
                "value", map.get("birthDate") );


        puts( "userFromFile.gender", "type", map.get("gender").getClass(),
                "value", map.get("gender") );


        User userFromMap =
               MapObjectConversion.fromMap(
                       map, User.class);

        puts ( userFromMap );

        boolean ok = user.equals ( userFromMap ) || die (userFromMap.toString ());


    }


    public static void part6WorkingWithLists() throws Exception {

        puts ("\n\n\n", "\npart6WorkingWithLists");

        ObjectMapper mapper = JsonFactory.createUseJSONDates();


        final User diana = BeanUtils.copy( user );
        final User rick = BeanUtils.copy( user );
        diana.getName().setFirst( "Diana" );
        diana.setGender( User.Gender.FEMALE );
        rick.getName().setFirst( "Rick" );
        diana.setBirthDate( Dates.getUSDate( 8, 21, 1984 ) );


        File file = File.createTempFile( "userList", ".json" );

        List<User> users = Lists.list( diana, rick );

        mapper.writeValue( file, users  );


        List<User> userList = mapper.readValue( file, List.class, User.class  );


        puts (userList);

        puts ( mapper.writeValueAsString( userList ) );


        boolean ok = userList.toString().equals ( users.toString() ) || die ( userList.toString() );



    }

    public static void part7WorkingWithListFromFile() throws Exception {

        puts ("\n\n\n", "\npart7WorkingWithListFromFile");

        ObjectMapper mapper = JsonFactory.createUseAnnotations( true );


        /* Create two users. */
        final User diana = BeanUtils.copy( user );
        final User rick = BeanUtils.copy( user );
        diana.getName().setFirst( "Diana" );
        diana.setGender ( User.Gender.FEMALE );
        rick.getName().setFirst ( "Rick" );
        rick.ssn="IAMSET";
        diana.ssn="dianaSSN";

        diana.setBirthDate( Dates.getUSDate( 8, 21, 1984 ) );

        File file = File.createTempFile( "userList", ".json" );
        List<User> users = Lists.list( diana, rick );


        /* Inspect the JSON of the users from the file. */
        puts ("users", mapper.writeValueAsString( users ) );


        /* Write users out to file. */
        mapper.writeValue( file, users  );

        /* Reader Users back from file. */
        List<User> userList = mapper.readValue( file, List.class, User.class  );


        puts ("userListBeansReadFromFile", userList);

        /* Inspect the JSON of the users from the file. */
        puts ("usersFromFileAsJSON", mapper.writeValueAsString( userList ) );


        boolean ok = userList.toString().equals ( users.toString() ) || die ( userList.toString() );



    }


    public static void part8WorkingWithPrimitives() throws Exception {

        puts ("\n\n\n\npart8WorkingWithPrimitives");

        ObjectMapper mapper = JsonFactory.create();


        String intStr = "123456";

        int someNumber = mapper.parser().parseInt( intStr );

        boolean ok = someNumber == 123456 || die( "" + someNumber );


        String jsonArray = "[0,1,2,3,4,5,6,7,8]";

        int [] intArray = mapper.parser().parseIntArray( jsonArray );

        ok |= Arrays.equals( new int[]{1,2,3,4,5,6,7,8}, intArray );


        String jsonMap = "{\"race\":true, \"speedup\": false, \"name\": \"bob\", \"value\": -0}";

        Map <String, Object> map  = mapper.parser().parseMap( jsonMap );

        ok |= ( map.get("race") == Boolean.TRUE  && map.get("name").equals( "bob" ) )  || die(map.toString());

        ok |= (int)map.get("value") == -0 || die(map.get("value"));

        puts("ok?", ok);




    }



    public static void part9Options() throws Exception {

        puts ("\n\n\n", "\npart9Options");


        JsonParserFactory jsonParserFactory = new JsonParserFactory()
                .useFieldsFirst().useFieldsOnly().usePropertiesFirst().usePropertyOnly() //one of these
                .lax() //allow loose parsing of JSON like JSON Smart
                .strict() //opposite of lax
                .setCharset( StandardCharsets.UTF_8 ) //Set the standard charset, defaults to UTF_8
                .setChop( true ) //chops up buffer overlay buffer (more discussion of this later)
                .setLazyChop( true ) //similar to chop but only does it after map.get
                ;

        JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory()
                .useFieldsFirst().useFieldsOnly().usePropertiesFirst().usePropertyOnly() //one of these
                //.addPropertySerializer(  )  customize property output
                //.addTypeSerializer(  )      customize type output
                .useJsonFormatForDates() //use json dates
                //.addFilter(  )   add a property filter to exclude properties
                .includeEmpty().includeNulls().includeDefaultValues() //override defaults
                .handleComplexBackReference() //uses identity map to track complex back reference and avoid them
                .setHandleSimpleBackReference( true ) //looks for simple back reference for parent
                .setCacheInstances( true ) //turns on caching for immutable objects
                ;



        final User diana = BeanUtils.copy( user );
        final User rick = BeanUtils.copy( user );
        diana.getName().setFirst( "Diana" );
        diana.setGender( User.Gender.FEMALE );
        rick.getName().setFirst( "Rick" );
        diana.setBirthDate( Dates.getUSDate( 8, 21, 1984 ) );
        List<User> users = Lists.list(  rick, diana );

        //You can use parser and serializer directly.
        final JsonParserAndMapper jsonParserAndMapper = jsonParserFactory.create();
        final JsonSerializer jsonSerializer = jsonSerializerFactory.create();

        File file = File.createTempFile( "userList", ".json" );
        String jsonString = jsonSerializer.serialize( users ).toString();

        puts( "JSON STRING", jsonString );

        IO.write( IO.path( file.toString()), jsonString);
        List<User> users2 = jsonParserAndMapper.parseListFromFile( User.class, file.toString() );

        //Or you can pass them to the ObjectMapper interface you know and love, just pass the factories to it.
        ObjectMapper mapper = JsonFactory.create( jsonParserFactory, jsonSerializerFactory );


        mapper.writeValue( file, users  );
        List<User> userList = mapper.readValue( file, List.class, User.class  );
        puts (userList);
        puts ( mapper.writeValueAsString( userList ) );

        puts (userList);
        puts (users);
        boolean ok = userList.toString().equals ( users.toString() ) || die ( userList.toString() );



    }



    public static void part10WorkingWithViews() throws Exception {


        final User rick = BeanUtils.copy( user );
        rick.getName().setFirst( "Rick" );

        boolean ok = true;


        JsonSerializer serializer = new JsonSerializerFactory().useAnnotations().setView( "public" ).create();
        String str = serializer.serialize( rick ).toString();

        puts (str);
        ok |= !str.contains( "\"empId\":" ) || die(str);


        serializer = new JsonSerializerFactory().useAnnotations().setView( "internal" ).create();
        str = serializer.serialize( rick ).toString();
        ok |= str.contains( "\"empId\":" ) || die(str);

        puts (str);


        serializer = new JsonSerializerFactory().useAnnotations().create();
        str = serializer.serialize( rick ).toString();
        ok |= str.contains( "\"empId\":" ) || die(str);

        puts (str);


    }


    public static void main( String... args ) throws Exception {

        puts(-0.0, 0.0, Float.parseFloat("-0.0"), -0, 0);




        String json = "{\"scout\": \"webdriver\", \"short_version\": \"4.3\", \"long_name\": \"iPad\", \"api_name\": \"ipad\", \n" +
                "\n" +
                "\"long_version\": \"4.3.\", \"automation_backend\": \"webdriver\", \"os\": \"Mac 10.6\"}";

        System.out.println( atIndex(fromJson(json), "os") );

        part1ReadAndWriteMyBeanToAFile ();
        part2WorkingWithInputStreamsReaders ();
        part3WorkingWithDates ();
        part4IntoAMapFirst ();
        part5WorkingWithLists ();
        part6WorkingWithLists ();
        part7WorkingWithListFromFile();
        part8WorkingWithPrimitives ();
        part9Options ();
        part10WorkingWithViews ();
        part11Subtypes();

        part12Gson();


    }

    static class BagOfPrimitives {
        private int value1 = 1;
        private String value2 = "abc";
        private transient int value3 = 3;
        BagOfPrimitives() {
        }
    }

    private static void part12Gson() {

        ObjectMapper gson = JsonFactory.createUseAnnotations( true );


        puts ( gson.toJson ( 1 ) );
        puts ( gson.toJson ( "abcd" ) );
        puts ( gson.toJson ( new Long ( 10 ) ) );
        int[] values = { 1 };
        puts ( gson.toJson ( values ) );


        int ione = gson.fromJson("1", int.class);
        Integer oneI = gson.fromJson("1", Integer.class);
        Boolean wrapper = gson.fromJson("false", Boolean.class);
        String str = gson.fromJson("\"abc\"", String.class);
        String anotherStr = (String)gson.fromJson("[\"abc\"]", List.class).get ( 0 );



        BagOfPrimitives obj = new BagOfPrimitives();
        String json = gson.toJson(obj);
        puts (json);



        int[] ints = {1, 2, 3, 4, 5};
        String[] strings = {"abc", "def", "ghi"};

        puts ( gson.toJson ( ints ) ); //     ==> prints [1,2,3,4,5]
        puts ( gson.toJson ( strings ) ); // ==> prints ["abc", "def", "ghi"]


        Collection<Integer> ints2 = Lists.list(1,2,3,4,5);

        puts ( gson.toJson(ints) ) ;// ==> json is [1,2,3,4,5]

        puts ( gson.parser ().parseList ( Integer.class, "[1,2,3,4,5]" ));


        //Serializing and Deserializing Generic Types TODO missing from GSON manual
        //Left off here https://sites.google.com/site/gson/gson-user-guide#TOC-Serializing-and-Deserializing-Generic-Types
    }

    private static void part11Subtypes() {
        Person person = new Person();
        person.name = "Rick";
        person.city = "Tucson";
        person.pet = new Dog();
        person.pet2 = new Dog ();
        person.pet.name = "Mooney";
        person.pet2.name ( "Annabel" );

        ((Dog)person.pet).barks = true;

        ObjectMapper mapper = JsonFactory.createUseAnnotations( true );



        //uts ( mapper.toJson ( person ) );

        Map <String, Object> map = ( Map<String, Object> ) mapper.fromJson ( mapper.toJson ( person ), Map.class );

        Map <String, Object> petMap = ( Map<String, Object> ) map.get ( "pet" );
        Map <String, Object> pet2Map = ( Map<String, Object> ) map.get ( "pet" );

        String className = (String)petMap.get("class");

        boolean ok = className.endsWith ( ".Dog" )  || die(className);

        className = (String)pet2Map.get("class");

        ok = className.endsWith ( ".Dog" )  || die(className);

        Person person2 = mapper.fromJson ( mapper.toJson ( person ), Person.class );

        Dog dog = (Dog)person2.pet;
        ok = dog.name.equals ( "Mooney" )  || die( dog.name );

        dog = (Dog)person2.pet2;
        ok = dog.name.equals ( "Annabel" )  || die( dog.name );

    }


    @Test
    public void test() throws Exception {
        JsonTutorial.main(  );
    }
}
