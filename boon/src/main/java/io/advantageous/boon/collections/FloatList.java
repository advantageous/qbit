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
import io.advantageous.boon.primitive.Flt;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static io.advantageous.boon.primitive.Flt.grow;

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
public class FloatList extends AbstractList<Float> {


    /**
     * Values in this list.
     */
    private float[] values;
    /**
     * Index of last value added.
     */
    private int end;

    /**
     * Create a new list with this many items in it.
     * @param capacity capacity
     */
    public FloatList(final int capacity) {
        this.values = new float[capacity];
    }


    /**
     * Create a new list with exactly 10 items in it.
     */
    public FloatList() {
        this.values = new float[10];
    }


    /**
     * Create a new list with this many items in it.
     * @param values  values
     */
    public FloatList(float values[]) {
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
    public static FloatList toFloatList(Collection<?> inputList, String propertyPath) {
        if (inputList.size() == 0) {
            return new FloatList(0);
        }

        FloatList outputList = new FloatList(inputList.size());

        if (propertyPath.contains(".") || propertyPath.contains("[")) {

            String[] properties = StringScanner.splitByDelimiters(propertyPath, ".[]");

            for (Object o : inputList) {
                outputList.add(BeanUtils.getPropertyFloat(o, properties));
            }

        } else {

            Map<String, FieldAccess> fields = BeanUtils.getFieldsFromObject(inputList.iterator().next());
            FieldAccess fieldAccess = fields.get(propertyPath);
            for (Object o : inputList) {
                outputList.add(fieldAccess.getFloat(o));
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
    public Float get(int index) {
        return values[index];
    }


    /**
     * Get the value at index
     *
     * @param index index
     * @return value
     */
    public float idx(int index) {
        return values[index];
    }


    /**
     * Get the value at index
     *
     * @param index index
     * @return value
     */
    public float atIndex(int index) {
        return values[index];
    }


    /**
     * Get the value at index but don't use a wrapper
     *
     * @param index index
     * @return value
     */
    public final float getFloat(int index) {
        return values[index];
    }

    /**
     * Add a new value to the list.
     *
     * @param integer new value
     * @return was able to add.
     */
    @Override
    public boolean add(Float integer) {
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
    public boolean addFloat(float value) {
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
    public FloatList add(float integer) {
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
     * @param values values
     * @return was able to add.
     */
    public boolean addArray(float... values) {
        if (end + values.length >= this.values.length) {
            this.values = grow(this.values, (this.values.length + values.length) * 2);
        }

        System.arraycopy(values, 0, this.values, end, values.length);
        end += values.length;
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
    public Float set(int index, Float element) {
        float oldValue = values[index];
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
    public float idx(int index, float element) {
        float oldValue = values[index];
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
    public float atIndex(int index, float element) {
        float oldValue = values[index];
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
    public float setFloat(int index, float element) {
        float oldValue = values[index];
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
     */
    public float sum() {

        return Flt.sum(values, end);
    }


    /**
     * Get a copy of the array up to the end element.
     *
     * @return float array
     */
    public float[] toValueArray() {

        return java.util.Arrays.copyOfRange(values, 0, end);
    }


    /**
     * This would be a good opportunity to reintroduce dynamic invoke
     *
     * @param function function
     * @return  result
     */
    public double reduceBy(Object function) {
        return Flt.reduceBy(values, end, function);
    }


    /**
     * This would be a good opportunity to reintroduce dynamic invoke
     *
     * @param function function
     * @param name name
     * @return result
     */
    public double reduceBy(Object function, String name) {
        return Flt.reduceBy(values, end, function, name);
    }


    /**
     * @param reduceBy reduceBy function
     * @return the reduction
     */
    public double reduceBy(Flt.ReduceBy reduceBy) {
        return Flt.reduceBy(values, end, reduceBy);
    }

    /**
     * Mean
     *
     * @return mean
     */
    public float mean() {
        return Flt.mean(values, end);
    }


    /**
     * standardDeviation
     *
     * @return standardDeviation
     */
    public float standardDeviation() {
        return Flt.standardDeviation(values, end);
    }


    /**
     * variance
     *
     * @return variance
     */
    public float variance() {
        return Flt.variance(values, end);
    }


    /**
     * max
     *
     * @return max
     */
    public float max() {
        return Flt.max(values, end);
    }


    /**
     * min
     *
     * @return min
     */
    public float min() {
        return Flt.min(values, end);
    }


    /**
     * median
     *
     * @return median
     */
    public float median() {
        return Flt.median(values, end);
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

        FloatList values = (FloatList) o;

        if (end != values.end) return false;
        if (!Flt.equals(0, end, this.values, values.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (values != null ? Flt.hashCode(0, end, values) : 0);
        result = 31 * result + end;
        return result;
    }


    public void clear() {
        this.values = new float[10];
        this.end = 0;
    }

}
