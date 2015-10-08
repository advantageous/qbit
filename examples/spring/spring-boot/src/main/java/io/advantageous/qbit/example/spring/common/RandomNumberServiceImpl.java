package io.advantageous.qbit.example.spring.common;

import java.util.Random;

public class RandomNumberServiceImpl implements RandomNumberService {

    @Override
    public int getRandom(int min, int max) {
        final int result = new Random().nextInt(max - min + 1) + min;
        if (String.valueOf(result).contains("7")) throw new RuntimeException("Oh no!  It's a seven!");
        return result;
    }
}
