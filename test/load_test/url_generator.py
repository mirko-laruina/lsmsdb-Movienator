#!/bin/env python3

base = "http://localhost:8080/api/v1/"
pages = []

# browse no filters
#pages += [
#    f"movie/browse?sortBy={sortBy}&sortOrder={sortOrder}&page={page}\n"
#    for sortBy in ["release", "title", "rating"]
#    for sortOrder in [1,-1]
#    for page in range(1,10)
#]*20
#print("browse no filters", len(pages))
#
# browse w/ filters min/max
pages += [
    f"movie/browse?sortBy={sortBy}&sortOrder={sortOrder}&minRating={minRating}&maxRating={maxRating}&fromYear={fromYear}&toYear={toYear}\n"
    for sortBy in ["release", "title", "rating"]
    for sortOrder in [1,-1]
    for minRating in range(0,6)
    for maxRating in range(0,6)
    for fromYear in range(1950,2020, 20)
    for toYear in range(1950,2020, 20)
    if minRating < maxRating
    if fromYear < toYear
]
print("browse w/ filters min/max", len(pages))

# browse w/ filter on actor
pages += [
    f"movie/browse?sortBy={sortBy}&sortOrder={sortOrder}&actor={actor}\n"
    for sortBy in ["release", "title", "rating"]
    for sortOrder in [1,-1]
    for actor in ["Tom%20Hanks", "Will%20Smith", "Brad%20Pitt", "Johnny%20Depp", "Leonardo%20DiCaprio", "Robert%20De%20Niro", "Robert%20Downey", "Morgan%20Freeman", "Harrison%20Ford"]
]*10
print("browse w/ filter on actor", len(pages))

# movie search
pages += [
   f"movie/search?query={query}\n"
   for query in ["godfather", "casablanca", "batman", "titanic", "forrest", "jojo", "america", "taxi", "shining", "parasyte", "shark"]
]*200

print("movie search", len(pages))

movie detail
pages += [
   f"movie/{id}\n"
   for id in ['tt0033202', 'tt0033630', 'tt0060633', 'tt0103750', 'tt0161401', 'tt0161402', 'tt0175632', 'tt0181625', 'tt0183432', 'tt0204253', 'tt0204270', 'tt0220502', 'tt0223555', 'tt0345281', 'tt0420842', 'tt0898279', 'tt0953497', 'tt1050198', 'tt10952938', 'tt11084380', 'tt1371108', 'tt1593735', 'tt1795707', 'tt1806925', 'tt1853612', 'tt2294803', 'tt2463820', 'tt2567606', 'tt2668152', 'tt2819370', 'tt2982488', 'tt3293190', 'tt10608660', 'tt11972952', 'tt11804094', 'tt11930236', 'tt1690231', 'tt1483764', 'tt1082858', 'tt0428925', 'tt6687948', 'tt9170952', 'tt1041737', 'tt5306474', 'tt12058866', 'tt1527819', 'tt1489151', 'tt11026890', 'tt1182879', 'tt1537853', 'tt6735740', 'tt6842524', 'tt11010206', 'tt7459096', 'tt7379662', 'tt8995266', 'tt3458312', 'tt0342423', 'tt0111161', 'tt0816692', 'tt1381288', 'tt0274494', 'tt2248647', 'tt2478440', 'tt5532258', 'tt5768980', 'tt8893296', 'tt1205585', 'tt2122378', 'tt1082592', 'tt2582626', 'tt3894774', 'tt7385120', 'tt9186872', 'tt0155199', 'tt0157374', 'tt0444490', 'tt2315610', 'tt2395469', 'tt3525174', 'tt5814480', 'tt7738784', 'tt9715646', 'tt0953321', 'tt2120779', 'tt0041736', 'tt0052268', 'tt0096403', 'tt0194247', 'tt0432189', 'tt0436746', 'tt0456613', 'tt0860906', 'tt10380266', 'tt10916326', 'tt1686329', 'tt6127136', 'tt6399296', 'tt6767400', 'tt0068646']
]*20

print("movie detail", len(pages))

# statistics
pages += [
   f"movie/statistics?groupBy={groupBy}&sortBy={sortBy}&sortOrder={sortOrder}&page={page}\n"
   for groupBy in ["country", "year", "director", "actor"]
   for sortBy in ["count", "rating", "alphabetic"]
   for sortOrder in [1,-1]
   for page in range(1,10,2)
]

print("statistics", len(pages))


statistics w/ filters min/max
pages += [
   f"movie/statistics?groupBy={groupBy}&sortBy={sortBy}&sortOrder={sortOrder}&minRating={minRating}&maxRating={maxRating}&fromYear={fromYear}&toYear={toYear}\n"
   for groupBy in ["country", "year", "director", "actor"]
   for sortBy in ["count", "rating", "alphabetic"]
   for sortOrder in [1,-1]
   for minRating in range(0,6,2)
   for maxRating in range(0,6,2)
   for fromYear in range(1950,2020, 25)
   for toYear in range(1950,2020, 25)
   if minRating < maxRating
   if fromYear < toYear
]

print("statistics w/ filters min/max", len(pages))

# statistics w/ filter on actor
pages += [
   f"movie/statistics?groupBy={groupBy}&sortBy={sortBy}&sortOrder={sortOrder}&actor={actor}\n"
   for groupBy in ["country", "year", "director", "actor"]
   for sortBy in ["count", "rating", "alphabetic"]
   for sortOrder in [1,-1]
   for actor in ["Tom Hanks", "Will Smith", "Brad Pitt", "Johnny Depp", "Leonardo DiCaprio", "Robert De Niro", "Robert Downey", "Morgan Freeman", "Harrison Ford"]
]

print("statistics w/ filter on actor", len(pages))

with open("siege_urls.txt", 'w') as f:
    f.writelines([base+page for page in pages])
