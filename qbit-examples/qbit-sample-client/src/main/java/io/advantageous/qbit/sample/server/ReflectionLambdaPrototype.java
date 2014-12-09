package io.advantageous.qbit.sample.server;

import io.advantageous.qbit.sample.server.model.TodoItem;
import io.advantageous.qbit.sample.server.service.TodoRepository;
import io.advantageous.qbit.sample.server.service.TodoService;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.IntBinaryOperator;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 12/8/14.
 */
public class ReflectionLambdaPrototype {


    public static void main(String... args) throws Throwable {

        TodoService todoService = new TodoService();


        Method reflected=TodoService.class
                .getDeclaredMethod("add", TodoItem.class);
        reflected.setAccessible(true);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandleForAddMethod=lookup.unreflect(reflected).bindTo(todoService);

        long start = System.nanoTime();

        for (int index = 0; index< 100_000_000; index++) {
            methodHandleForAddMethod.invoke(new TodoItem("Hello", "Hello", new Date()));
        }


        long duration = System.nanoTime() - start;
        puts(duration/1_000_000);



        start = System.nanoTime();

        for (int index = 0; index< 10_000_000; index++) {
            todoService.add(new TodoItem("Hello", "Hello", new Date()));
        }


        duration = System.nanoTime() - start;
        puts(duration/1_000_000);



        start = System.nanoTime();

        for (int index = 0; index< 10_000_000; index++) {
            reflected.invoke(todoService, new TodoItem("Hello", "Hello", new Date()));
        }


        duration = System.nanoTime() - start;
        puts(duration/1_000_000);

        //
//
//        final MethodType addMethodType = MethodType.methodType(void.class, TodoItem.class);
//
//
//        final MethodType creatorMethodType = MethodType.methodType(TodoRepository.class);
//
//
//        TodoRepository lambda=(TodoRepository) LambdaMetafactory.metafactory(
//                lookup, "add", creatorMethodType,
//                methodHandleForAddMethod.type(),
//                methodHandleForAddMethod, addMethodType).getTarget().invokeExact();
//
//        lambda.add(new TodoItem("description", "aa", new Date()));


    }
}
