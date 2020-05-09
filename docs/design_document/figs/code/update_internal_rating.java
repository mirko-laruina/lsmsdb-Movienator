// 1 - fetch movie
Movie m = dba.getMovieDetails(movieId);

// 2 - calculate new aggregated ratings
// [...]

// 3 - update database
if (oldUpdate == null) {// there is no previous internal rating entry
    match = and( // make sure no one added it in the meanwhile
        eq("_id", movieId),
        not(elemMatch("ratings", new Document().append("source", "internal")))
    );
    if (internalRating.getCount() != 0){ // push it if needs to be added
        updates.add(push("ratings", AggregatedRating.Adapter.toDBObject(internalRating)));
    }
} else { // there is a previous entry: replace or pull 
    match = and(    // make sure no one updated it in the meanwhile
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

if (m.getRatings().size() > 0) { // update total_rating
        updates.add(set("total_rating", m.getTotalRating()));
} else { // no rating available: unset it
        updates.add(unset("total_rating"));
}

UpdateResult result = moviesCollection.updateOne(match, combine(updates));
if (result.getModifiedCount() == 1)
    // ok
else
    // retry: someone modified it in the meanwhile
