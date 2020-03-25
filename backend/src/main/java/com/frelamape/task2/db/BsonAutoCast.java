package com.frelamape.task2.db;

import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BsonAutoCast {
    public static Integer asInteger(Document d, String key){
        try{
            return d.getInteger(key);
        } catch (ClassCastException e){
            try{
                return Integer.parseInt(d.getString(key));
            } catch (NumberFormatException e2){
                return null;
            }
        }
    }

    public static Date asDate(Document d, String key){
        try{
            return d.getDate(key);
        } catch (ClassCastException e){
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(d.getString(key));
            } catch (ParseException e2){
                return null;
            }
        }
    }
}
