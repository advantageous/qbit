package io.advantageous.qbit.metrics.support;

import io.advantageous.boon.primitive.Int;

import java.util.AbstractList;
import java.util.Arrays;

import static io.advantageous.boon.primitive.Int.bigSum;
import static io.advantageous.boon.primitive.Int.varianceDouble;

/**
 * Holds primitive values in a list like object for ints.
 * <p>
 * <p>
 * Has sum, mean, median, standardDeviation, reduceBy,
 * variance.
 * </p>
 *
 * @author Rick Hightower
 */
public class StatList extends AbstractList<Integer> {


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
     *
     * @param capacity capacity
     */
    public StatList(final int capacity) {
        this.values = new int[capacity];
    }


    /**
     * Create a new list with exactly 10 items in it.
     */
    public StatList() {
        this.values = new int[10];
    }


    /**
     * Create a new list with this many items in it.
     *
     * @param values values
     */
    public StatList(int values[]) {
        this.values = values;
        this.end = values.length;
    }

    private static double meanDouble(int[] values, final int start, final int length) {
        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("UnnecessaryLocalVariable") double mean = ((double) bigSum(values, start, length)) / ((double) length);
        return mean;
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
    public StatList add(int integer) {
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
     *
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
     * @param name     name
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
    public float mean() {
        return (float) meanDouble(values, 0, end);
    }

    /**
     * standardDeviation
     *
     * @return standardDeviation
     */
    public float standardDeviation() {
        double variance = varianceDouble(values, 0, end);
        return (float) Math.sqrt(variance);
    }


    /**
     * variance
     *
     * @return variance
     */
    public float variance() {
        return (float) varianceDouble(values, 0, end);
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
     */
    public void sort() {
        Arrays.sort(values, 0, end);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatList integers = (StatList) o;

        //noinspection SimplifiableIfStatement
        if (end != integers.end) return false;
        return Int.equals(0, end, values, integers.values);

    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (values != null ? Int.hashCode(0, end, values) : 0);
        result = 31 * result + end;
        return result;
    }
}

