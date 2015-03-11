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

package io.advantageous.boon.collections;

import io.advantageous.boon.StringScanner;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.primitive.Int;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static io.advantageous.boon.primitive.Int.grow;

/**
 * Holds primitive values in a list like object for ints.
 *
 * <p>
 * Has sum, mean, median, standardDeviation, reduceBy,
 * variance.
 * </p>
 *
 * @author Rick Hightower
 */
public class IntList extends AbstractList<Integer> {


    /**
     * Values in this list.
     */
    private int[] values;
    /**
     * Index of last value added.
     */
    private int end;

    /**
     * Create a new list with this many items in it.
     * @param capacity capacity
     */
    public IntList(final int capacity) {
        this.values = new int[capacity];
    }


    /**
     * Create a new list with exactly 10 items in it.
     */
    public IntList() {
        this.values = new int[10];
    }


    /**
     * Create a new list with this many items in it.
     * @param values values
     */
    public IntList(int values[]) {
        this.values = values;
        this.end = values.length;
    }

    /**
     * Creates a primitive list based on an input list and a property path
     *
     * @param inputList    input list
     * @param propertyPath property path
     * @return primitive list
     */
    public static IntList toIntList(Collection<?> inputList, String propertyPath) {
        if (inputList.size() == 0) {
            return new IntList(0);
        }

        IntList outputList = new IntList(inputList.size());

        if (propertyPath.contains(".") || propertyPath.contains("[")) {

            String[] properties = StringScanner.splitByDelimiters(propertyPath, ".[]");

            for (Object o : inputList) {
                outputList.add(BeanUtils.getPropertyInt(o, properties));
            }

        } else {

            Map<String, FieldAccess> fields = BeanUtils.getFieldsFromObject(inputList.iterator().next());
            FieldAccess fieldAccess = fields.get(propertyPath);
            for (Object o : inputList) {
                outputList.add(fieldAccess.getInt(o));
            }
        }

        return outputList;
    }

    public void clear() {
        this.values = new int[10];
        this.end = 0;
    }

    /**
     * Get the value at index
     *
     * @param index index
     * @return value
     */
    @Override
    public Integer get(int index) {
        return values[index];
    }


    /**
     * Get the value at index but don't use a wrapper
     *
     * @param index index
     * @return value
     */
    public final int getInt(int index) {
        return values[index];
    }

    /**
     * Add a new value to the list.
     *
     * @param integer new value
     * @return was able to add.
     */
    @Override
    public boolean add(Integer integer) {
        if (end + 1 >= values.length) {
            values = Int.grow(values);
        }
        values[end] = integer;
        end++;
        return true;
    }

    /**
     * Add a new value to the list but don't employ a wrapper.
     *
     * @param integer new value
     * @return was able to add.
     */
    public boolean addInt(int integer) {
        if (end + 1 >= values.length) {
            values = Int.grow(values);
        }
        values[end] = integer;
        end++;
        return true;
    }

    /**
     * Add a new value to the list but don't employ a wrapper.
     *
     * @param integer new value
     * @return was able to add.
     */
    public IntList add(int integer) {
        if (end + 1 >= values.length) {
            values = Int.grow(values);
        }
        values[end] = integer;
        end++;
        return this;
    }

    /**
     * Add a new array to the list.
     *
     *
     * @param newValues new values
     * @return was able to add.
     */
    public boolean addArray(int... newValues) {
        if (end + newValues.length >= values.length) {
            values = Int.grow(values, (values.length + newValues.length) * 2);
        }

        System.arraycopy(newValues, 0, values, end, newValues.length);
        end += newValues.length;
        return true;
    }

    /**
     * Set a value in the list.
     *
     * @param index   index
     * @param element new value
     * @return old value at this index
     */
    @Override
    public Integer set(int index, Integer element) {
        int oldValue = values[index];
        values[index] = element;
        return oldValue;
    }


    /**
     * Set in a new value no wrapper
     *
     * @param index   index
     * @param element new value
     * @return old value at this index
     */
    public int setInt(int index, int element) {
        int oldValue = values[index];
        values[index] = element;
        return oldValue;
    }

    /**
     * Return the current size.
     *
     * @return size
     */
    @Override
    public int size() {
        return end;
    }


    /**
     * Sums the values with bounds checking.
     * @return sum
     */
    public int sum() {

        return Int.sum(values, end);
    }


    /**
     * Get a copy of the array up to the end element.
     *
     * @return array
     */
    public int[] toValueArray() {

        return java.util.Arrays.copyOfRange(values, 0, end);
    }


    /**
     * This would be a good opportunity to reintroduce dynamic invoke
     *
     * @param function function
     * @return array
     */
    public long reduceBy(Object function) {
        return Int.reduceBy(values, end, function);
    }


    /**
     * This would be a good opportunity to reintroduce dynamic invoke
     *
     * @param function function
     * @param name name
     * @return result
     */
    public long reduceBy(Object function, String name) {
        return Int.reduceBy(values, end, function, name);
    }


    /**
     * @param reduceBy reduceBy function
     * @return the reduction
     */
    public long reduceBy(Int.ReduceBy reduceBy) {
        return Int.reduceBy(values, end, reduceBy);
    }

    /**
     * Mean
     *
     * @return mean
     */
    public int mean() {
        return Int.mean(values, end);
    }


    /**
     * standardDeviation
     *
     * @return standardDeviation
     */
    public int standardDeviation() {
        return Int.standardDeviation(values, end);
    }


    /**
     * variance
     *
     * @return variance
     */
    public int variance() {
        return Int.variance(values, end);
    }


    /**
     * max
     *
     * @return max
     */
    public int max() {
        return Int.max(values, end);
    }


    /**
     * min
     *
     * @return min
     */
    public int min() {
        return Int.min(values, end);
    }


    /**
     * median
     *
     * @return median
     */
    public int median() {
        return Int.median(values, end);
    }


    /**
     * sort
     *
     */
    public void sort() {
        Arrays.sort(values, 0, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntList integers = (IntList) o;

        if (end != integers.end) return false;
        if (!Int.equals(0, end, values, integers.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (values != null ? Int.hashCode(0, end, values) : 0);
        result = 31 * result + end;
        return result;
    }
}
