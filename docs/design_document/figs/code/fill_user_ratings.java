// 1 - build list of ids of the movies I'm interested in
List<String> ids = new ArrayList<>();
for (Movie m:movies){
    ids.add(m.getId());
}

// 2 - fetch corresponding ratings from database
FindIterable<Document> ratingIterable = ratingsCollection.find(
    and(
        eq("_id.user_id", u.getId()),
        in("_id.movie_id", ids)
    )
);