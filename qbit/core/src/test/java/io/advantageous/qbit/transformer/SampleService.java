package io.advantageous.qbit.transformer;


import io.advantageous.qbit.annotation.*;
import io.advantageous.qbit.reactive.Callback;

import static io.advantageous.boon.core.Str.sputs;


@RequestMapping("/sample/service")
public class SampleService {


    @RequestMapping("/simple1/")
    public String simple1() {
        return "simple1";
    }


    @RequestMapping("/call1/foo/{arg4}/{2}")
    public String method1(@RequestParam("arg1") final String arg1,
                          @HeaderParam("arg2") final int arg2,
                          @PathVariable final float arg3,
                          @PathVariable("arg4") final double arg4) {


        return sputs(arg1, arg2, arg3, arg4);
    }


    //"/call2/{2}/{arg4}")
    public String method2(final String arg1,
                          final int arg2,
                          final float arg3,
                          final double arg4) {

        return sputs(arg1, arg2, arg3, arg4);
    }


    @RequestMapping("/simple2/path/")
    public String simple2(@RequestParam("arg1") final String arg1) {
        return "simple2";
    }


    @RequestMapping(value = "/method3/", method = RequestMethod.POST)
    public String method3(@RequestParam("arg1") final String arg1,
                          @HeaderParam("arg2") final int arg2,
                          Employee employee) {


        return sputs(arg1, arg2, employee);
    }

    @RequestMapping("/simpleBadConfig1/{0}/")
    public void simpleBadConfig(Callback<String> callback, @PathVariable(defaultValue = "missing") final String arg1) {
        callback.accept("simple3");
    }
}
