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


/**
 * This is more for documentation then actually using.
 * Nothing will likely implement this.
 * It is more conceptional.
 * <p/>
 * There are examples in the documentation.
 * <p/>
 * The following is a basis for many of the examples.
 * <p/>
 * <blockquote>
 * <pre>
 *
 *
 * //Works with lists, arrays, sets, maps, sorted maps, etc.
 * List<String> fruitList;
 * String [] fruitArray;
 * Set<String> veggiesSet;
 * char [] letters;
 * byte [] bytes;
 * NavigableMap <Integer, String> favoritesMap;
 * Map<String, Integer> map;
 *
 * // These helper methods are used to create common Java types.
 * // Sets and lists have concurrent and non concurrent variants
 * // Set also has sorted and non sorted variants
 * // This makes safeList, listStream, set, sortedSet, safeSet, safeSortedSet
 * veggiesSet  =  set( "salad", "broccoli", "spinach");
 * fruitList   =  listStream( "apple", "oranges", "pineapple");
 * fruitArray  =  array( "apple", "oranges", "pineapple");
 * letters     =  array( 'a', 'b', 'c');
 * bytes       =  array( new byte[]{0x1, 0x2, 0x3, 0x4});
 *
 * //You addObject up name / value pairs as a pseudo literal for map
 * favoritesMap = sortedMap(
 * 2, "pineapple",
 * 1, "oranges",
 * 3, "apple"
 * );
 *
 *
 * // You addObject up name / value pairs as a pseudo literal for map
 * // map, sortedMap, safeMap (thread safe concurrent), and sortedSafeMap are
 * // supported.
 * map =    map (
 * "pineapple",  2,
 * "oranges",    1,
 * "apple",      3
 * );
 *
 *
 * </pre>
 * </blockquote>
 */
public interface UniversalOperations<ITEM, INDEX> {

    public enum Returns {
        VARIES
    }

    ;

    /**
     * Get the item at the index.
     * This works with Maps, TreeSets, Strings, Object arrays, primitive arrays,
     * etc.
     * <p/>
     * Implemented by:
     * <pre>
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * Maps
     * Sets
     * </pre>
     * <p/>
     * Works with maps and sets
     * <p/>
     * <pre>
     *     Map<String,Dog> dogMap; //map of dogs
     *
     *     Dog dog = new Dog("dog");
     *
     *     NavigableSet<String> set; //set of strings
     *
     *     List<String> listStream;
     *
     *     char[] letters;
     *
     *
     *
     * </pre>
     * Initialize our examples
     * <pre>
     *        dogMap = map("dog", dog);
     *        set = sortedSet("apple", "kiwi", "oranges", "pears", "pineapple");
     *        listStream = listStream("apple", "oranges", "pears");
     *        letters = array('a', 'b', 'c', 'd');
     *
     * </pre>
     * The methods map, listStream, sortedSet, arrays
     * are utility methods for creating Maps, lists sets, etc.
     * <br />
     * <p/>
     * Using
     * <pre>
     *
     *              //Get the dog at the index "dog" in the map
     *              assertEquals(
     *
     *                  dog, idx(dogMap, "dog")
     *
     *              );
     *
     *              //Get the string "oranges" at index "ora"
     *              assertEquals(
     *
     *                  "oranges",
     *                  idx(set, "ora")
     *
     *              );
     *
     *              //Get the string "oranges" at index "o"
     *              assertEquals(
     *
     *                  "oranges",
     *                  idx(set, "o")
     *
     *               );
     *
     *               //Get the string "oranges" at index 1 of the listStream.
     *               assertEquals(
     *                   "oranges",
     *                   idx(listStream, 1)
     *               );
     *
     *               // Get the string "pears" at index -1 (using Python style slice notation)
     *               // of the listStream.
     *               assertEquals(
     *                   "pears",
     *                   idx(listStream, -1)
     *                );
     *
     *               //oranges are two from the back
     *               assertEquals(
     *               "oranges",
     *               idx(listStream, -2));
     *
     *
     *               //apple are two from the back
     *               assertEquals(
     *               "apple",
     *               idx(listStream, -3));
     *
     *
     *
     * </pre>
     * <p/>
     * Based on the example at top {@link UniversalOperations}:
     * <blockquote>
     * <pre>
     *
     * //Using idx to access a value.
     *
     * assert idx( veggiesSet, "b").equals("broccoli");
     *
     * assert idx( fruitList, 1 ).equals("oranges");
     *
     * assert idx( fruitArray, 1 ).equals("oranges");
     *
     * assert idx( letters, 1 ) == 'b';
     *
     * assert idx( bytes, 1 )      == 0x2;
     *
     * assert idx( favoritesMap, 2 ).equals("pineapple");
     *
     * assert idx( map, "pineapple" )  == 2;
     *
     * </pre>
     * </blockquote>
     * <p/>
     * Negative index works with listStream like, array like things.
     * <p/>
     * <blockquote>
     * <pre>
     *
     *          //Negative indexes
     *
     *          assert idx( fruitList, -2 ).equals("oranges");
     *
     *          assert idx( fruitArray, -2 ).equals("oranges");
     *
     *          assert idx( letters, -2 ) == 'b';
     *
     *          assert idx( bytes, -3 )   == 0x2;
     * </pre>
     * </blockquote>
     *
     * @param index the index of the item
     * @return the item at the index
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets#idx(java.util.NavigableSet, Object)
     * @see io.advantageous.boon.primitive.Byt#idx(byte[], int)
     * @see io.advantageous.boon.primitive.Chr#idx(char[], int)
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry#idx(Object[], int)
     * @see Lists
     * @see Maps
     * @see Sets
     */
    ITEM idx( INDEX index );

    /**
     * Sets the value at an index.
     *
     * @param index index
     * @param item  item you are setting
     */
    void idx( INDEX index, ITEM item );

    /**
     * Gets the length
     * This works with Maps, TreeSets, Strings, Object arrays, primitive arrays,
     * etc.
     * <p/>
     * <p/>
     * <br />
     * Building from the example at top ({@link UniversalOperations}).
     * <blockquote>
     * <pre>
     *
     * // Getting the length
     * assert len( veggiesSet )        == 3;
     * assert len( fruitList )         == 3;
     * assert len( fruitArray )        == 3;
     * assert len( letters )           == 3;
     * assert len( bytes )             == 4;
     * assert len( favoritesMap )      == 3;
     * assert len( map )               == 3;
     *
     *
     *
     * </pre>
     * </blockquote>
     * <p/>
     * Implemented by:
     * <pre>
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * Maps
     * Sets
     * </pre>
     *
     * @return the length
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets
     */
    int len();


    /**
     * Adds something to a collection, map or array
     * <p/>
     * For maps you addObject an entry
     * <p/>
     * <p/>
     * Implemented by:
     * <pre>
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * Maps
     * Sets
     * </pre>
     *
     * @return for arrays, this will return the new array,
     *         for all other collection like things, it returns void.
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets
     */
    Returns add( ITEM item );


    /**
     * Copies something.
     * This does a shallow copy.
     * There will be a clone that does a deep invasive copy.
     * <p/>
     * <p/>
     * <p/>
     * Implemented by:
     * <pre>
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * Maps
     * Sets
     * </pre>
     *
     * @return the copied object
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets
     */
    UniversalOperations copy( UniversalOperations thing );


    /**
     * NOT IMPLEMENTED YET.
     * Clone does a deep recursive copy.
     * <p/>
     * <p/>
     * <p/>
     * Implemented by:
     * <pre>
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * Maps
     * Sets
     * </pre>
     *
     * @return the length
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets
     */
    UniversalOperations clone( UniversalOperations thing );


    /**
     * This does not work with Sets or Maps (use idx for those)
     * <p/>
     * Implemented by:
     * <pre>
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * </pre>
     *
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets
     * @see io.advantageous.boon.primitive.Byt#insert
     * @see io.advantageous.boon.primitive.Chr#insert
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry#insert
     * @see Lists#insert
     */
    void insert( ITEM item );


    /**
     * Slice a string, array, TreeSet, or TreeMap.
     * Works like python slice notation
     * <p/>
     * <pre>
     *     >>> string [0:3]
     *     'foo'
     *     >>> string [-3:7]
     *     'bar'
     * </pre>
     * <p/>
     * What follows is derived from
     * <a href="http://stackoverflow.com/questions/509211/pythons-slice-notation">
     * Python's slice notation
     * </a>.
     * <p/>
     * <br /> <br />
     * It's pretty simple really (Python):
     * <pre>
     *
     *      a[ start : end ] # items start through end-1
     *      a[ start : ]     # items start through the rest of the array
     *      a[ : end ]       # items from the beginning through end-1
     *      a[ : ]           # a copy of the whole array
     *
     * </pre>
     * <p/>
     * Boon would be  (Java):
     * <pre>
     *
     *      slc( a, start, end ) // items start through end-1
     *      slc( a, start )      // items start through the rest of the array
     *      slcEnd( a, end )     // items from the beginning through end-1
     *      copy( a )            // a copy of the whole array
     * </pre>
     * <p/>
     * NOT IMPLEMENTED YET: <br />
     * <p/>
     * There is also the step value, which can be used with any of the above:
     * <p/>
     * <br />
     * Python
     * <pre>
     *
     *      a[ start : end : step] # start through not past end, by step
     *
     * </pre>
     * Boon
     * <pre>
     *
     *      slc(a, start , end, step) // start through not past end, by step
     *
     * </pre>
     * The key point to remember is that the :end value represents the
     * first value that is not in the selected slice. So, the difference
     * between end and start is the number of elements selected
     * (if step is 1, the default).
     * <br />
     * The other feature is that start or end may be a
     * negative number, which means it counts from the end of the
     * array instead of the beginning. So:
     * <p/>
     * <br/>
     * Python slice notation
     * <pre>
     *          a[ -1 ]    # last item in the array
     *          a[ -2: ]   # last two items in the array
     *          a[ :-2 ]   # everything except the last two items
     * </pre>
     * Boon slice notation
     * <pre>
     *          slc( a, -1)     # last item in the array
     *          slc( -2 )       # last two items in the array
     *          slcEnd( -2 )    # everything except the last two items
     * </pre>
     * <p/>
     * Python and boon are kind to the programmer
     * if there are fewer items than you ask for.
     * For example, if you ask for a[:-2] and a only contains one element,
     * you get an empty listStream instead of an error.
     * Sometimes you would prefer the error, so you have to
     * be aware that this may happen.
     * <p/>
     * Implemented by:
     * <pre>
     *
     * Byt
     * Chr
     * Int (in progress)
     * Lng (in progress)
     * Arry
     * io.advantageous.boon.core.Strings (planned)
     * io.advantageous.boon.core.StrBuf (planned)
     * Lists
     * </pre>
     *
     * @see io.advantageous.boon.primitive.Byt
     * @see io.advantageous.boon.primitive.Chr
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry
     * @see Lists
     * @see Maps
     * @see Sets
     * @see io.advantageous.boon.primitive.Byt#slc
     * @see io.advantageous.boon.primitive.Chr#slc
     * @see io.advantageous.boon.primitive.Int
     * @see io.advantageous.boon.primitive.Arry#slc
     * @see Lists#slc
     * @see Maps#slc
     * @see Sets#slc
     */
    void slc( INDEX start, INDEX end );

    /**
     * @param start index start
     * @see UniversalOperations#slc(Object, Object)
     */
    void slc( INDEX start );

    /**
     * @param end index end
     * @see UniversalOperations#slc(Object, Object)
     */
    void slcEnd( INDEX end );


}
