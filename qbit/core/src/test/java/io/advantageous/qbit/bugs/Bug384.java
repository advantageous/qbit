package io.advantageous.qbit.bugs;


import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.ServiceQueue;
import org.junit.Test;


//https://github.com/advantageous/qbit/issues/384
public class Bug384 {

    @Test //(expected = IllegalStateException.class)
    public void noOverloading() {

        ServiceQueue serviceQueue = ServiceBuilder.serviceBuilder().setServiceObject(new MyService()).build();

    }

    @Test //(expected = IllegalStateException.class)
    public void noOverloadingBundle() {

        ServiceBundleBuilder.serviceBundleBuilder().build().addService(new MyService()).start();
    }


    public interface IMyService {
        void add(Person person);

        void add(Event person);
    }

    public static class Event {
        final String id;

        public Event(String id) {
            this.id = id;
        }
    }

    public static class Person {
        final String id;

        public Person(String id) {
            this.id = id;
        }
    }

    public static class MyService {

        public void addPerson(Person person) {
            System.out.println(person.id);

        }

        public void add(Person person) {

            System.out.println(person.id);

        }

        public void add(Event person) {

            System.out.println(person.id);
        }
    }
}
