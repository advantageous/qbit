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

package io.advantageous.boon.json.implementation;

import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.JsonParser;
import io.advantageous.boon.IO;
import io.advantageous.boon.primitive.CharBuf;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Richard on 2/2/14.
 */
public abstract class BaseJsonParser implements JsonParser {


        protected static final int COLON = ':';
        protected static final int COMMA = ',';
        protected static final int CLOSED_CURLY = '}';
        protected static final int CLOSED_BRACKET = ']';

        protected static final int LETTER_E = 'e';
        protected static final int LETTER_BIG_E = 'E';

        protected static final int MINUS = '-';
        protected static final int PLUS = '+';

        protected static final int DECIMAL_POINT = '.';


        protected static final int ALPHA_0 = '0';
        protected static final int ALPHA_1 = '1';
        protected static final int ALPHA_2 = '2';
        protected static final int ALPHA_3 = '3';
        protected static final int ALPHA_4 = '4';
        protected static final int ALPHA_5 = '5';
        protected static final int ALPHA_6 = '6';
        protected static final int ALPHA_7 = '7';
        protected static final int ALPHA_8 = '8';
        protected static final int ALPHA_9 = '9';


        protected static final int DOUBLE_QUOTE = '"';

        protected static final int ESCAPE = '\\';

        protected static final boolean internKeys = Boolean.parseBoolean( System.getProperty( "io.advantageous.boon.json.implementation.internKeys", "false" ) );
        protected static ConcurrentHashMap<String, String> internedKeysCache;

        protected Charset charset  = StandardCharsets.UTF_8;


        protected int bufSize  = 1024;

        protected char[] copyBuf;


        static {
            if ( internKeys ) {
                internedKeysCache = new ConcurrentHashMap<>();
            }
        }



        protected String charDescription( int c ) {
            String charString;
            if ( c == ' ' ) {
                charString = "[SPACE]";
            } else if ( c == '\t' ) {
                charString = "[TAB]";

            } else if ( c == '\n' ) {
                charString = "[NEWLINE]";

            } else {
                charString = "'" + (char)c + "'";
            }

            charString = charString + " with an int value of " + ( ( int ) c );
            return charString;
        }





        public void setCharset( Charset charset ) {
            this.charset = charset;
        }


        @Override
        public Object parse ( String jsonString ) {
            return parse ( FastStringUtils.toCharArray(jsonString) );
        }

        @Override
        public Object parse ( byte[] bytes ) {
            return parse ( bytes, charset );
        }


        @Override
        public Object parse ( CharSequence charSequence ) {
            return parse ( FastStringUtils.toCharArray ( charSequence ) );
        }

        @Override
        public  Object parse(  Reader reader ) {

            if (copyBuf==null) {
                copyBuf = new char[bufSize];
            }

            fileInputBuf = IO.read ( reader, fileInputBuf, bufSize, copyBuf );
            return parse( fileInputBuf.readForRecycle() );

        }

        @Override
        public Object parse ( InputStream input ) {
            return parse ( input, charset );
        }



        @Override
        public Object parse ( InputStream inputStream, Charset charset ) {



            return parse ( new InputStreamReader ( inputStream, charset ) );
        }



        private CharBuf fileInputBuf;



        int[] indexHolder = new int[1];


}
