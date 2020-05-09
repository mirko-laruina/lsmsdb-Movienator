FindIterable<Document> movieIterable = moviesCollection
    .find(text("\""+ query + "\""))
    .projection(Projections.metaTextScore("score"))
    .sort(Sorts.metaTextScore("score"))
    .skip(n*(page-1))
    .limit(n);