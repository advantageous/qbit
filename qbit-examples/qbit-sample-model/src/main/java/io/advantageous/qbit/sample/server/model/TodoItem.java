package io.advantageous.qbit.sample.server.model;

import java.util.Date;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoItem {


    private final String description;
    private final String name;
    private final Date due;

    public TodoItem(final String description, final String name, final Date due) {
        this.description = description;
        this.name = name;
        this.due = due;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Date getDue() {
        return due;
    }
}
