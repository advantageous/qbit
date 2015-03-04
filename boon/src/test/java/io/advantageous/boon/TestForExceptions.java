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


import io.advantageous.boon.Exceptions;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.tryIt;

public class TestForExceptions {


    public void methodThatThrowsException() throws IOException {

        throw new IOException( "Bad stuff happens to good people" );

    }



    public void methodThatThrowsException0() throws IOException {

       IOException ioe =  new IOException( "ROOT Bad stuff happens to good people" );
       ioe.fillInStackTrace();
       throw ioe;

    }


    public void methodThatThrowsException1() throws IOException {

        try {
            methodThatThrowsException0();
        } catch ( IOException e ) {

            Exceptions.handle("LEVEL 1", e);
        }

    }



    public void methodThatThrowsException2() throws IOException {

        try {
            methodThatThrowsException1();
        } catch ( IOException e ) {

            Exceptions.handle( "LEVEL 2", e );
        }
    }




    public void methodThatThrowsException3() throws IOException {

        try {
            methodThatThrowsException2();
        } catch ( IOException e ) {

            Exceptions.handle( "LEVEL 3", e );
        }
    }

    @Test (expected = Exceptions.SoftenedException.class)
    public void deepNestTest() {

        try {

            methodThatThrowsException3();
        } catch ( Exception e ) {
            e.printStackTrace();
            Exceptions.handle( "well that did not work very well", e );
        }

    }


    @Test
    public void deepNestTestJSON() {

        try {

            methodThatThrowsException3();
        } catch ( Exception e ) {
            puts(Exceptions.asJson(e));
        }

    }

    @Test (expected = Exceptions.SoftenedException.class)
    public void testException() {

        try {
            methodThatThrowsException();
        } catch ( IOException e ) {
            e.printStackTrace();
            Exceptions.handle( e );
        }

    }

    @Test (expected = Exceptions.SoftenedException.class)
    public void die() {

        try {
            Exceptions.die( "Die" );
        } catch ( Exception e ) {
            Exceptions.handle( e );
        }

    }


    @Test (expected = Exceptions.SoftenedException.class)
    public void testExceptionWithMessage() {

        try {
            methodThatThrowsException();
        } catch ( IOException e ) {
            e.printStackTrace();
            Exceptions.handle( "well that did not work very well", e );
        }

    }





    @Test (expected = Exceptions.SoftenedException.class)
    public void testTryIt() {

        tryIt( new Exceptions.Trial() {
            @Override
            public void tryIt() throws Exception {
                methodThatThrowsException();
            }
        } );

    }

    @Test
    public void testTryItNoOp() {

        Exceptions.tryIt( new Exceptions.Trial() {
            @Override
            public void tryIt() throws Exception {
            }
        } );

    }

    @Test
    public void testTryItNoOp2WithMessage() {

        tryIt( "no op", new Exceptions.Trial() {
            @Override
            public void tryIt() throws Exception {
            }
        } );

    }


    @Test (expected = Exceptions.SoftenedException.class)
    public void testTryItWithMessage() {

        tryIt( "Calling method that throws exception", new Exceptions.Trial() {
            @Override
            public void tryIt() throws Exception {
                methodThatThrowsException();
            }
        } );

    }

    @Test     //mostly just checking for null checks
    public void testSupportMethods() {

        try {
            tryIt( new Exceptions.Trial() {
                @Override
                public void tryIt() throws Exception {
                    methodThatThrowsException();
                }
            } );
        } catch ( Exception ex ) {
            ex.printStackTrace();


            ex.printStackTrace( new PrintWriter( new StringWriter() ) );

            ex.printStackTrace( System.out );
            StackTraceElement[] stackTrace = ex.getStackTrace();
            for ( StackTraceElement aStackTrace : stackTrace ) {
                System.out.println( "\t\t" + aStackTrace );
            }

            System.out.println( ex.getMessage() );
        }

        Exception ex = new Exceptions.SoftenedException( "" );
        ex.printStackTrace();
        ex.printStackTrace( System.out );


        ex.printStackTrace( new PrintWriter( new StringWriter() ) );

        StackTraceElement[] stackTrace = ex.getStackTrace();
        for ( StackTraceElement aStackTrace : stackTrace ) {
            System.out.println( "\t\t" + aStackTrace );
        }
        System.out.println( ex.getMessage() );


        ex = new Exceptions.SoftenedException( "Foobar" );

        ex.printStackTrace( new PrintWriter( new StringWriter() ) );

        ex.printStackTrace();
        ex.printStackTrace( System.out );
        stackTrace = ex.getStackTrace();
        for ( StackTraceElement aStackTrace : stackTrace ) {
            System.out.println( "\t\t" + aStackTrace );
        }
        System.out.println( ex.getMessage() );

    }


    @Test
    public void testPrintWriter() {

    }

}
