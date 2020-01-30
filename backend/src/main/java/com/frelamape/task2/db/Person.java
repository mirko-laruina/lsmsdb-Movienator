package com.frelamape.task2.db;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Person implements Statistics.Aggregator {
    private String id;
    private String name;

    public Person(){}

    public Person(String name) {
        this.name = name;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void fromDBObject(Document d) {
        id = d.getString("_id");
        name = d.getString("name");
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Adapter {
        public static Person fromDBObject (Document d){
            if (d == null)
                return null;

            Person person = new Person();
            person.fromDBObject(d);
            return person;
        }

        public static List<Person> fromDBObjectIterable(Iterable<Document> documents){
            List<Person> people = new ArrayList<>();
            for(Document d:documents){
                people.add(fromDBObject(d));
            }
            return people;
        }
    }

}
