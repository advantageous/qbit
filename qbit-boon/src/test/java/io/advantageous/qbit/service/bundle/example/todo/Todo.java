package io.advantageous.qbit.service.bundle.example.todo;

import java.util.Date;

/**
 * Created by rhightower on 10/24/14.
 */
public class Todo {

    private final String name;
    private final String description;
    private final Date dueDate;

    public Todo(String name, String description, Date dueDate) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todoItem = (Todo) o;

        if (description != null ? !description.equals(todoItem.description) : todoItem.description != null)
            return false;
        if (dueDate != null ? !dueDate.equals(todoItem.dueDate) : todoItem.dueDate != null) return false;
        if (name != null ? !name.equals(todoItem.name) : todoItem.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}
