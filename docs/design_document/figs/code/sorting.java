Bson sorting;
if (sortOrder == 1) {
    sorting = ascending(realSortBy);
} else if (sortOrder == -1) {
    sorting = descending(realSortBy);
} else {
    throw new RuntimeException("sortOrder must be 1 or -1.");
}