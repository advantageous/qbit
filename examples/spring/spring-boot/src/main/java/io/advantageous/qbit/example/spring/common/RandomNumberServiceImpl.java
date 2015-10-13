package io.advantageous.qbit.example.spring.common;

import java.util.Random;

/**
 * Simple implementation of the {@link RandomNumberService RandomNumberService}.  Returns a number between the min and
 * max parameters and blows up if it contains a seven do demonstrate error handling.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
public class RandomNumberServiceImpl implements RandomNumberService {

    @Override
    public int getRandom(int min, int max) {
        final int result = new Random().nextInt(max - min + 1) + min;
        if (String.valueOf(result).contains("7")) throw new RuntimeException("Oh no!  It's a seven!");
        return result;
    }
}
