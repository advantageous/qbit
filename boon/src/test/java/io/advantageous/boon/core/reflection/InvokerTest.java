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

package io.advantageous.boon.core.reflection;

import io.advantageous.com.examples.model.test.movies.admin.AdminService;
import io.advantageous.com.examples.model.test.movies.crud.CrudType;
import io.advantageous.com.examples.model.test.movies.entitlement.Rights;
import io.advantageous.com.examples.model.test.movies.entitlement.RightsCrudRequest;
import io.advantageous.com.examples.model.test.movies.entitlement.RightsPushRequest;
import io.advantageous.com.examples.model.test.movies.entitlement.RightsType;
import io.advantageous.com.examples.model.test.time.TimeZoneType;
import io.advantageous.boon.*;
import io.advantageous.boon.primitive.CharBuf;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.PostConstruct;


import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.boon.json.JsonFactory.toJson;
import static io.advantageous.boon.primitive.CharBuf.createCharBuf;

/**
 * Created by Richard on 2/17/14.
 */
public class InvokerTest {

    int counter = 0;

    boolean ok;

    @Before
    public void init() {

    }

    @Test
    public void invokeObj() {
        AdminService adminService = new AdminService(){

            @Override
            public boolean rightsPush(RightsPushRequest request) {

                puts(request);
                counter++;
                return false;
            }
        };


        Rights rights = Rights.createRights(
                RightsType.AMAZON_PRIME, true, TimeZoneType.EST, System.currentTimeMillis());
        RightsCrudRequest rightsCrudRequest = new RightsCrudRequest("Bob",
                CrudType.ADD, rights);

        RightsCrudRequest rightsCrudRequest2 = BeanUtils.copy(rightsCrudRequest);
        rightsCrudRequest2.setUsername("Rick2");
        RightsCrudRequest rightsCrudRequest3 = BeanUtils.copy(rightsCrudRequest);
        rightsCrudRequest3.setUsername("Jason3");

        List<RightsCrudRequest> rightsList = Lists.list(rightsCrudRequest, rightsCrudRequest2, rightsCrudRequest3);

        RightsPushRequest rightsPushRequest = new RightsPushRequest(1L, rightsList);

        adminService.rightsPush( rightsPushRequest );

        Invoker.invokeFromObject(adminService, "rightsPush", rightsPushRequest);

        String json = toJson(rightsPushRequest);
        Object o = fromJson(json);
        Invoker.invokeFromObject(adminService, "rightsPush", o);

        ok = counter == 3 || die();
    }


    public static class HelloWorldArg  {
         int i = 0;
         String hello = "null";

        public HelloWorldArg( int i ) {
            this.i = i;
        }

        public HelloWorldArg( int i, String hello ) {
            this.i = i;
            this.hello = hello;
        }


        public HelloWorldArg(  String hello ) {
            this.hello = hello;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;

            HelloWorldArg that = ( HelloWorldArg ) o;

            if ( i != that.i ) return false;
            if ( hello != null ? !hello.equals( that.hello ) : that.hello != null ) return false;

            return true;
        }



        @Override
        public int hashCode() {
            int result = i;
            result = 31 * result + ( hello != null ? hello.hashCode() : 0 );
            return result;
        }
    }

    public static class HelloWorld  {

        boolean initCalled;

        boolean called1st;
        boolean called2nd;



        private HelloWorldArg sayArg(HelloWorldArg hi, int i) {
            return hi;
        }



        private HelloWorldArg sayArg2 (HelloWorldArg hi) {
            return hi;
        }

        private String sayHi(String hi) {
            puts ( "hi ", hi );

            return Str.add("hi ", hi);
        }





        private String say(String hi, int i) {
            return sputs( "hi ", hi, i );

        }



        private void sayHi2(String hi) {
            called1st = true;
            puts ( "hi ", hi );

        }

        private void sayHi2(String hi, int i) {
            called2nd = true;

            puts ( "hi ", hi );

        }

        private void sayDie(String hi, int i) {
           die("SAY HI 3 AND DIE");
        }


        @PostConstruct
        private void init() {
            initCalled = true;

        }



    }

    @Test
    public void test() {
        Invoker.invoke( new HelloWorld(), "sayHi", "Rick" );
    }


    @Test
    public void testBug159() {
        String value = (String) Invoker.invokeFromObject(
                new HelloWorld(), "sayHi", Lists.list("Rick"));

        puts(value);

        Boon.equalsOrDie("Should be", "hi Rick", value);
    }

    @Test
    public void testPostConstruct() {
        HelloWorld hw = new HelloWorld();
        hw.initCalled = false;
        Invoker.invokeMethodWithAnnotationNoReturn( hw, "postConstruct" );
        if (!hw.initCalled) {
            die("Post construct not called");
        }
    }


    @Test
    public void testPostConstruct2() {
        HelloWorld hw = new HelloWorld();
        hw.initCalled = false;
        Invoker.invokeMethodWithAnnotationNoReturn( hw, "PostConstruct" );
        if (!hw.initCalled) {
            die("Post construct not called");
        }
    }


    @Test
    public void testPostConstruct3() {
        HelloWorld hw = new HelloWorld();
        hw.initCalled = false;
        Invoker.invokeMethodWithAnnotationNoReturn( hw, "javax.annotation.PostConstruct" );
        if (!hw.initCalled) {
            die("Post construct not called");
        }
    }


    @Test
    public void testNoOverloads() {
        try {
            Invoker.invoke( new HelloWorld(), "sayHi2", "Rick" );
            die("can't get this far");
        } catch (Exception ex) {

        }
    }




    @Test
    public void testAllowOverloads() {
        HelloWorld hw = new HelloWorld();
        hw.called1st = false;
        hw.called2nd = false;

        Invoker.invokeOverloaded( hw, "sayHi2", "Rick" );

        if ((!hw.called1st)) {
            die("");
        }


        if ((hw.called2nd)) {
            die("");
        }
    }




    @Test
    public void testAllowOverloads3() {
        HelloWorld hw = new HelloWorld();
        hw.called1st = false;
        hw.called2nd = false;

        Invoker.invokeOverloadedFromList( hw, "sayHi2", Lists.list("Rick") );

        if ((!hw.called1st)) {
            die("");
        }


        if ((hw.called2nd)) {
            die("");
        }
    }



    @Test
    public void testAllowOverloads2() {
        HelloWorld hw = new HelloWorld();
        hw.called1st = false;
        hw.called2nd = false;

        Invoker.invokeOverloaded( hw, "sayHi2", "Rick", 1 );

        if ((hw.called1st)) {
            die("");
        }


        if ((!hw.called2nd)) {
            die("");
        }
    }



    @Test
    public void testAllowOverloads4() {
        HelloWorld hw = new HelloWorld();
        hw.called1st = false;
        hw.called2nd = false;

        Invoker.invokeOverloadedFromList( hw, "sayHi2", Lists.list("Rick", "1") );

        if ((hw.called1st)) {
            die("");
        }


        if ((!hw.called2nd)) {
            die("");
        }
    }


    @Test
    public void testDie() {
        HelloWorld hw = new HelloWorld();
        hw.called1st = false;
        hw.called2nd = false;

        try {

            Invoker.invokeOverloadedFromList(hw, "sayDie", Lists.list("Rick", "1"));

        }
        catch (Exception ex) {

            CharBuf buf = createCharBuf();
            ex.printStackTrace(buf);

            if (!buf.toString().contains("SAY HI 3 AND DIE")) {
                die();
            }
        }

    }


    @Test
    public void testDie2() {
        HelloWorld hw = new HelloWorld();
        hw.called1st = false;
        hw.called2nd = false;

        try {

            Invoker.invokeOverloadedFromList(hw, "sayDie", Lists.list("Rick", "1"));

        }
        catch (Exception ex) {


            if (!Exceptions.asJson(ex).contains("SAY HI 3 AND DIE")) {
                die();
            }

        }

    }


    @Test
    public void testWithListSimple() {
        Invoker.invokeFromList( new HelloWorld(), "sayHi", Lists.list( "Rick" ) );
    }


    @Test
    public void testWithListSimple2() {
        String message = (String) Invoker.invokeFromList( new HelloWorld(), "say", Lists.list( "Rick", 1 ) );
        puts (message);

        if (!message.equals( "hi  Rick 1\n" )) die(message);
    }


    @Test
    public void testWithListSimpleWithConversion() {
        String message = (String) Invoker.invokeFromList( new HelloWorld(), "say", Lists.list( "Rick", "1" ) );
        puts (message);
        if (!message.equals( "hi  Rick 1\n" )) die(message);
    }



    @Test
    public void testComplex() {
        HelloWorldArg message = (HelloWorldArg) Invoker.invokeFromList( new HelloWorld(), "sayArg",
                Lists.list( Lists.list( "1", "Hello" ), 1 ) );


        if (!message.equals( new HelloWorldArg( 1, "Hello" ) )) {
            die();
        }
    }



    @Test
    public void testComplex2() {
        HelloWorldArg message = (HelloWorldArg) Invoker.invokeFromObject( new HelloWorld(), "sayArg2",
                Lists.list( "1", "Hello" ) );


        if (!message.equals( new HelloWorldArg( 1, "Hello" ) )) {
            die();
        }
    }



    @Test
    public void testComplex3() {
        HelloWorldArg message = (HelloWorldArg) Invoker.invokeFromList( new HelloWorld(), "sayArg2",
                Lists.list( (Object)Lists.list( "1", "Hello" ) ) );


        if (!message.equals( new HelloWorldArg( 1, "Hello" ) )) {
            die();
        }
    }



    @Test
    public void testComplex4() {
        HelloWorldArg message = (HelloWorldArg) Invoker.invokeFromList( new HelloWorld(), "sayArg2",
                Lists.list( Maps.map("i", "1", "hello", "Hello")));


        if (!message.equals( new HelloWorldArg( 1, "Hello" ) )) {
            die();
        }
    }



    @Test
    public void testComplex5() {
        HelloWorldArg message = (HelloWorldArg) Invoker.invokeFromList( new HelloWorld(), "sayArg2",
                Lists.list( Maps.map( "i", "1", "hello", "Hello" )));


        if (!message.equals( new HelloWorldArg( 1, "Hello" ) )) {
            die();
        }
    }

}


