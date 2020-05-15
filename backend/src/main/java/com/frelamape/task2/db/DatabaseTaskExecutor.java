package com.frelamape.task2.db;


import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

@Repository
public class DatabaseTaskExecutor{
    private static final int MAX_RETRY = 5;

    @Autowired()
    @Qualifier("databaseAdapter")
    private DatabaseAdapter dba;

    /**
     * Updates the internal and total rating of the movie.
     *
     * This is done by incrementing the rating sum and count and then
     * recalculating the total_average.
     *
     * NB: both ratings must refer to the same movie.
     * NB: at least one rating must be non-null.
     *
     * @param oldRating old rating or null in case of insertion
     * @param newRating new rating or null in case of deletion
     * @return true if the update was successful, false otherwise.
     */
    @Async("taskExecutor")
    public void updateInternalRating(Rating oldRating,
                                        Rating newRating){
        if ((oldRating == null && newRating == null)
                || (oldRating != null && newRating != null && oldRating.getRating() == newRating.getRating()))
            return;

        // get movie info

        String movieId = null;
        if (oldRating != null){
            movieId = oldRating.getMovieId();
        }
        if (newRating != null){
            if (movieId != null && !movieId.equals(newRating.getMovieId()))
                return;
            else
                movieId = newRating.getMovieId();
        }

        Movie m = dba.getMovieDetails(movieId);
        if (m == null)
            return;

        AggregatedRating internalRating = null;
        for (AggregatedRating ar : m.getRatings()) {
            if (ar.getSource().equals("internal")) {
                internalRating = ar;
                break;
            }
        }
        // update internal rating

        Bson match;
        List<Bson> updates = new ArrayList<>();

        if (internalRating != null) {
            match = and(
                    eq("_id", movieId),
                    elemMatch("ratings", new Document() // always present
                            .append("source", "internal"))
            );

            double avg = internalRating.getAvgRating() != null ? internalRating.getAvgRating() : 0;
            int n = internalRating.getCount();
            if (oldRating == null) { // adding
                updates.add(inc("ratings.$.sum", newRating.getRating()));
                updates.add(inc("ratings.$.count", 1));

                avg = (avg * n + newRating.getRating()) / (n + 1);
                n++;
            } else if (newRating == null) { // deleting
                updates.add(inc("ratings.$.sum", -oldRating.getRating()));
                updates.add(inc("ratings.$.count", -1));

                if (n - 1 != 0)
                    avg = (avg * n - oldRating.getRating()) / (n - 1);
                else
                    avg = 0;
                n--;
            } else { // changing
                updates.add(inc("ratings.$.sum", newRating.getRating() - oldRating.getRating()));

                avg = avg + (newRating.getRating() - oldRating.getRating()) / n;
            }

            updates.add(set("ratings.$.last_update", new Date()));

            internalRating.setCount(n);
            internalRating.setAvgRating(avg);
        } else { // internal rating is missing and must be created
            match = eq("_id", movieId);
            assert oldRating == null; // this should be impossible
            internalRating = new AggregatedRating(
                    "internal",
                    newRating.getRating(),
                    1,
                    1.0,
                    new Date()
            );
            internalRating.setSum(newRating.getRating());
            updates.add(push("ratings", AggregatedRating.Adapter.toDBObject(internalRating)));
            m.getRatings().add(internalRating);
        }

        // update total rating

        double ar_sum = 0;
        int ar_count = 0;
        for (AggregatedRating ar : m.getRatings()) {
            if (ar.getAvgRating() != null && ar.getCount() > 0) {
                ar_sum += ar.getAvgRating() * ar.getWeight();
                ar_count++;
            }
        }

        if (ar_count > 0) {
            m.setTotalRating(ar_sum / ar_count);
        }

        if (ar_count > 0) {
            updates.add(set("total_rating", m.getTotalRating()));
        } else {
            updates.add(unset("total_rating"));
        }

        // commit updates

        dba.getMoviesCollection().updateOne(match, combine(updates));
    }
}