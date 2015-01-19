package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

public class ResponseImplTest {


    Response<Object> response;

    Response<Object> response2;

    boolean ok;

    @Before
    public void setUp() throws Exception {
        response = ResponseImpl.response(10, 11, "/uri", "/returnAddress", "love", null, true);
        response2 = ResponseImpl.response(10, 11, "/uri", "/returnAddress", "love", null, true);

    }


    @Test
    public void test() throws Exception {

        ok = response.equals(response2) || die();

        ok = response.hashCode() == response2.hashCode() || die();

        puts(response);
    }

}