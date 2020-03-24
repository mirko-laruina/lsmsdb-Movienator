# API Specifications
The API base URL is: `http://example.com/api/v1/`.

All responses are like the one below:

```json
{
    "success": true|false,
    "message": "Error message (if success is false)",
    "response": {
        ...
    }
}
```

In what follows, only the response nested document is reported.

## `POST /auth/login`
Logs the user in.

NB: not logged-in sessions must be able to call this API!

### Input
```
username=<username>
password=<plain_text_password>
```

### Output
```json
{
    "sessionId": "<sessionId>",
    "is_admin": false
}
```

## `POST /auth/logout`
Logs the user out, terminating his session.

NB: this method is a POST even though it has no inputs since it changes the server internal state.

### Input
None

### Output
None (just success/failure).

## `POST /auth/register`
Registers the user.

NB: not logged-in sessions must be able to call this API!

### Input
```
username=<username>
password=<plain_text_password>
```

### Output
```json
{
    "sessionId": "<sessionId>"
}
```

## `GET /movie/browse`
Returns a list of movies with the given sorting and filters. Paging is supported.

NB: if a logged-in user browses the movies, his own rating is returned alongside
the overall rating.

### URL parameters
All parameters are optional. Default behaviour is sorting by release date (ascending) with no filters.
 - sortBy: one of ["release", "title", "rating"]. Ties are resolved on ascending title ordering.
 - sortOrder: 1 for ascending, -1 for descending
 - minRating: minimum rating (included)
 - maxRating: maximum rating (included)
 - director: director name (or part of)
 - actor: actor name (or part of)
 - country: country name (or part of)
 - fromYear: minimum release year (included)
 - toYear: maximum release year (included)
 - genre: genre that the movie must contain
 - n: number of elements per page (default: 10)
 - page: page number (default: 1)

### Output
```json
[
    {
        "_id": "tt7286456",
        "title": "Joker",
        "year": 2019,
        "poster": "https://m.media-amazon.com/images/M/[...].jpg",
        "genres": ["Crime", "Drama", "Thriller"],
        "description": " In Gotham City, mentally troubled comedian Arthur Fleck is disregarded and mistreated by society. He then embarks on a downward spiral of revolution and bloody crime. This path brings him face-to-face with his alter-ego: the Joker. ",
        "total_rating": 8.87,
        "user_rating": 7 (optional)
    },
    ...
]
```

## `GET /movie/search`
Returns a list of movies that match the given search string. Paging is supported.

NB: if a logged-in user browses the movies, his own rating is returned alongside
the overall rating.

### URL parameters
 - query: search query (required)
 - n: number of elements per page (default: 10)
 - page: page number (default: 1) 

### Output
```json
[
    {
        "_id": "tt7286456",
        "title": "Joker",
        "year": 2019,
        "poster": "https://m.media-amazon.com/images/M/[...].jpg",
        "genres": ["Crime", "Drama", "Thriller"],
        "total_rating": 8.87,
        "user_rating": 7 (optional)
    },
    ...
]
```

## `GET /movie/<id>`
Returns detailed information about a movie.

If the user is logged-in, this method will show his rating withing the ratings 
array.

### Output
```json
{
	"_id": "tt7286456",
	"title": "Joker",
	"original_title": "Joker",
	"runtime": 122,
	"country_code": "US",
	"country": "USA",
	"year": 2019,
	"date": "2019-10-04",
	"description": "In Gotham City, mentally troubled comedian [...]",
	"poster": "https://m.media-amazon.com/images/M/[...].jpg",
	"characters": [
		{
			"name": "Joker",
			"actor_name": "Joaquin Phoenix",
			"actor_id": "nm0001618"
		},
		...
	],
	"directors": [
		{
			"id": "nm0680846",
			"name": "Todd Phillips",
		}
	],
	"genres": ["Crime", "Drama", "Thriller"],
	"ratings": [
		{
			"source": "internal",
			"avgrating": 9,
			"count": 100,
			"weight": 2
		},
		{
			"source": "IMDb",
			"avgrating": 8.6,
			"count": 628981,
			"weight": 1
		},
		{
			"source": "user",
			"avgrating": 7,
			"count": 1,
			"weight": 0
		},
		...
	],
	"total_rating": 8.87
}
```

## `PUT /movie/<id>/rating`
Update/Inserts the user rating.

NB: only logged-in users.

### Input
```
rating=<rating>
```

### Output
None (just success/failure).

## `DELETE /movie/<id>/rating`
Deletes the user rating.

NB: only logged-in users.

### Output
None (just success/failure).

## `GET /movie/statistics`
Returns statistics of movies with the given sorting and aggregation. 
Paging is supported.

### URL parameters
All parameters are optional, except for the aggregation (groupBy). 
Default behaviour is sorting by rating (descending).
 - groupBy: one of ["country", "year", "director", "actor"]. 
 - sortBy: one of ["count", "rating", "alphabetic"]. Ties are resolved on ascending alphabetic ordering.
 - sortOrder: 1 for ascending, -1 for descending
 - minRating: minimum rating (included)
 - maxRating: maximum rating (included)
 - director: director name (or part of)
 - actor: actor name (or part of)
 - country: country name (or part of)
 - fromYear: minimum release year (included)
 - toYear: maximum release year (included)
 - genre: genre that the movie must contain
 - n: number of elements per page (default: 10)
 - page: page number (default: 1)

### Output
```json
[
    {
        "id": "US",
        "name": "USA",
        "count": 500,
        "rating": 8.87
    },
    ...
]
```

## `GET /user/<username>`
Returns user information (username, email, top-3 most liked actors, directors, 
genres).

NB: only the user himself and admins are allowed.

### Output
```json
{
    "username": "joker",
    "email": "joker@dccomics.com",
    "favourite_actors": [
		{
			"id": "nm0001618",
			"name": "Joaquin Phoenix",
			"avg_rating": 7.5,
			"rating_count": 2
		},
		...
	],
	"favourite_directors": [
		{
			"id": "nm0680846",
			"name": "Todd Phillips",
			"avg_rating": 7.7,
			"rating_count": 4
		},
		...
	],
	"favourite_genres": [
		{
			"name": "Crime",
			"avg_rating": 7.1,
			"rating_count": 4
		},
		...
	],

}
```

## `GET /user/<username>/ratings`
Returns a list of movies rated by the user.
Paging is supported.

NB: only the user herself and admins are allowed.

### URL parameters
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
[
    {
        "_id": 7286456,
        "title": "Joker",
        "year": 2019,
        "poster": "https://m.media-amazon.com/images/M/[...].jpg",
        "genres": ["Crime", "Drama", "Thriller"],
        "total_rating": 8.87,
        "user_rating": 7
    },
    ...
]
```

## `POST /user/<username>/rating/<movie_id>`
Update/Inserts the user rating.

NB: only the user himself and admins are allowed.

### Input
```
rating=<rating>
```

### Output
None (just success/failure).

## `DELETE /user/<username>/rating/<movie_id>`
Deletes the user rating.

NB: only the user herself and admins are allowed.

### Output
None (just success/failure).

## `POST /user/<username>/ban`
Bans the user and deletes all his ratings.

NB: only admins are allowed.

### Output
None (just success/failure).

## `GET /user/search`
Searches a user from a username (or part of).

NB: only admins are allowed.

### URL parameters
 - query: the query that the username (required)
 - limit: maximum number of results (optional, default = 10)

### Output
```
["username1, "username2", "usernameN"]
```

## `GET /ratings`
Returns the list of all ratings, sorted by date (descending).
Paging is supported.

NB: only admins are allowed.

### URL parameters
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
[
    {
        "username": "topolino.hackerino",
        "movie_id": 7286456,
        "title": "Joker",
        "year": 2019,
        "user_rating": 1
    },
    ...
]
```