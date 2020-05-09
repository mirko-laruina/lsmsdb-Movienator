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
     * This is done by updating the average adding, removing or updating
     * this rating.
     *
     * NB: both ratings must refer to the same movie.
     * NB: at least one rating must be non-null.
     *
     * @param oldRating old rating or null in case of insertion
     * @param newRating new rating or null in case of deletion
     * @return True if the update was successful, false otherwise.
     */
    @Async("taskExecutor")
    public void updateInternalRating(Rating oldRating,
                                        Rating newRating){
        if ((oldRating == null && newRating == null)
                || (oldRating != null && newRating != null && oldRating.getRating() == newRating.getRating()))
            return;

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


        for (int i = 0; i < MAX_RETRY; i++) {
            Movie m = dba.getMovieDetails(movieId);
            if (m == null)
                return;

            Date oldUpdate = null;

            AggregatedRating internalRating = null;
            for (AggregatedRating ar : m.getRatings()) {
                if (ar.getSource().equals("internal")) {
                    internalRating = ar;
                    break;
                }
            }

            if (internalRating == null) {
                internalRating = new AggregatedRating(
                        "internal",
                        0.0,
                        0,
                        1.0,
                        new Date()
                );
                m.getRatings().add(internalRating);
            } else {
                oldUpdate = internalRating.getLastUpdate();
                internalRating.setLastUpdate(new Date());
            }

            if (oldRating == null) {
                internalRating.setAvgRating(
                        (internalRating.getAvgRating() * internalRating.getCount()
                                + newRating.getRating())
                                / (internalRating.getCount() + 1)
                );
                internalRating.setCount(internalRating.getCount() + 1);
            } else if (newRating == null) {
                internalRating.setAvgRating(
                        (internalRating.getAvgRating() * internalRating.getCount()
                                - oldRating.getRating())
                                / (internalRating.getCount() - 1)
                );
                internalRating.setCount(internalRating.getCount() - 1);
            } else {
                internalRating.setAvgRating(
                        internalRating.getAvgRating()
                                + (newRating.getRating() - oldRating.getRating())
                                / internalRating.getCount()
                );
            }
            if (internalRating.getCount() == 0) {
                m.getRatings().remove(internalRating);
            }

            double sum = 0;
            for (AggregatedRating ar : m.getRatings()) {
                sum += ar.getAvgRating() * ar.getWeight();
            }

            if (m.getRatings().size() > 0) {
                m.setTotalRating(sum / m.getRatings().size());
            }

            List<Bson> updates = new ArrayList<>();
            Bson match;
            if (oldUpdate == null) {
                match = and(
                        eq("_id", movieId),
                        not(elemMatch("ratings", new Document().append("source", "internal")))
                );
                if (internalRating.getCount() != 0){ // push it if it exists
                    updates.add(push("ratings", AggregatedRating.Adapter.toDBObject(internalRating)));
                }
            } else {
                match = and(
                        eq("_id", movieId),
                        elemMatch("ratings", new Document()
                                .append("source", "internal")
                                .append("last_update", oldUpdate))
                );
                if (internalRating.getCount() != 0) { // replace it if it exists
                    updates.add(set("ratings.$", AggregatedRating.Adapter.toDBObject(internalRating)));
                } else { // else pull it
                    updates.add(pull("ratings", eq("source", "internal")));
                }
            }

            if (m.getRatings().size() > 0) {
                updates.add(set("total_rating", m.getTotalRating()));
            } else {
                updates.add(unset("total_rating"));
            }

            UpdateResult result = dba.getMoviesCollection().updateOne(match, combine(updates));
            if (result.getModifiedCount() == 1)
                return;
            // otherwise another backend updated it, let's retry and hope we will succeed
        }
        // tried too many times, let's give up
        // scraper will fix this consistency in a few days
    }
}