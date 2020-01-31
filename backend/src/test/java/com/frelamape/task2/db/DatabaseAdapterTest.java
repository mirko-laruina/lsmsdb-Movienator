package com.frelamape.task2.db;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class DatabaseAdapterTest {
    private static DatabaseAdapter dba;
    private static User user1;
    private static User user2;
    private static Movie movie1;
    private static Movie movie2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        dba = new DatabaseAdapter("mongodb://localhost:27017", "task2-test");

        user1 = dba.getUserProfile("mancio");
        Assert.assertNotNull(user1);

        user2 = dba.getUserProfile("topolino.hackerino");
        Assert.assertNotNull(user2);

        movie1 = dba.getMovieDetails("tt7286456");
        Assert.assertNotNull(movie1);

        movie2 = dba.getMovieDetails("tt10413566");
        Assert.assertNotNull(movie2);
    }

    @Test
    public void testRatings() {
        Rating rating1 = new Rating(user2.getId(), movie1.getId(), new Date(), 4.0);
        dba.insertRating(rating1);

        Rating rating2 = new Rating(user2.getId(), movie2.getId(), new Date(), 3.0);
        dba.insertRating(rating2);

        List<Rating> ratingList = dba.getUserRatings(user2);
        Assert.assertEquals(2, ratingList.size());
        for (Rating r:ratingList){
            Assert.assertEquals(user2.getId(), r.getUserId());
            if (!r.getMovieId().equals(movie1.getId())) {
                Assert.assertEquals(movie2.getId(), r.getMovieId());
            }
        }

        dba.deleteRating(rating1);
        dba.deleteRating(rating2);
    }

    @Test
    public void searchMovie() {
        List<Movie> movies = dba.searchMovie("Tolo", 1, 1);
        Assert.assertEquals(1, movies.size());
        Assert.assertEquals(movie2.getId(), movies.get(0).getId());

        movies = dba.searchMovie("Joker", 1, 1);
        Assert.assertEquals(1, movies.size());
        Assert.assertEquals(movie1.getId(), movies.get(0).getId());
    }

    @Test
    public void searchActor() {
    }

    @Test
    public void searchDirector() {
    }

    @Test
    public void searchCountry() {
    }

    @Test
    public void searchUser() {
    }
}