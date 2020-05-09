// 1 - define filters
List<Bson> conditions = new ArrayList<>();
if (minRating != -1)
    conditions.add(gte("total_rating", minRating));

if (maxRating != -1)
    conditions.add(lte("total_rating", maxRating));

if (director != null && !director.isEmpty()){
    List<Bson> directorConditions = new ArrayList<>();
    for (String s:director.split(" ")){
        directorConditions.add(regex("directors.name", s, "i"));
    }
    conditions.add(and(directorConditions.toArray(new Bson[]{})));
}

if (actor  != null && !actor.isEmpty()){
    List<Bson> actorConditions = new ArrayList<>();
    for (String s:actor.split(" ")){
        actorConditions.add(regex("characters.actor_name", s, "i"));
    }
    conditions.add(and(actorConditions.toArray(new Bson[]{})));
}

if (country != null && !country.isEmpty())
    conditions.add(eq("countries", country));

if (fromYear != -1){
    conditions.add(gte("year", fromYear));
}

if (toYear != -1)
    conditions.add(lte("year", toYear));

if (genre != null && !genre.isEmpty())
    conditions.add(eq("genres", genre));

// 2 - and conditions
Bson filters;

if (!conditions.isEmpty())
    filters = and(conditions.toArray(new Bson[]{}));
else
    filters = new BsonDocument();

// 3 - make the query
FindIterable<Document> movieIterable = moviesCollection
    .find(filters)
    .sort(sorting)
    .projection(include("title", "year", "poster", "genres", "total_rating", "description"))
    .skip(n*(page-1))
    .limit(n);