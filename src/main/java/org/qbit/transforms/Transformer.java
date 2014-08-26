package org.qbit.transforms;

/**
 * Transforms a queue items to other items.
 * @author Richard Hightower
 */
public interface Transformer<INPUT, OUTPUT> {

    OUTPUT transform(INPUT input);
}
