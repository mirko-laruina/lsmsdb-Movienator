package com.frelamape.task2.db;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public class Neo4jTaskExecutor {

    @Autowired()
    private Neo4jAdapter dba;

    @Async("taskExecutor")
    public void updateInternalRating(){
    }
}