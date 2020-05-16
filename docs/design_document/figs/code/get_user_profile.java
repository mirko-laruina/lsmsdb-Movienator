// 1 - fetch User from DB
User u = User.Adapter.fromDBObject(
        usersCollection.find(
                eq("username", username)
        ).first()
);

// 2 - calculate statistics

for (String[] field:new String[][]{actors, directors, genres}) {
    // for each aggregation (actors, directors, genres) th pipeline is the 
    // same so I just set the specific fields and reuse the same code in a 
    // loop
    AggregateIterable<Document> iterable = ratingsCollection
        .aggregate(Arrays.asList(
            Aggregates.match(eq("_id.user_id", u.getId())),
            // [...] lookup, project, unwind, unwind, group
            // sort by rating
            Aggregates.sort(descending("avg_rating", "movie_count")),
            // get three most liked
            Aggregates.limit(3)
    ));

    // 3 - add result to user profile
    if (field == actors){
        u.setFavouriteActors(
            Statistics.Adapter.fromDBObjectIterable(iterable, Person.class));
    } else if (field == directors){
        u.setFavouriteDirectors(
            Statistics.Adapter.fromDBObjectIterable(iterable, Person.class));
    } else if (field == genres){
        u.setFavouriteGenres(
            Statistics.Adapter.fromDBObjectIterable(iterable, Genre.class));
    }
}