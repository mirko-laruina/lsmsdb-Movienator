package com.frelamape.task2.db;

import org.bson.Document;

import java.util.Calendar;
import java.util.Date;

public class Session {
    public static final int EXPIRAL_DAYS = 7;
    private String id;
    private Date expiry;

    public Session(String id) {
        this.id = id;
        Calendar expiry = Calendar.getInstance();
        expiry.add(EXPIRAL_DAYS, Calendar.DAY_OF_YEAR);
        this.expiry = expiry.getTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public static class Adapter {
        public static Session fromDBObject (Document d){
            if (d == null)
                return null;

            Session session = new Session(
                    d.getString("_id")
            );
            session.setExpiry(d.getDate("expiry"));
            return session;
        }

        public static Document toDBObject (Session s){
            return new Document("_id", s.getId())
                    .append("expiry", s.getExpiry());
        }
    }
}
