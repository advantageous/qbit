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

import org.junit.Before;
import org.junit.Test;



public class ProcessRunnerTest {

    @Before
    public void init() {

    }


    @Test
    public void run() {
        //Runner.runShell( "ls -l" );
    }

    @Test
    public void runWithTimeout() {
        //puts( Runner.runShell( 1, "ls -l" ) );
    }

    private void puts( Object s ) {
    }

    @Test
    public void runExec() {
        //puts( Runner.execShell( "ls -l" ) );
    }


    @Test
    public void runExecTimeout() {
        //puts( Runner.execShell( 1, "ls -l" ) );
    }


//    @Test (expected = Runner.ProcessException.class)
//    public void runRunTimeoutFails() {
//        //puts( Runner.run( 1, "sleep 20" ) );
//    }


//    @Test  (expected = Runner.ProcessException.class)
//    public void execTimeoutFails () {
//        outputs ( Runner.exec ( 1, "sleep 20" ) );
//    }


//    @Test
//    public void execComplex() throws InterruptedException {
//        final Runner.ProcessInOut inOut = Runner.launchProcess ( 20, null, false, "lsof" );
//
//        String str ="";
//
//        while (str!=null && !inOut.isDone ()) {
//            str = inOut.getStdOut ().take ();
//            outputs (str);
//        }
//
//        str = inOut.getStdErr ().poll (1L, TimeUnit.SECONDS);
//        System.err.println(str);
//
//        System.out.println(inOut.processOut ().getStdout ());
//
//
//    }


    public static void main( String... args ) {
        //System.out.println( Runner.run( "date +%s" ) );
    }

}
