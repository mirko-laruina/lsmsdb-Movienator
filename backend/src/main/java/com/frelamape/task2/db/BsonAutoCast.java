package com.frelamape.task2.db;

import org.bson.Document;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BsonAutoCast {
    public static Integer asInteger(Document d, String key){
        try{
            return d.getInteger(key);
        } catch (ClassCastException e){
            try {
                return d.getDouble(key).intValue();
            } catch (ClassCastException e2){
                try{
                    return Integer.parseInt(d.getString(key));
                } catch (NumberFormatException e3){
                    return null;
                }
            }
        }
    }

    public static Double asDouble(Document d, String key){
        try{
            return d.getDouble(key);
        } catch (ClassCastException e){
            try{
                return d.getInteger(key).doubleValue();
            } catch (ClassCastException e2) {
                try {
                    return Double.parseDouble(d.getString(key));
                } catch (NumberFormatException e3) {
                    return null;
                }
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
