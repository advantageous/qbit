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

import org.junit.Test;

import java.util.Collection;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 3/7/14.
 */
public class ClassMetaTest {


    public static void hitMeStatic() {}
    boolean ok;

    public static interface MyService {

        void hitMe();
        String hitMeAgain(int i);
    }


    @Test
    public void test() {

        Collection collection = ClassMeta.classMeta(MyService.class).instanceMethods();
        puts (collection);

        ok = collection.size() == 2 || die();



        collection = ClassMeta.classMeta(ClassMetaTest.class).instanceMethods();
        puts (collection);


        ok = collection.size() == 1 || die();


        collection = ClassMeta.classMeta(ClassMetaTest.class).classMethods();
        puts (collection);

        ok = collection.size() == 1 || die();

    }
}
