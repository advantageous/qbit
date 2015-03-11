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

/** */
public interface CharacterSource {

    /** Skip white space. */
    void skipWhiteSpace();

    /** returns the next character moving the file pointer or index
     * to the next location.
     * @return next char
     */
    int nextChar();
    /** returns the current character without changing the IO pointer or index.
     * @return currentChar
     * */
    int currentChar();
    /** Checks to see if there is a next character.
     * @return has a char
     */
    boolean hasChar();
    /** Useful for finding constants in a string like true, false, etc.
     * @return consume if match
     **/
    boolean consumeIfMatch( char[] match );
    /** This is mostly for debugging and testing.
     * @return location
     */
    int location();

    /** Combines the operations of nextChar and hasChar.
     *  Characters is -1 if not found which signifies end of file.
     *  This might be preferable to avoid two method calls.
     * @return safe next char
     **/
    int safeNextChar();

    /**
     * Used to find strings and their ilk
     * Finds the next non-escaped char
     * @param ch character to find
     * @param esc escape character to avoid next char if escaped
     * @return list of chars until this is found.
     */
    char[] findNextChar( int ch, int esc );

    boolean hadEscape();

    /** Reads a number from the character source.
     * @return readNumber
     **/
    char[] readNumber(  );

    String errorDetails( String message  );

}
