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
    "isAdmin": false
}
```

## `POST /auth/password`
Changes the password of the currenlty logged-in user.

NB: session ID is required.

### Input
```
password=<plain_text_password>
```

### Output
None, just success/failure.

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
 - country: country name
 - fromYear: minimum release year (included)
 - toYear: maximum release year (included)
 - genre: genre that the movie must contain
 - n: number of elements per page (default: 10)
 - page: page number (default: 1)

### Output
```json
{
	"list": [
		{
			"_id": "tt7286456",
			"title": "Joker",
			"year": 2019,
			"poster": "https://m.media-amazon.com/images/M/[...].jpg",
			"genres": ["Crime", "Drama", "Thriller"],
			"description": " In Gotham City, mentally troubled comedian Arthur Fleck is disregarded and mistreated by society. He then embarks on a downward spiral of revolution and bloody crime. This path brings him face-to-face with his alter-ego: the Joker. ",
			"totalRating": 8.87,
			"userRating": 7 (optional)
		},
		...
	],
	"totalCount": 51500
}
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
{
	"list": [
		{
			"_id": "tt7286456",
			"title": "Joker",
			"year": 2019,
			"poster": "https://m.media-amazon.com/images/M/[...].jpg",
			"genres": ["Crime", "Drama", "Thriller"],
			"totalRating": 8.87,
			"userRating": 7 (optional)
		},
		...
	],
	"totalCount": 5
}
```


## `GET /movie/suggestion`
Returns a list of movies that the user might like.

The totalCount in this case is the number of results, not the total number.

NB: only registered users.

### URL parameters
 - n: number of suggestions (default: 5)

### Output
```json
{
	"list": [
		{
			"_id": "tt7286456",
			"title": "Joker",
			"year": 2019,
			"poster": "https://m.media-amazon.com/images/M/[...].jpg"
		},
		...
	],
	"totalCount": 5
}
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
	"originalTitle": "Joker",
	"runtime": 122,
	"countries": ["USA", "Canada"],
	"originalLanguage": "English",
	"year": 2019,
	"date": "2019-10-04",
	"description": "In Gotham City, mentally troubled comedian [...]",
	"storyline": "Joker centers around an origin of the iconic arch [...]",
	"tagline": "Put on a happy face.",
	"poster": "https://m.media-amazon.com/images/M/[...].jpg",
	"mpaa": "Rated R for strong bloody violence, disturbing behavior, [...]",
	"budget": 55000000,
	"gross": 1074251311, 
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
		}
		...
	],
	"totalRating": 8.87,
	"userRating": 7
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
 - country: country name
 - fromYear: minimum release year (included)
 - toYear: maximum release year (included)
 - genre: genre that the movie must contain
 - n: number of elements per page (default: 10)
 - page: page number (default: 1)

### Output
```json
{
	"list": [
		{
			"aggregator":{
				"id": "US",
				"name": "USA"
			},
			"movieCount": 500,
			"avgRating": 8.87
		},
		...
	],
	"totalCount": 500
}
```

## `GET /user/<username>`
Returns user information (username, email, top-3 most liked actors, directors, 
genres).

### Output
```json
{
    "username": "joker",
    "email": "joker@dccomics.com",
	"followed": true,
	"following": false,
    "favourite_actors": [
		{
			"aggregator": {
				"id": "nm0001618",
				"name": "Joaquin Phoenix"
			}
			"avgRating": 7.5,
			"ratingCount": 2
		},
		...
	],
	"favourite_directors": [
		{
			"aggregator": {
				"id": "nm0680846",
				"name": "Todd Phillips"
			}
			"avgRating": 7.7,
			"ratingCount": 4
		},
		...
	],
	"favourite_genres": [
		{
			"aggregator": {
				"name": "Crime"
			}
			"avgRating": 7.1,
			"ratingCount": 4
		},
		...
	],

}
```

## `GET /user/<username>/ratings`
Returns a list of movies rated by the user.
Paging is supported.

### URL parameters
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
{
	"list": [
		{
			"_id": 7286456,
			"title": "Joker",
			"year": 2019,
			"poster": "https://m.media-amazon.com/images/M/[...].jpg",
			"genres": ["Crime", "Drama", "Thriller"],
			"totalRating": 8.87,
			"userRating": 7
		},
		...
	],
	"totalCount": 5
}
```

## `GET /user/<username>/followers`
Returns a list of users following the given user.

If the user issuing the query is registered, he will also receive information
about whether the queried user is following him or being followed by him.

Paging is supported.

### URL parameters
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
{
	"list": [
		{
			"username": "joker",
			"followed": true
		},
		...
	],
	"totalCount": 25
}
```

## `GET /user/<username>/followings`
Returns a list of users following the given user.

If the user issuing the query is registered, he will also receive information
about whether the queried user is following him or being followed by him.

Paging is supported.

### URL parameters
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
{
	"list": [
		{
			"username": "joker"
		},
		...
	],
	"totalCount": 25
}
```


## `GET /users/suggested`
Returns a list of user suggestions.

Output is paged-like to uniform with the other APIs but paging is not supported.

The totalCount in this case is the number of results, not the total number.

NB: only the user himself can call this API.

### URL parameters
 - n: number of suggestions (optional, default: 10)

### Output
```json
{
	"list": [
		{
			"username": "joker"
		},
		...
	],
	"totalCount": 10
}
```

## `GET /user/<username>/social`
Returns the result of the 3 above APIs aggretated in a single one since they 
are usually called together.

Only first page is retrieved for each query.

NB: only the user himself will receive the list of suggested users

### URL parameters
 - n_followers: number of followers to show (optional, default: 10)
 - n_followings: number of followed users to show (optional, default: 10)
 - n_suggestions: number of suggestions to show (optional, default: 10)

### Output
```json
{
	"followers": {
		"list": [
			{
				"username": "joker",
				"followed": true
			},
			...
		],
		"totalCount": 25
	},
	"followings": {
		"list": [
			{
				"username": "joker"
			},
			...
		],
		"totalCount": 25
	},
	"suggestions": {
		"list": [
			{
				"username": "joker"
			},
			...
		],
		"totalCount": 10
	},
}
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
Searches a user from a username (or part of). Paging is supported.

### URL parameters
 - query: the query that the username (required)
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
{
	"list": [
		{
			"username": "joker",
			"followed": true,
			"following": false
		},
		...
	],
	"totalCount": 100
}
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
{
	"list": [
		{
			"username": "topolino.hackerino",
			"movieId": "tt7286456",
			"title": "Joker",
			"year": 2019,
			"rating": 1,
			"date": "2020-07-15"
		},
		...
	],
	"totalCount": 5100
}
```

## `GET /ratings/friends`
Returns the list of all ratings from friends, sorted by date (descending).
Paging is supported.

NB: only registered users are allowed.

### URL parameters
 - n: number of elements per page (optional, default: 10)
 - page: page number (optional, default: 1)

### Output
```json
{
	"list": [
		{
			"username": "topolino.hackerino",
			"movieId": "tt7286456",
			"title": "Joker",
			"year": 2019,
			"rating": 1,
			"date": "2020-07-15"
		},
		...
	],
	"totalCount": 5100
}
```