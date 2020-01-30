package com.frelamape.task2;

import com.frelamape.task2.db.DatabaseAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Main {
    private static DatabaseAdapter dba;

    public static void main(String[] args) {
        String connectionURI = null;
        String dbName = null;

        if (args.length == 2){
            connectionURI = args[0];
            dbName = args[1];

            dba = new DatabaseAdapter(connectionURI, dbName);
        } else{
            System.out.println("This executable takes 2 parameters: the connection URI and the database name.");
            return;
        }

        SpringApplication.run(Main.class, args);
    }

}