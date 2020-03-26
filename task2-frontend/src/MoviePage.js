import React, { useEffect } from 'react'

import { Rating } from '@material-ui/lab'
import { Typography, Grid, Chip } from '@material-ui/core'
import axios from 'axios'

import { baseUrl } from './utils'
import BasicPage from './BasicPage'
import MyCard from './MyCard'
import FilmListSkeleton from './FilmListSkeleton'

const input = {
    "_id": "tt7286456",
    "title": "Joker",
    "original_title": "Joker",
    "runtime": 122,
    "countries": ["USA", "Canada"],
    "original_language": "English",
    "year": 2019,
    "date": "2019-10-04",
    "description": "In Gotham City, mentally troubled comedian [...]",
    "storyline": "Joker centers around an origin of the iconic arch [...]",
    "tagline": "Put on a happy face.",
    "poster": "https://m.media-amazon.com/images/M/MV5BOTQ2ZTIwODMtMTFiNS00Njk0LWExNzEtZGFmOGY2OGY0YjQyXkEyXkFqcGdeQXVyMTQ3Njg3MQ@@._V1_.jpg",
    "mpaa": "Rated R for strong bloody violence, disturbing behavior, [...]",
    "budget": 55000000,
    "gross": 1074251311,
    "characters": [
        {
            "name": "Joker",
            "actor_name": "Joaquin Phoenix",
            "actor_id": "nm0001618"
        },
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
    ],
    "total_rating": 8.87
}

const styles = {
    cardRoot: {
        padding: '1em 3em',
    },
    img: {
        maxWidth: '100%',
        maxHeight: '100%'
    },
    genre: {
        marginRight: '0.5em',
        fontWeight: 'bold',
    }
}

const months = new Array('January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December')
const getDate = function (stringDate) {
    var date = new Date(stringDate)
    var print_date = months[date.getMonth()] + " " + date.getDate() + ", " + date.getFullYear()
    return print_date
}

export default function MoviePage(props) {
    const [movie, setMovie] = React.useState(null)

    const genGenreChips = (movie) => {
        return movie.genres.map((gen, index) => (
            <Chip
                key={index}
                label={gen}
                variant="outlined"
                color="primary"
                style={styles.genre} />
        ))
    }

    useEffect(() => {
        var url = baseUrl + 'movie/' + props.match.params.id
        axios.get(url).then((data) => {
            if (data.data.success) {
                console.log(data.data.response)
                setMovie(data.data.response)
            }
        })
    }, [props.match.params.id])

    return (
        <BasicPage history={props.history}>
            <MyCard style={styles.cardRoot}>
                {
                    !movie ?
                        <FilmListSkeleton />
                        :
                        <>
                            <br />

                            <Typography
                                variant="h3"
                                component="h1"
                            >
                                {movie.title} ({movie.year})
                            </Typography>
                            {movie.tagline ?
                                <Typography variant="h6" component="h2">
                                    {movie.tagline}
                                </Typography>
                                :
                                <br />
                            }
                            <br />
                            <Grid container spacing={4}>
                                <Grid item xs={4}>
                                    <img alt={movie.title}
                                        src={movie.poster ? movie.poster : require('./blank_poster.png')}
                                        style={styles.img} />
                                </Grid>
                                <Grid item xs={8}>
                                    {movie.totalRating ?
                                        <Grid container>
                                            <Grid item xs={8}>
                                                {genGenreChips(movie)}
                                            </Grid>
                                            <Grid item xs={4}>
                                                <Rating name="avg-rating" size="large" value={movie.totalRating} max={5} precision={0.1} readOnly />
                                            </Grid>
                                        </Grid>
                                        :
                                        <React.Fragment>
                                        {genGenreChips(movie)}
                                        <br />
                                        </React.Fragment>
                                    }
                                    <br />
                                    {movie.description &&
                                        <Typography
                                            variant="body1"
                                            component="p"
                                        >
                                            {movie.description}
                                            <br />
                                            <br />
                                        </Typography>
                                    }
                                    {movie.directors.length !== 0 &&
                                        <Typography
                                            variant="body1"
                                            component="p"
                                        >
                                            <b>Directed by</b>: {movie.directors.map((dir, i) => {
                                                var director = dir.name
                                                if (i !== 0) {
                                                    director = ", " + director
                                                }
                                                return director
                                            })}
                                        </Typography>
                                    }
                                    <Typography
                                        variant="body1"
                                        component="p"
                                    >
                                        <b>Runtime</b>: {Math.floor(movie.runtime / 60)}h {movie.runtime % 60}m
                                    </Typography>
                                    {movie.characters.length !== 0 &&
                                        <Typography
                                            variant="body1"
                                            component="p"
                                        >
                                            <b>Starring</b>: {movie.characters.map((char, i) => {
                                                var actor = char.actor.name + " (" + char.name + ")"
                                                if (i !== 0) {
                                                    actor = ", " + actor
                                                }
                                                return actor
                                            })

                                            }
                                        </Typography>
                                    }
                                    {
                                        movie.date &&
                                        <Typography
                                            variant="body1"
                                            component="p"
                                        >
                                            <b>Released on</b>: {getDate(movie.date)}
                                        </Typography>
                                    }
                                </Grid>
                            </Grid>
                        </>
                }
            </MyCard>
        </BasicPage >
    )
}