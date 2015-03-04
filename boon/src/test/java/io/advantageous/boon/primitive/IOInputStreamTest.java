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

package io.advantageous.boon.primitive;

import io.advantageous.boon.primitive.IOInputStream;
import io.advantageous.boon.primitive.InMemoryInputStream;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by Richard on 3/11/14.
 */
public class IOInputStreamTest {

    IOInputStream ioInputStream;

    @Test
    public void test() throws IOException {

        byte[] content = new String("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz").getBytes(StandardCharsets.UTF_8);


        byte [] readBy = new byte[4];

        InMemoryInputStream stream = new InMemoryInputStream(content);

        ioInputStream = IOInputStream.input(ioInputStream, 10).input(stream);


        for (int index=0; index < 25; index++) {



            int read = ioInputStream.read(readBy, 0, readBy.length);

            puts(read, readBy, new String(readBy, StandardCharsets.UTF_8));

        }


    }
}
