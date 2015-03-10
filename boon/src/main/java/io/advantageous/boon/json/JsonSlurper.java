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
package io.advantageous.boon.json;

import io.advantageous.boon.IO;
import io.advantageous.boon.HTTP;
import io.advantageous.boon.json.implementation.JsonParserUsingCharacterSource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * JSON slurper which parses text or reader content into a data structure of lists and maps.
 * <p>
 * Example usage:
 * </p>
 *
 *
 * def slurper = new JsonSlurper()
 * def result = slurper.parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
 *
 * assert result.person.name == "Guillaume"
 * assert result.person.age == 33
 * assert result.person.pets.size() == 2
 * assert result.person.pets[0] == "dog"
 * assert result.person.pets[1] == "cat"
 *
 * @author Guillaume Laforge
 * @author Rick Hightower
 * @since 1.8.0
 */
public class JsonSlurper {

    /**
     * Parse a text representation of a JSON data structure
     *
     * @param text JSON text to parse
     * @return a data structure of lists and maps
     */
    public Object parseText(String text) {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("The JSON input text should neither be null nor empty.");
        }

        return JsonFactory.create().fromJson ( text );
    }

    /**
     * Parse a JSON data structure from content from a reader
     *
     * @param reader reader over a JSON content
     * @return a data structure of lists and maps
     */
    public Object parse(Reader reader) {
         return new JsonParserUsingCharacterSource (  ).parse ( reader );
    }


    /**
     * Parse a JSON data structure from content within a given File.
     *
     * @param file File containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(File file) {
        return parseFile(file, null);
    }

    /**
     * Parse a JSON data structure from content within a given File.
     *
     * @param file File containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(File file, String charset) {
        return parseFile(file, charset);
    }

    /** Slight changes to remove groovy dependencies .*/
    private Object parseFile(File file, String scharset) {
        Charset charset = scharset==null || scharset.length ()==0 ? StandardCharsets.UTF_8 : Charset.forName ( scharset );

        if (file.length() > 2_000_000) {
            try (Reader reader = Files.newBufferedReader( IO.path ( file.toString () ), charset )) {
                return parse(reader);
            } catch(IOException ioe) {
                throw new JsonException("Unable to process file: " + file.getPath(), ioe);
            }
        } else {
            try {
                return JsonFactory.create().fromJson ( Files.newBufferedReader( IO.path ( file.toString () ), charset ) );
            } catch ( IOException e ) {
                throw new JsonException("Unable to process file: " + file.getPath(), e);
            }
        }
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url) {
        return parseURL(url, null);
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param params connection parameters
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, Map params) {
        return parseURL(url, params);
    }

    /**
     * Parse a JSON data structure from content at a given URL. Convenience variant when using Groovy named parameters for the connection params.
     *
     * @param params connection parameters
     * @param url URL containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(Map params, URL url) {
        return parseURL(url, params);
    }

    private Object parseURL(URL url, Map params) {
            return parse(new StringReader ( IO.read ( url.toString () )));
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, String charset) {
        return parseURL(url, null, charset);
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param params connection parameters
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, Map params, String charset) {
        return parseURL(url, params, charset);
    }

    /**
     * Parse a JSON data structure from content at a given URL. Convenience variant when using Groovy named parameters for the connection params.
     *
     * @param params connection parameters
     * @param url URL containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(Map params, URL url, String charset) {
        return parseURL(url, params, charset);
    }

    private Object parseURL(URL url, Map params, String charset) {
            return parse(new StringReader( HTTP.getJSONWithParams ( url.toString (), null, params )));
    }
}
