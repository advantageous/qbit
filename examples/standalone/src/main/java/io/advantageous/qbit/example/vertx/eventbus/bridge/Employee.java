package io.advantageous.qbit.example.vertx.eventbus.bridge;

public class Employee {


    private final String id;
    private final String firstName;
    private final String lastName;
    private final int birthYear;
    private final long socialSecurityNumber;

    public Employee(String id, String firstName, String lastName, int birthYear, long socialSecurityNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
        this.socialSecurityNumber = socialSecurityNumber;
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public long getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthYear=" + birthYear +
                ", socialSecurityNumber=" + socialSecurityNumber +
                '}';
    }
}
