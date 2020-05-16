// 1 - Fetch movie from DB
Movie m = dba.getMovieDetails(movieId);

// 2- Build the update request
Bson match;
List<Bson> updates = new ArrayList<>();

// 2a - update internal rating

if (internalRating != null) { // there are previous ratings
    // match the related nested document
    match = and(
        eq("_id", movieId),
        elemMatch("ratings", new Document() // always present
            .append("source", "internal"))
    );

    if (oldRating == null) { // adding
        updates.add(inc("ratings.$.sum", newRating.getRating()));
        updates.add(inc("ratings.$.count", 1));
    } else if (newRating == null) { // deleting
        updates.add(inc("ratings.$.sum", -oldRating.getRating()));
        updates.add(inc("ratings.$.count", -1));
    } else { // changing
        updates.add(inc("ratings.$.sum", newRating.getRating() - oldRating.getRating()));
        updates.add(set("ratings.$.last_update", new Date()));
    }
    // [...] update internalRating
} else { // internal rating is missing and must be created
    match = eq("_id", movieId);
    // [...] create new internalRating
    updates.add(
        push("ratings", AggregatedRating.Adapter.toDBObject(internalRating))
    );
}

// 2b - update total rating 
// [...] calculate total rating 
if (ar_count > 0) { 
    updates.add(set("total_rating", m.getTotalRating()));
} else { // if no rating source found
    updates.add(unset("total_rating"));
}

// 3 - execute query on DB
dba.getMoviesCollection().updateOne(match, combine(updates));