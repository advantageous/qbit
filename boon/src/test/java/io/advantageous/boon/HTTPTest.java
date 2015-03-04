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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.advantageous.boon.HTTP;
import io.advantageous.boon.IO;
import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Maps.copy;
import static io.advantageous.boon.Maps.map;
import static org.junit.Assert.assertTrue;

public class HTTPTest {


    static class MyHandler2 implements HttpHandler {
        public void handle( HttpExchange t ) throws IOException {

            String contentType = null;
            final List<String> strings = t.getRequestHeaders().get( "Content-Type" );
            if ( strings.size() > 0 ) {
                contentType = strings.get( 0 );
            }

            if ( contentType == null ) {
                testResponse( t );
            } else if ( contentType.equals( "application/java-archive" ) ) {
                binaryResponse( t );

            } else {
                testResponse( t );
            }
        }

        private void binaryResponse( HttpExchange t ) throws IOException {

            int boonSize = -1;

            final List<String> strings = t.getRequestHeaders().get( "Boon-Size" );
            if ( strings != null && strings.size() > 0 ) {
                boonSize = Integer.parseInt( strings.get( 0 ) );
            }

            boonSize = boonSize == -1 ? 19 : boonSize;


            t.getResponseHeaders().put( "Content-Type", Lists.list("application/java-archive") );
            t.sendResponseHeaders( 200, boonSize );

            OutputStream os = t.getResponseBody();

            byte[] buffer = new byte[ boonSize ];

            int value = 0;
            for ( int index = 0; index < boonSize; index++, value++ ) {
                buffer[ index ] = ( byte ) value;
                if ( value == Byte.MAX_VALUE ) {
                    value = 0;
                }
            }
            os.write( buffer );
            os.close();
        }

        private void testResponse( HttpExchange t ) throws IOException {

            if ( t.getRequestMethod().equals( "GET" ) ) {

                Headers requestHeaders = t.getRequestHeaders();
                String body = "";
                body = body + "\n" + copy( requestHeaders ).toString();
                body = body + "\n\n" + "Boon test";

                byte[] buffer = body.getBytes( "UTF-8" );
                t.sendResponseHeaders( 200, buffer.length );

                try ( OutputStream os = t.getResponseBody() ) {

                    os.write( buffer );
                    os.flush();
                }


            } else if ( t.getRequestMethod().equals( "POST" ) ) {
                InputStream requestBody = t.getRequestBody();
                String body = IO.read( requestBody );
                Headers requestHeaders = t.getRequestHeaders();
                body = body + "\n" + copy( requestHeaders ).toString();
                body = body + "\n\n" + "Boon test";

                byte[] buffer = body.getBytes( "UTF-8" );
                t.sendResponseHeaders( 200, buffer.length );
                OutputStream os = t.getResponseBody();
                os.write( buffer );
                os.close();

            }
        }
    }


    static class MyHandler implements HttpHandler {
        public void handle( HttpExchange t ) throws IOException {

            InputStream requestBody = t.getRequestBody();
            String body = IO.read( requestBody );
            Headers requestHeaders = t.getRequestHeaders();
            body = body + "\n" + copy( requestHeaders ).toString();
            t.sendResponseHeaders( 200, body.length() );
            OutputStream os = t.getResponseBody();
            os.write( body.getBytes() );
            os.close();
        }
    }


    @Test
    public void testBinary() throws Exception {

        HttpServer server = HttpServer.create( new InetSocketAddress( 7212 ), 0 );
        server.createContext( "/test", new MyHandler2() );
        server.setExecutor( null ); // creates a default executor
        server.start();

        Thread.sleep( 10 );

        byte[] response = HTTP.getBytesWithHeaders(
                "http://localhost:7212/test",
                "application/java-archive",
                Maps.map(
                        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                        "Accept-Encoding", "gzip,deflate,sdch",
                        "Accept-Language", "en-US,en;q=0.8"
                )
        );


        boolean ok = true;

        ok &= response.length == 19 || die( "response is the wrong length" + response.length );

        for ( int index = 0; index < 19; index++ ) {
            ok &= response[ index ] == index || die( sputs( "index", index, "ressponse at index", response[ index ] ) );

        }
        Thread.sleep( 10 );

        //uts( "binary test passed", ok );

        server.stop( 0 );


    }


    @Test
    public void testPostBody() throws Exception {

        HttpServer server = HttpServer.create( new InetSocketAddress( 9290 ), 0 );
        server.createContext( "/test", new MyHandler() );
        server.setExecutor( null ); // creates a default executor
        server.start();

        Thread.sleep( 10 );


        String response = HTTP.post( "http://localhost:9290/test", "hi mom" );

        assertTrue( response.contains( "hi mom" ) );


        Thread.sleep( 10 );

        server.stop( 0 );


    }


    @Test
    public void testPostForm() throws Exception {

        HttpServer server = HttpServer.create( new InetSocketAddress( 9220 ), 0 );
        server.createContext( "/test", new MyHandler() );
        server.setExecutor( null ); // creates a default executor
        server.start();

        Thread.sleep( 10 );


        String response = HTTP.postForm( "http://localhost:9220/test",
                Collections.EMPTY_MAP,
                map( "hI", ( Object ) "hi-mom", "image", new byte[]{ 1, 2, 3 } )
        );

        boolean ok = true;
        ok |= response.startsWith( "hI=hi-mom&image=%01%02%03" ) ||
                die( "encoding did not work --" + response + "--" );

        Thread.sleep( 10 );

        server.stop( 0 );


    }

    @Test ( expected = RuntimeException.class )
    public void testSad() throws Exception {

        HttpServer server = HttpServer.create( new InetSocketAddress( 9213 ), 0 );
        server.createContext( "/test", new MyHandler() );
        server.setExecutor( null ); // creates a default executor
        server.start();

        Thread.sleep( 10 );


        Map<String, String> headers = map( "foo", "bar", "fun", "sun" );

        String response = HTTP.postWithContentType( "http://localhost:9213/foo", headers, "text/plain", "hi mom" );

        System.out.println( response );

        assertTrue( response.contains( "hi mom" ) );
        assertTrue( response.contains( "Fun=[sun], Foo=[bar]" ) );

        Thread.sleep( 10 );

        server.stop( 0 );


    }

    @Test
    public void testHappyFeet() throws Exception {

        HttpServer server = HttpServer.create( new InetSocketAddress( 8888 ), 0 );
        server.createContext( "/test", new MyHandler() );
        server.setExecutor( null ); // creates a default executor
        server.start();

        Thread.sleep( 10 );

        Map<String, String> headers = map( "foo", "bar", "fun", "sun" );


        String response = HTTP.get( "http://localhost:8888/test" );


        System.out.println( response );


        response = HTTP.getWithHeaders( "http://localhost:8888/test", headers );

        System.out.println( response );

        assertTrue( response.contains( "Fun=[sun]" ) );
        assertTrue( response.contains( "Foo=[bar]" ) );


        response = HTTP.getWithContentType( "http://localhost:8888/test", headers, "text/plain" );

        System.out.println( response );

        assertTrue( response.contains( "Fun=[sun]" ) );
        assertTrue( response.contains( "Foo=[bar]" ) );


        response = HTTP.getWithCharSet( "http://localhost:8888/test", headers, "text/plain", "UTF-8" );

        System.out.println( response );

        assertTrue( response.contains( "Fun=[sun]" ) );
        assertTrue( response.contains( "Foo=[bar]" ) );


    }

    @Test
    public void testHappy() throws Exception {

        HttpServer server = HttpServer.create( new InetSocketAddress( 9212 ), 0 );
        server.createContext( "/test", new MyHandler() );
        server.setExecutor( null ); // creates a default executor
        server.start();

        Thread.sleep( 10 );


        Map<String, String> headers = map( "foo", "bar", "fun", "sun" );

        String response = HTTP.postWithContentType( "http://localhost:9212/test", headers, "text/plain", "hi mom" );

        System.out.println( response );

        assertTrue( response.contains( "hi mom" ) );
        assertTrue( response.contains( "Fun=[sun]" ) );
        assertTrue( response.contains( "Foo=[bar]" ) );


        response = HTTP.postWithCharset( "http://localhost:9212/test", headers, "text/plain", "UTF-8", "hi mom" );

        System.out.println( response );

        assertTrue( response.contains( "hi mom" ) );
        assertTrue( response.contains( "Fun=[sun]" ) );
        assertTrue( response.contains( "Foo=[bar]" ) );

        response = HTTP.postWithHeaders( "http://localhost:9212/test", headers, "hi mom" );

        System.out.println( response );

        assertTrue( response.contains( "hi mom" ) );
        assertTrue( response.contains( "Fun=[sun]" ) );
        assertTrue( response.contains( "Foo=[bar]" ) );


        Thread.sleep( 10 );

        server.stop( 0 );


    }


    //@Test
    public void damnYouChris() {
        final String s = HTTP.get("https://login.yahoo.com");
        puts(s);
    }



}
