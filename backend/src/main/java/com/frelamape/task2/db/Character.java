package com.frelamape.task2.db;

import org.bson.Document;

public class Character {
    private String name;
    private Person actor;

    public Character(String name, Person actor) {
        this.name = name;
        this.actor = actor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getActor() {
        return actor;
    }

    public void setActor(Person actor) {
        this.actor = actor;
    }

    public static class Adapter {
        public static Character fromDBObject (Document d){
            if (d == null)
                return null;

            Person actor = new Person(
                    d.getString("actor_name")
            );
            actor.setId(d.getString("actor_id"));

            return new Character(
                d.getString("name"),
                actor
            );
        }
    }
}
