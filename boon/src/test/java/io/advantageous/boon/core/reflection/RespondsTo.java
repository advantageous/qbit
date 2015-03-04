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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Str.lines;
import static io.advantageous.boon.core.reflection.Reflection.handles;
import static io.advantageous.boon.core.reflection.Reflection.invoke;
import static io.advantageous.boon.core.reflection.Reflection.respondsTo;

public class RespondsTo {

    public static interface FileInterface {
        void open(String fileName);

        String readLine();

        void close();
    }


    public static class FileObject {

        boolean readLine;
        boolean openCalled;
        boolean closeCalled;

        boolean addCalled;

        public void open(String fileName) {

            openCalled = true;
            puts("Open ", fileName);
        }

        public String readLine() {
            readLine = true;
            return "read line";
        }

        public int add(int i, int f) {
           addCalled = true;
           return i + f;
        }

        public void close() {
            closeCalled = true;
        }


    }

    public static class StringReaderThing {

        BufferedReader reader = null;

        String contents;

        boolean readLine;
        boolean openCalled;
        boolean closeCalled;

        boolean addCalled;

        public StringReaderThing(String contents) {
            this.contents = contents;
        }

        public void open(String fileName) {

            openCalled = true;
            reader = new BufferedReader(new StringReader(contents));

        }

        public String readLine() throws IOException {
            readLine = true;
            return reader.readLine();
        }

        public void close() throws IOException {
            closeCalled = true;
            reader.close();
        }

    }



    boolean ok;
    @Test
    public void test() {
        FileObject file = new FileObject();
        StringReaderThing reader = new StringReaderThing(lines("Hi mom", "how are you?"));
        List<Object> list = list(file, reader);


        for (Object object : list) {
            if ( respondsTo(object, "open", String.class) ) invoke(object, "open", "hi");

//            ok = reader.openCalled || die("Open not called");

            if ( respondsTo(object, "add", int.class, int.class) ) puts ("add", invoke(object, "add", 1, 2));


//            ok = reader.addCalled || die("Add not called");

            if ( respondsTo(object, "readLine") ) puts ( invoke(object, "readLine") );


//            ok = reader.readLine || die("Read Line not called");



            if ( respondsTo(object, "close" ) ) invoke(object, "close");

//            ok = reader.closeCalled || die("Close not called");

        }

        boolean ok = file.closeCalled && file.readLine && file.openCalled && file.addCalled || die();


    }



    @Test
    public void testInvoke() {
        FileObject file = new FileObject();
        StringReaderThing reader = new StringReaderThing(lines("Hi mom", "how are you?"));
        List<Object> list = list(file, reader);


        for (Object object : list) {
            if (respondsTo(object, "open", "hi") ) invoke(object, "open", "hi");

            if (respondsTo(object, "add", 1, 2) ) puts ("add", invoke(object, "add", 1, 2));


            if (respondsTo(object, "readLine") ) puts ( invoke(object, "readLine") );

            if (respondsTo(object, "readLine") ) puts ( invoke(object, "readLine") );

            if (respondsTo(object, "close" ) ) invoke(object, "close");
        }

        boolean ok = file.closeCalled && file.readLine && file.openCalled && file.addCalled || die();



    }


    @Test
    public void testInvokeByList() {
        FileObject file = new FileObject();
        StringReaderThing reader = new StringReaderThing(lines("Hi mom", "how are you?"));
        List<Object> list = list(file, reader);



        List <?> openList = list("hi");
        List <?> addList = list(1, 2);

        for (Object object : list) {
            if (respondsTo(object, "open", openList) ) invoke(object, "open", openList);

            if (respondsTo(object, "add", addList) ) puts ("add", invoke(object, "add", addList));


            if (respondsTo(object, "readLine") ) puts ( invoke(object, "readLine") );

            if (respondsTo(object, "readLine") ) puts ( invoke(object, "readLine") );

            if (respondsTo(object, "close" ) ) invoke(object, "close");
        }

        boolean ok = file.closeCalled && file.readLine && file.openCalled && file.addCalled || die();



    }



    @Test
    public void testHandles() {
        FileObject file = new FileObject();
        StringReaderThing reader = new StringReaderThing(lines("Hi mom", "how are you?"));
        List<Object> list = list(file, reader);



        List <?> openList = list("hi");
        List <?> addList = list(1, 2);

        for (Object object : list) {
            if ( handles(object, FileInterface.class) ) invoke(object, "open", openList);

            if ( respondsTo(object, "add", addList) ) puts ("add", invoke(object, "add", addList));


            if ( handles(object, FileInterface.class) ) {
                puts ( invoke(object, "readLine") );
                puts ( invoke(object, "readLine") );
                invoke(object, "close");
            }
        }

        boolean ok = file.closeCalled && file.readLine && file.openCalled && file.addCalled || die();



    }
}
