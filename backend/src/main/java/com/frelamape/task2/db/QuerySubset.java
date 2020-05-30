package com.frelamape.task2.db;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public class QuerySubset<T> {
    private List<T> list;
    private boolean lastPage = false;

    public QuerySubset(List<T> list, boolean lastPage) {
        this.list = list;
        this.setLastPage(lastPage);
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    
}
