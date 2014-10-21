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

package org.boon.qbit.vertx.integration.server;

import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.Str;
import org.boon.primitive.Arry;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.boon.Boon.puts;

public class QBitServiceMain {



    public static void main(String... args) throws Exception {

        RegisterBoonWithQBit.registerBoonWithQBit();

        runServer("qbit", "emp", "1.0");

        // Prevent the JVM from exiting
        System.in.read();
    }

    static void runServer(String owner, String serverName, String version) {
        PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();
        platformManager.deployModuleFromClasspath(
                Str.add(owner, "~", serverName, "~", version),
                (JsonObject) null, 1, classpath(), null);
    }


    static URL[] classpath() {
        String classPathParts = System.getProperty("java.class.path");
        String[] split = classPathParts.split(":");
        List<URL> urls = new ArrayList<>(split.length);

        for (String classPathPart : split) {
            try {
                urls.add(new File(classPathPart).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return Arry.array(urls);
    }

}
