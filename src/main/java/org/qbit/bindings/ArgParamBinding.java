package org.qbit.bindings;

/**
 * Created by Richard on 7/22/14.
 */
public class ArgParamBinding {
    int position;
    private String name;


    public static ArgParamBinding param(String name, int position) {
        return new ArgParamBinding(position, name);
    }

    private ArgParamBinding(int position, String name) {
        this.position = position;
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }
}
