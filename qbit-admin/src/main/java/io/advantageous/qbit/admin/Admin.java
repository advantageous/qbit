package io.advantageous.qbit.admin;

import io.advantageous.qbit.annotation.RequestMapping;

/**
 * Created by rhightower on 2/9/15.
 */
@RequestMapping("/admin")
public class Admin {

    @RequestMapping("/ok")
    public String ok() {
        return "ok";
    }
}
