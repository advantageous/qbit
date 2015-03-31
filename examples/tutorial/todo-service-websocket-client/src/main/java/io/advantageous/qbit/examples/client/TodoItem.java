package io.advantageous.qbit.examples.client;

import java.util.Date;


public class TodoItem {


    private String description;
    private String name;
    private Date due;

    public TodoItem(final String description, final String name, final Date due) {
        this.description = description;
        this.name = name;
        this.due = due;
    }

    public TodoItem(String description) {

        this.description = description;
        name = description;
        due = new Date();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }
}