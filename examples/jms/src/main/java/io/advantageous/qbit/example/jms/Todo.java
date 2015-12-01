package io.advantageous.qbit.example.jms;

public class Todo {


    private String name;
    private boolean done;

    public Todo(String name, boolean done) {
        this.name = name;
        this.done = done;
    }

    public Todo() {
    }

    public String getName() {
        return name;
    }

    public Todo setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isDone() {
        return done;
    }

    public Todo setDone(boolean done) {
        this.done = done;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (done != todo.done) return false;
        return !(name != null ? !name.equals(todo.name) : todo.name != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (done ? 1 : 0);
        return result;
    }
}
