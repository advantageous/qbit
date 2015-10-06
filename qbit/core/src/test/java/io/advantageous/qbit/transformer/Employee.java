package io.advantageous.qbit.transformer;

public class Employee {

    private String firstName;
    private String lastName;

    public Employee(@SuppressWarnings("SameParameterValue") String firstName, @SuppressWarnings("SameParameterValue") String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Employee() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
