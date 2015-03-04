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

import io.advantageous.boon.primitive.CharBuf;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static io.advantageous.boon.Maps.map;
import static io.advantageous.boon.primitive.Arry.add;
import static io.advantageous.boon.primitive.Arry.array;
import static io.advantageous.boon.Boon.sputs;
import static io.advantageous.boon.Sets.set;
import static io.advantageous.boon.Str.startsWithItemInCollection;

public class Exceptions {



    private static final Set<String> ignorePackages = set("sun.", "com.sun.",
            "javax.java", "java.",  "oracle.", "com.oracle.", "org.junit", "Exceptions",
            "com.intellij");


    public static void requireNonNull(Object obj) {
        if (obj == null)
            die("Required object assertion exception");
    }

    public static void  requireNonNulls(String message, Object... array) {

        int index = 0;
        for (Object obj : array) {
            if (obj == null)
               die(message, index);

            index++;

        }
    }

    public static void  dieIfAnyParametersAreNull(String methodName, Object... parameters) {

        requireNonNull(sputs("METHOD", methodName, "Parameter at index was null: index="));
    }

    public static void requireNonNull(Object obj, String message) {
        if (obj == null)
            die(message);

    }

    public static boolean die() {
        throw new SoftenedException( "died" );
    }

    public static boolean die( String message ) {
        throw new SoftenedException( message );
    }


    public static boolean die( Object... messages ) {
        throw new SoftenedException( sputs(messages) );
    }


    public static <T> T die( Class<T> clazz, String message ) {
        throw new SoftenedException( message );
    }



    public static <T> T die( Class<T> clazz, Object... messages ) {
        throw new SoftenedException( sputs(messages) );
    }

    public static void handle( java.lang.Exception e ) {
        throw new SoftenedException( e );
    }


    public static <T> T handle( Class<T> clazz, java.lang.Exception e ) {

        if ( e instanceof SoftenedException ) {
            throw ( SoftenedException ) e;
        }
        throw new SoftenedException( e );
    }

    public static <T> T handle( Class<T> clazz, String message, Throwable e ) {

        throw new SoftenedException( message, e );
    }



    public static <T> T handle( Class<T> clazz,  Throwable e, Object... messages ) {

        throw new SoftenedException( sputs(messages), e );
    }

    public static void handle( Throwable e, Object... messages ) {

        throw new SoftenedException( sputs(messages), e );
    }


    public static void printStackTrace(CharBuf charBuf, StackTraceElement[] stackTrace) {
        for (StackTraceElement st : stackTrace) {
            if (st.getClassName().contains("Exceptions")) {
                continue;
            }
            charBuf.indent(10).println(st);
        }
    }

    public static <T> T tryIt( Class<T> clazz, TrialWithReturn<T> tryIt ) {
        try {
            return tryIt.tryIt();
        } catch ( java.lang.Exception ex ) {
            throw new SoftenedException( ex );
        }
    }


    public static void tryIt( Trial tryIt ) {
        try {
            tryIt.tryIt();
        } catch ( java.lang.Exception ex ) {
            throw new SoftenedException( ex );
        }
    }

    public static void handle( String message, Throwable e ) {
        throw new SoftenedException( message, e );
    }

    public static void tryIt( String message, Trial tryIt ) {
        try {
            tryIt.tryIt();
        } catch ( java.lang.Exception ex ) {
            throw new SoftenedException( message, ex );
        }
    }


    public static interface Trial {
        void tryIt() throws java.lang.Exception;
    }

    public static interface TrialWithReturn<T> {
        T tryIt() throws java.lang.Exception;
    }




    public static StackTraceElement[] getFilteredStackTrace(StackTraceElement[] stackTrace) {


        if (stackTrace == null || stackTrace.length == 0) {
            return new StackTraceElement[0];
        }
        List<StackTraceElement> list = new ArrayList<>();
        Set<String> seenThisBefore = new HashSet<>();

        for (StackTraceElement st : stackTrace) {
            if ( startsWithItemInCollection( st.getClassName(), ignorePackages ) ) {

                continue;
            }

            String key =   Boon.sputs(st.getClassName(), st.getFileName(), st.getMethodName(), st.getLineNumber());
            if (seenThisBefore.contains(key)) {
                continue;
            } else {
                seenThisBefore.add(key);
            }

            list.add(st);
        }

        return array( StackTraceElement.class, list );

    }


    public static class SoftenedException extends RuntimeException {

        public SoftenedException( String message ) {
            super( message );
        }

        public SoftenedException( String message, Throwable cause ) {
            super( message, cause );
        }

        public SoftenedException( Throwable cause ) {
            super( "Wrapped Exception", cause );
        }



        @Override
        public String getMessage() {
            return super.getMessage() + ( getCause() == null ? "" :
                    getCauseMessage() );
        }

        private String getCauseMessage() {
            return "\n CAUSE " + getCause().getClass().getName() + " :: " +
                    getCause().getMessage();
        }

        @Override
        public String getLocalizedMessage() {
            return this.getMessage();
        }

        @Override
        public StackTraceElement[] getStackTrace() {
            if ( getRootCause() != null ) {
                return add(getRootCause().getStackTrace(), super.getStackTrace());
            } else {
                return super.getStackTrace();
            }
        }

        @Override
        public Throwable getCause() {
            return super.getCause();
        }


        public Throwable getRootCause() {

            Throwable cause = super.getCause();

            Throwable lastCause = super.getCause();

            while (cause != null) {
               lastCause = cause;
               cause = cause.getCause();

            }
            return lastCause;
        }



        public void printStackTrace( CharBuf charBuf) {


            charBuf.puts("MESSAGE:", this.getMessage());
            if (this.getRootCause() !=null) {
                charBuf.puts("ROOT CAUSE MESSAGE:", this.getRootCause().getMessage());
            } else if (this.getCause()!=null) {
                charBuf.puts("CAUSE MESSAGE:", this.getCause().getMessage());
            }


            StackTraceElement[] stackTrace = this.getFilteredStackTrace();

            if (stackTrace.length > 0) {
                charBuf.indent(5).addLine("This happens around this area in your code.");
                Exceptions.printStackTrace(charBuf, stackTrace);
            }



            if ( getRootCause() != null ) {
                charBuf.addLine().puts("Caused by:", "message:", this.getRootCause().getMessage(), "type", this.getRootCause().getClass().getName());
                stackTrace = this.getRootCause().getStackTrace();
                Exceptions.printStackTrace(charBuf, stackTrace);
            }

            charBuf.addLine().multiply('-', 50).addLine().multiply('-', 50).addLine();

            StringWriter writer = new StringWriter();

            super.printStackTrace( new PrintWriter(writer) );

            charBuf.add(writer);

            charBuf.addLine().multiply('-', 50).addLine();


        }


        public StackTraceElement[] getFilteredStackTrace() {


            StackTraceElement[] filteredStackTrace = Exceptions.getFilteredStackTrace(super.getStackTrace());
            if ( filteredStackTrace.length > 0 ) {

                if (super.getCause() !=  null) {
                    StackTraceElement[] cause = Exceptions.getFilteredStackTrace(super.getCause().getStackTrace());

                    if (cause.length > 0) {
                        filteredStackTrace= add(cause, filteredStackTrace);
                    }
                }
            } else {
                if (super.getCause() !=  null) {

                    filteredStackTrace =  Exceptions.getFilteredStackTrace(super.getCause().getStackTrace());
                }
            }

            return Exceptions.getFilteredStackTrace(super.getStackTrace());

        }


        public CharBuf printStackTraceIntoCharBuf(  ) {

            CharBuf out = CharBuf.create(100);
            printStackTrace(out);
            return out;

        }

        @Override
        public void printStackTrace( PrintStream s ) {
            s.print(printStackTraceIntoCharBuf().toString());
        }


        @Override
        public void printStackTrace( PrintWriter s ) {
            s.print(printStackTraceIntoCharBuf().toString());
        }

        @Override
        public void printStackTrace() {
            System.err.print(printStackTraceIntoCharBuf().toString());
        }
    }


    public static String toString( Exception ex ) {
        CharBuf buffer = CharBuf.create( 255 );
        buffer.addLine( ex.getLocalizedMessage() );

        final StackTraceElement[] stackTrace = ex.getStackTrace();
        for ( StackTraceElement element : stackTrace ) {
            buffer.add( element.getClassName() );
            sputs( "      ", buffer, "class", element.getClassName(),
                    "method", element.getMethodName(), "line", element.getLineNumber() );
        }

        return buffer.toString();

    }


    public static String asJson(Exception ex) {
        CharBuf buffer = CharBuf.create( 255 );

        buffer.add('{');

        buffer.addLine().indent(5).addJsonFieldName("message")
                .asJsonString(ex.getMessage()).addLine(',');

        if (ex.getCause()!=null) {
            buffer.addLine().indent(5).addJsonFieldName("causeMessage")
                    .asJsonString(ex.getCause().getMessage()).addLine(',');


            if (ex.getCause().getCause()!=null) {
                buffer.addLine().indent(5).addJsonFieldName("cause2Message")
                        .asJsonString(ex.getCause().getCause().getMessage()).addLine(',');

                if (ex.getCause().getCause().getCause()!=null) {
                    buffer.addLine().indent(5).addJsonFieldName("cause3Message")
                            .asJsonString(ex.getCause().getCause().getCause().getMessage()).addLine(',');

                    if (ex.getCause().getCause().getCause().getCause()!=null) {
                        buffer.addLine().indent(5).addJsonFieldName("cause4Message")
                                .asJsonString(ex.getCause().getCause().getCause().getCause().getMessage()).addLine(',');

                    }

                }

            }

        }





        StackTraceElement[] stackTrace = getFilteredStackTrace(ex.getStackTrace());

        if ( stackTrace!=null && stackTrace.length > 0 ) {

            buffer.addLine().indent(5).addJsonFieldName("stackTrace").addLine();

            stackTraceToJson(buffer, stackTrace);

            buffer.add(',');
        }

        buffer.addLine().indent(5).addJsonFieldName("fullStackTrace").addLine();
        stackTrace = ex.getStackTrace();
        stackTraceToJson(buffer, stackTrace);

        buffer.add( '}' );
        return buffer.toString();

    }


    public static Map asMap(Exception ex) {
        StackTraceElement[] stackTrace = getFilteredStackTrace(ex.getStackTrace());

        List stackTraceList = Lists.list(stackTrace);

        List fullStackTrace = Lists.list(ex.getStackTrace());


        return map(

                    "message", ex.getMessage(),
                    "causeMessage", ex.getCause()!=null ? ex.getCause().getMessage() : "none",
                    "stackTrace", stackTraceList,
                    "fullStackTrace", fullStackTrace

            );

    }


    public static void stackTraceToJson(CharBuf buffer, StackTraceElement[] stackTrace) {

        if (stackTrace.length==0) {
            buffer.addLine("[]");
            return;
        }


        buffer.multiply(' ', 16).addLine('[');

        for ( int index = 0; index <  stackTrace.length; index++ ) {
            StackTraceElement element = stackTrace[ index ];

            if (element.getClassName().contains("Exceptions")) {
                continue;
            }
            buffer.indent(17).add("[  ").asJsonString(element.getMethodName())
                    .add(',');


            buffer.indent(3).asJsonString(element.getClassName());


            if (element.getLineNumber()>0) {
                buffer.add(",");
                buffer.indent(3).asJsonString(""+element.getLineNumber())
                    .addLine("   ],");
            } else {
                buffer.addLine(" ],");
            }

        }
        buffer.removeLastChar(); //trailing \n
        buffer.removeLastChar(); //trailing ,

        buffer.addLine().multiply(' ', 15).add(']');
    }


}
