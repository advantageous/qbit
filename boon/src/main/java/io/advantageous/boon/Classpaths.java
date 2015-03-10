
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


import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;

import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Exceptions.requireNonNull;
import static io.advantageous.boon.Lists.*;

public class Classpaths {


    public static List<URL> classpathResources( ClassLoader loader, String resource ) {
        try {

            Enumeration<URL> resources = loader.getResources( resource );
            List<URL> list = list( resources );

            if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
                resource = resource.substring( 1 );
                return classpathResources( loader, resource );
            }

            return list;


        } catch ( Exception ex ) {

            return Exceptions.handle( List.class, Boon.sputs("Unable to load listFromClassLoader for", resource),
                    ex );
        }


    }

    public static List<URL> classpathResources( Class<?> clazz, String resource ) {


        List<URL> list = classpathResources( Thread.currentThread().getContextClassLoader(), resource );

        if ( isEmpty( list ) ) {
            list = classpathResources( clazz.getClassLoader(), resource );
        }


        if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
            resource = resource.substring( 1 );
            return classpathResources( clazz, resource );
        }

        return list;
    }

    public static List<String> resources( Class<?> clazz, String resource ) {


        List<String> list = listFromClassLoader(Thread.currentThread().getContextClassLoader(), resource);

        if ( isEmpty( list ) ) {
            list = listFromClassLoader(clazz.getClassLoader(), resource);
        }


        if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
            resource = resource.substring( 1 );
            return resources( clazz, resource );
        }

        return list;
    }


    public static List<Path> paths( Class<?> clazz, String resource ) {


        List<Path> list = pathsFromClassLoader(Thread.currentThread().getContextClassLoader(), resource);

        if ( isEmpty( list ) ) {
            list = pathsFromClassLoader(clazz.getClassLoader(), resource);
        }


        if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
            resource = resource.substring( 1 );
            return paths( clazz, resource );
        }

        return list;
    }

    /**
     * Load the listFromClassLoader
     * @param loader loader
     * @param resource resource
     * @return list of strings
     */
    public static List<String> listFromClassLoader(ClassLoader loader, String resource) {
        final List<URL> resourceURLs = Classpaths.classpathResources( loader, resource );
        final List<String> resourcePaths = Lists.list( String.class );
        final Map<URI, FileSystem> pathToZipFileSystems = new HashMap<>(); //So you don't have to keep loading the same jar/zip file.
        for ( URL resourceURL : resourceURLs ) {

            if ( resourceURL.getProtocol().equals( "jar" ) ) {
                resourcesFromJar( resourcePaths, resourceURL, pathToZipFileSystems );

            } else {
                resourcesFromFileSystem( resourcePaths, resourceURL );
            }
        }
        return resourcePaths;
    }


    /**
     * Load the listFromClassLoader
     * @param loader loader
     * @param resource resource
     * @return array of strings
     */
    public static List<Path> pathsFromClassLoader(ClassLoader loader, String resource) {
        final List<URL> resourceURLs = Classpaths.classpathResources( loader, resource );
        final List<Path> resourcePaths = Lists.list( Path.class );
        final Map<URI, FileSystem> pathToZipFileSystems = new HashMap<>(); //So you don't have to keep loading the same jar/zip file.
        for ( URL resourceURL : resourceURLs ) {

            if ( resourceURL.getProtocol().equals( "jar" ) ) {
                pathsFromJar( resourcePaths, resourceURL, pathToZipFileSystems );

            } else {
                pathsFromFileSystem( resourcePaths, resourceURL );
            }
        }
        return resourcePaths;
    }



    private static void resourcesFromFileSystem( List<String> resourcePaths, URL u ) {
        URI fileURI = IO.createURI( u.toString() );


        add( resourcePaths, IO.uriToPath( fileURI ).toString() );
    }



    private static void pathsFromFileSystem( List<Path> resourcePaths, URL u ) {
        URI fileURI = IO.createURI( u.toString() );


        add( resourcePaths, IO.uriToPath( fileURI ) );
    }

    private static void resourcesFromJar( List<String> resourcePaths, URL resourceURL, Map<URI, FileSystem> pathToZipFileSystems ) {

        String str = resourceURL.toString();

        final String[] strings = StringScanner.split( str, '!' );

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( fileJarURI, IO.zipFileSystem(fileJarURI) );
        }

        FileSystem fileSystem = pathToZipFileSystems.get( fileJarURI );

        Path path = fileSystem.getPath(resourcePath);

        if (path != null) {
            add( resourcePaths, str);
        }
    }

    private static void pathsFromJar( List<Path> resourcePaths, URL resourceURL, Map<URI, FileSystem> pathToZipFileSystems ) {

        String str = resourceURL.toString();

        final String[] strings = StringScanner.split( str, '!' );

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( fileJarURI, IO.zipFileSystem(fileJarURI) );
        }

        FileSystem fileSystem = pathToZipFileSystems.get( fileJarURI );

        Path path = fileSystem.getPath(resourcePath);

        if (path != null) {
            add( resourcePaths, path);
        }
    }


    private static void resourcePathsFromJar( List<Path> resourcePaths, URL resourceURL, Map<URI, FileSystem> pathToZipFileSystems ) {

        String str = resourceURL.toString();

        final String[] strings = StringScanner.split( str, '!' );

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( fileJarURI, IO.zipFileSystem(fileJarURI) );
        }

        FileSystem fileSystem = pathToZipFileSystems.get( fileJarURI );

        Path path = fileSystem.getPath(resourcePath);

        if (path != null) {
            add( resourcePaths, path);
        }
    }

}
