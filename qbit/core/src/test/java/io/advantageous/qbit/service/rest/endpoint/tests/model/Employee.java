package io.advantageous.qbit.service.rest.endpoint.tests.model;


import java.util.ArrayList;
import java.util.List;

public class Employee {

    private final long id;
    private final String name;
    private List<PhoneNumber> phoneNumbers;


    public Employee(long id, String name, List<PhoneNumber> phoneNumbers) {
        this.id = id;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }


    public Employee(long id, String name) {
        this.id = id;
        this.name = name;
        this.phoneNumbers = new ArrayList<>();
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

    public void addPhoneNumber(PhoneNumber phoneNumber) {
        if (this.phoneNumbers == null) {
            phoneNumbers = new ArrayList<>();
        }
        phoneNumbers.add(phoneNumber);
    }


    public List<PhoneNumber> getPhoneNumbers() {
        return new ArrayList<>(phoneNumbers);
    }
}
