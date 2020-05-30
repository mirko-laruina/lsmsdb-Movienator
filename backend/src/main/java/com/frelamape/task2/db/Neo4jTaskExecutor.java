package com.frelamape.task2.db;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Future;

@Repository
public class Neo4jTaskExecutor {

    @Autowired()
    private Neo4jAdapter dba;

    @Async("taskExecutor")
    public void updateInternalRating(){
    }

    @Async("taskExecutor")
    public Future<User.Relationship> getUserRelationship(User u1, User u2) {
        return new AsyncResult<>(dba.getUserRelationship(u1, u2));
    }

    @Async("taskExecutor")
    public Future<QuerySubset<User>> getFollowers(User user, User relationshipPoV, int n, int page) {
        return new AsyncResult<>(dba.getFollowers(user, relationshipPoV, n, page));
    }

    @Async("taskExecutor")
    public Future<QuerySubset<User>>getFollowings(User user, User relationshipPoV, int n, int page) {
        return new AsyncResult<>(dba.getFollowings(user, relationshipPoV, n, page));
    }

    @Async("taskExecutor")
    public Future<QuerySubset<User>> getUserSuggestions(User user, int n) {
        return new AsyncResult<>(dba.getUserSuggestions(user, n));
    }

    @Async("taskExecutor")
    public void insertUser(User u){
        dba.insertUser(u);
    }

    @Async("taskExecutor")
    public void insertRating(Rating r){
        dba.insertRating(r);
    }

    @Async("taskExecutor")
    public void deleteRating(Rating r){
        dba.deleteRating(r);
    }

}