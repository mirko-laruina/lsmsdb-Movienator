package com.frelamape.task2.db;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public class QuerySubset<T> {
    private List<T> list;
    private long totalCount;

    public QuerySubset(List<T> list, long totalCount) {
        this.list = list;
        this.totalCount = totalCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
