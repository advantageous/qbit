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
import io.advantageous.boon.primitive.Dbl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static io.advantageous.boon.primitive.Dbl.grow;

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
public class DoubleList extends AbstractList<Double> {


    /**
     * Values in this list.
     */
    private double[] values;
    /**
     * Index of last value added.
     */
    private int end;

    /**
     * Create a new list with this many items in it.
     * @param capacity capacity
     */
    public DoubleList(final int capacity) {
        this.values = new double[capacity];
    }


    /**
     * Create a new list with exactly 10 items in it.
     */
    public DoubleList() {
        this.values = new double[10];
    }


    /**
     * Create a new list with this many items in it.
     * @param values values
     *
     */
    public DoubleList(double values[]) {
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
    public static DoubleList toDoubleList(Collection<?> inputList, String propertyPath) {
        if (inputList.size() == 0) {
            return new DoubleList(0);
        }

        DoubleList outputList = new DoubleList(inputList.size());

        if (propertyPath.contains(".") || propertyPath.contains("[")) {

            String[] properties = StringScanner.splitByDelimiters(propertyPath, ".[]");

            for (Object o : inputList) {
                outputList.add(BeanUtils.getPropertyDouble(o, properties));
            }

        } else {

            Map<String, FieldAccess> fields = BeanUtils.getFieldsFromObject(inputList.iterator().next());
            FieldAccess fieldAccess = fields.get(propertyPath);
            for (Object o : inputList) {
                outputList.add(fieldAccess.getDouble(o));
            }
        }

        return outputList;
    }

    /**
     * Get the value at index
     *
     * @param index index
     * @return value
     */
    @Override
    public Double get(int index) {
        return values[index];
    }


    /**
     * Get the value at index
     *
     * @param index index
     * @return value
     */
    public double idx(int index) {
        return values[index];
    }


    /**
     * Get the value at index
     *
     * @param index index
     * @return value
     */
    public double atIndex(int index) {
        return values[index];
    }


    /**
     * Get the value at index but don't use a wrapper
     *
     * @param index index
     * @return value
     */
    public final double getFloat(int index) {
        return values[index];
    }

    /**
     * Add a new value to the list.
     *
     * @param integer new value
     * @return was able to add.
     */
    @Override
    public boolean add(Double integer) {
        if (end + 1 >= values.length) {
            values = grow(values);
        }
        values[end] = integer;
        end++;
        return true;
    }

    /**
     * Add a new value to the list but don't employ a wrapper.
     *
     * @param value new value
     * @return was able to add.
     */
    public boolean addFloat(double value) {
        if (end + 1 >= values.length) {
            values = grow(values);
        }
        values[end] = value;
        end++;
        return true;
    }

    /**
     * Add a new value to the list but don't employ a wrapper.
     *
     * @param integer new value
     * @return was able to add.
     */
    public DoubleList add(double integer) {
        if (end + 1 >= values.length) {
            values = grow(values);
        }
        values[end] = integer;
        end++;
        return this;
    }

    /**
     * Add a new array to the list.
     *
     * @param newValues values
     * @return was able to add.
     */
    public boolean addArray(double... newValues) {
        if (end + newValues.length >= this.values.length) {
            this.values = grow(this.values, (this.values.length + newValues.length) * 2);
        }

        System.arraycopy(newValues, 0, this.values, end, newValues.length);
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
    public Double set(int index, Double element) {
        double oldValue = values[index];
        values[index] = element;
        return oldValue;
    }


    /**
     * Set a value in the list.
     *
     * @param index   index
     * @param element new value
     * @return old value at this index
     */
    public double idx(int index, double element) {
        double oldValue = values[index];
        values[index] = element;
        return oldValue;
    }


    /**
     * Set a value in the list.
     *
     * @param index   index
     * @param element new value
     * @return old value at this index
     */
    public double atIndex(int index, double element) {
        double oldValue = values[index];
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
    public double setFloat(int index, double element) {
        double oldValue = values[index];
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
    public double sum() {

        return Dbl.sum(values, end);
    }


    /**
     * Get a copy of the array up to the end element.
     *
     * @return array
     */
    public double[] toValueArray() {

        return java.util.Arrays.copyOfRange(values, 0, end);
    }


    /**
     * This would be a good opportunity to reintroduce dynamic invoke
     *
     * @param function function
     * @return result
     */
    public double reduceBy(Object function) {
        return Dbl.reduceBy(values, end, function);
    }


    /**
     * This would be a good opportunity to reintroduce dynamic invoke
     *
     * @param function function
     * @param name name
     * @return result
     */
    public double reduceBy(Object function, String name) {
        return Dbl.reduceBy(values, end, function, name);
    }


    /**
     * @param reduceBy reduceBy function
     * @return the reduction
     */
    public double reduceBy(Dbl.ReduceBy reduceBy) {
        return Dbl.reduceBy(values, end, reduceBy);
    }

    /**
     * Mean
     *
     * @return mean
     */
    public double mean() {
        return Dbl.mean(values, end);
    }


    /**
     * standardDeviation
     *
     * @return standardDeviation
     */
    public double standardDeviation() {
        return Dbl.standardDeviation(values, end);
    }


    /**
     * variance
     *
     * @return variance
     */
    public double variance() {
        return Dbl.variance(values, end);
    }


    /**
     * max
     *
     * @return max
     */
    public double max() {
        return Dbl.max(values, end);
    }


    /**
     * min
     *
     * @return min
     */
    public double min() {
        return Dbl.min(values, end);
    }


    /**
     * median
     *
     * @return median
     */
    public double median() {
        return Dbl.median(values, end);
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

        DoubleList values = (DoubleList) o;

        if (end != values.end) return false;
        if (!Dbl.equals(0, end, this.values, values.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (values != null ? Dbl.hashCode(0, end, values) : 0);
        result = 31 * result + end;
        return result;
    }


    public void clear() {
        this.values = new double[10];
        this.end = 0;
    }

}

