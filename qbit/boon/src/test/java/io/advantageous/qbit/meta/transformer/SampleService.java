package io.advantageous.qbit.meta;


import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.annotation.HeaderParam;

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


}
