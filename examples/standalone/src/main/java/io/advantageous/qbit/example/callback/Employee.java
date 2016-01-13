package io.advantageous.qbit.example.callback;

public class Employee {


    private String name;

    public Employee(String name) {
        this.name = name;
    }


    public Employee() {
    }



    public String getName() {
        return name;
    }

    public Employee setName(String name) {
        this.name = name;
        return this;
    }


}
