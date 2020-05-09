package com.frelamape.task2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AsyncApplication {
    public static void main(String[] args) {
        System.out.println(args.length);
        for (String arg:args){
            System.out.println(arg);
        }

        if (args.length != 2){
            System.out.println("This executable takes 2 parameters: the connection URI and the database name.");
            return;
        }

       SpringApplication.run(AsyncApplication.class, args);
    }
}
