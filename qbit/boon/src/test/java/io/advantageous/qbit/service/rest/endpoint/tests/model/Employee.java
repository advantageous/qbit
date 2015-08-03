package io.advantageous.qbit.service.rest.endpoint.tests.model;


public class Employee {

    private final long id;
    private final String name;

    public Employee(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
