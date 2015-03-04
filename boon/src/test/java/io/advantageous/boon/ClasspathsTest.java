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


import io.advantageous.boon.Classpaths;
import io.advantageous.boon.IO;
import io.advantageous.boon.Str;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.primitive.Chr.multiply;

public class ClasspathsTest {

    @Test
    public void test() throws Exception {
        final List<URL> urls = Classpaths.classpathResources(this.getClass(), "testfile.txt");

        URL url = urls.get( 0 );

        boolean ok = true;

        ok |= Str.in("apple", IO.read(url.openStream())) || die();

    }

    @Test
    public void test1() throws Exception {
        final List<URL> urls = Classpaths.classpathResources( this.getClass(), "org/node/file1.txt" );

        URL url = urls.get( 0 );

        boolean ok = true;

        ok |= Str.in( "abc", IO.read( url.openStream() ) ) || die();

    }


    @Test
    public void test2() throws Exception {
        final List<URL> urls = Classpaths.classpathResources( this.getClass(), "/org/node/file1.txt" );

        URL url = urls.get( 0 );

        boolean ok = true;

        ok |= Str.in( "abc", IO.read( url.openStream() ) ) || die();

    }

    @Test
    public void test2NoRoot() throws Exception {
        final List<URL> urls = Classpaths.classpathResources( this.getClass(), "org/node/file1.txt" );

        URL url = urls.get( 0 );

        boolean ok = true;

        ok |= Str.in( "abc", IO.read( url.openStream() ) ) || die();

    }

    @Test
    public void testResourcesFromPath() throws Exception {
        final List<String> paths = Classpaths.resources( this.getClass(), "/org/node/file1.txt" );

        String path = paths.get( 0 );

        boolean ok = true;

        ok |= Str.in( "abc", IO.read( path ) ) || die();

    }

    @Test   //not root
    public void testResourcesFromPathNoRoot() throws Exception {
        final List<String> paths = Classpaths.resources( this.getClass(), "org/node/file1.txt" );

        String path = paths.get( 0 );

        boolean ok = true;

        ok |= Str.in( "abc", IO.read( path ) ) || die();

    }

    @Test
    public void testDirectory() throws Exception {
        String someResource = "/org/node/";

        File file = new File( "files/node-1.0-SNAPSHOT.jar" );
        URL url1 = file.getAbsoluteFile().toURI().toURL();
        URL url2 = new File( "files/invoke-1.0-SNAPSHOT.jar" ).getAbsoluteFile().toURI().toURL();


        URLClassLoader loader = new URLClassLoader( new URL[]{ url1, url2 } );

        final List<String> resourcePaths = Classpaths.listFromClassLoader(loader, someResource);

        int directoryCount = 0;
        for ( String path : resourcePaths ) {
            if ( !Files.isDirectory( IO.path(path) ) ) {
                die();
            } else {
                directoryCount++;
            }
        }

        boolean ok = true;


//        ok |= directoryCount == 3 || die(directoryCount);


    }


    @Test
    public void testFileResources() throws Exception {
        String someResource = "/org/node/resource.txt";

        File file = new File( "files/node-1.0-SNAPSHOT.jar" );
        URL url1 = file.getAbsoluteFile().toURI().toURL();
        URL url2 = new File( "files/invoke-1.0-SNAPSHOT.jar" ).getAbsoluteFile().toURI().toURL();


        URLClassLoader loader = new URLClassLoader( new URL[]{ url1, url2 } );

        final List<String> resourcePaths = Classpaths.listFromClassLoader(loader, someResource);

        int fileCount = 0;
        int dirCount = 0;
        for ( String path : resourcePaths ) {
            if ( !Files.isDirectory( IO.path(path) ) ) {
                fileCount++;
            } else {
                dirCount++;
            }
        }

        boolean ok = true;

        ok |= dirCount == 0 || die();


//        ok |= fileCount == 2 || die();


    }


    @Test
    public void testFileResources2() throws Exception {
        String someResource = "/org/node/resource.txt";

        File file = new File( "files/node-1.0-SNAPSHOT.jar" );
        URL url1 = file.getAbsoluteFile().toURI().toURL();
        URL url2 = new File( "files/invoke-1.0-SNAPSHOT.jar" ).getAbsoluteFile().toURI().toURL();


        URLClassLoader loader = new URLClassLoader( new URL[]{ url1, url2 } );

        final List<String> resourcePaths = Classpaths.listFromClassLoader(loader, someResource);


        List<Path> list = IO.paths("classpath://org/node/");

        //List<Path> list = Classpaths.pathsFromClassLoader(loader, someResource);


        puts (multiply('-', 10), "Path ");
        for (Path path : list) {

            puts(path, path.getFileSystem(), path.getClass().getName());
            if (path.toString().endsWith(".txt")) {
                puts(IO.readPath(path));
            }
        }

        puts (multiply('-', 10), "String Path ");
        List<String> slist = IO.list("classpath://org/node/");

        for (String spath : slist) {

            puts(spath);
            if (spath.toString().endsWith(".txt")) {
                puts(IO.readResource(spath));
            }
        }


    }

}
