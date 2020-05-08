AggregateIterable<Document> iterable = moviesCollection.aggregate(
        Arrays.asList(
                Aggregates.match(filters),
                Aggregates.unwind("$" + realUnwindBy),
                Aggregates.group(
                        "$" + realGroupBy,
                        Accumulators.first("name", "$" + realGroupName),
                        Accumulators.avg("avg_rating", "$total_rating"),
                        Accumulators.sum("movie_count", 1)
                ),
                Aggregates.sort(sorting),
                Aggregates.skip(n*(page-1)),
                Aggregates.limit(n)
        )
);