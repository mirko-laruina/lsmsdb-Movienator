package com.frelamape.task2.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class Neo4jAdapter {
    // TODO

    private static final Logger logger = LoggerFactory.getLogger(Neo4jAdapter.class);

    @Value("${com.frelamape.task2.db.Neo4jAdapter.connectionURI}")
    private String connectionURI;

    @Autowired
    private Neo4jTaskExecutor executor;

    @Autowired
    private ApplicationArguments args;

    @PostConstruct
    public void init() {
        if (args.getSourceArgs().length >= 3) {
            connectionURI = args.getSourceArgs()[3];
        }

        logger.info("Connecting to Neo4J at " + connectionURI);

        // TODO
    }

    public QuerySubset<Movie> getMovieSuggestions(User u, int n) {
        // TODO
        return null;
    }

    public User.Relationship getUserRelationship(User u1, User u2) {
        // TODO
        return null;
    }

    public QuerySubset<User> getFollowers(User user, int n, int page) {
        // TODO
        return null;
    }

    public QuerySubset<User> getFollowings(User user, int n, int page) {
        // TODO
        return null;
    }

    public QuerySubset<User> getUserSuggestions(User user, int n) {
        // TODO
        return null;
    }

    public QuerySubset<RatingExtended> getFriendsRatings(User u, int n, int page) {
        // TODO
        return null;
    }

    public boolean insertUser(User u){
        // TODO
        return false;
    }

    public boolean insertRating(Rating r){
        // TODO
        // NB: this is an upsert!
        return false;
    }

    public boolean deleteRating(Rating r){
        // TODO
        return false;
    }

    public boolean follow(User a, User b){
        // TODO
        // a follows b
        return false;
    }

    public boolean unfollow(User a, User b){
        // TODO
        // a unfollows b
        return false;
    }
}
