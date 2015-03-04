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

package io.advantageous.boon.zipfilesystem;

import io.advantageous.boon.Classpaths;
import io.advantageous.boon.IO;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Lists.add;
import static io.advantageous.boon.Maps.idx;

public class ZipFileSystemTest {


    public static void main( String... args ) throws Exception {


        String someResource = args.length > 0 ? args[ 0 ] :
                "classpath:///org/node/";                 //It also works with directories

        URI someResourceURI = URI.create( someResource );

        System.out.println( "URI of resource = " + someResourceURI );

        someResource = someResourceURI.getPath();

        System.out.println( "PATH of resource =" + someResource );


        File file = new File( "files/node-1.0-SNAPSHOT.jar" );


        URL url = file.getAbsoluteFile().toURI().toURL();


        URLClassLoader loader = new URLClassLoader( new URL[]{ url,
                new File( "files/invoke-1.0-SNAPSHOT.jar" ).getAbsoluteFile().toURI().toURL() } );

        final List<String> resourcePaths = Classpaths.listFromClassLoader(loader, someResource);


        for ( String path : resourcePaths ) {
            if ( !Files.isDirectory( IO.path(path) ) ) {
                puts( IO.read( path ) );
            } else {
                puts( IO.list( path ) );
            }
        }


    }


}
