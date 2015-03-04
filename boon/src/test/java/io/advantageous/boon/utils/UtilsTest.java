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

package io.advantageous.boon.utils;

import io.advantageous.boon.Str;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testCamel() throws Exception {
        String myFoo = "MY FOO_BAR_FUN\t_STUFF";

        String camelCaseUpper = Str.camelCaseUpper( myFoo );
        assertEquals( "MyFooBarFunStuff", camelCaseUpper );

        String camelCaseLower = Str.camelCaseLower( myFoo );
        assertEquals( "myFooBarFunStuff", camelCaseLower );

    }

    @Test
    public void testUnderBarCase() throws Exception {
        String myFoo = "FooFunFaceFact";

        String underBar = Str.underBarCase( myFoo );
        assertEquals( "FOO_FUN_FACE_FACT", underBar );

    }

    @Test
    public void testUnderBarCase2() throws Exception {
        String myFoo = "FooFunFaceFact Fire Free FOO foo\tbar";

        String underBar = Str.underBarCase( myFoo );
        assertEquals( "FOO_FUN_FACE_FACT_FIRE_FREE_FOO_FOO_BAR", underBar );

    }

}
