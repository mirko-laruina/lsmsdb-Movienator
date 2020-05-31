import React, { useEffect } from 'react'
import { Rating } from '@material-ui/lab'
import { Typography, Grid, Chip } from '@material-ui/core'
import axios from 'axios'

import { baseUrl, getDate, errorHandler, httpErrorhandler } from '../utils'
import BasicPage from './BasicPage'
import UserRating from '../components/UserRating'
import MoviePageSkeleton from '../skeletons/MoviePageSkeleton'

const styles = {
    img: {
        maxWidth: '100%',
        maxHeight: '100%'
    },
    genre: {
        marginRight: '0.5em',
        fontWeight: 'bold',
    }
}

export default function MoviePage(props) {
    const [movie, setMovie] = React.useState(null)

    const genGenreChips = (movie) => {
        return movie.genres.map((gen, index) => {
            return (
                <Chip
                    key={index}
                    label={gen}
                    variant="outlined"
                    color="primary"
                    style={styles.genre} />
            )
        })
    }

    const displayRatings = (ratings) => {
        let len = 0
        movie.ratings.map((rating, i) => {
            if (rating.source !== 'user') {

                if (rating.count > 0) {
                    len += 1
                }
                if (rating.source === 'internal') {
                    movie.ratings[i].source = 'Movienator'
                }
            }
            return movie.ratings[i]
        })

        return movie.ratings.map((rating, i) => {
            if (rating.source === 'user') {
                return null
            }
            else if (rating.count > 0) {
                return (
                    <Grid item xs={12 / len} key={i}>
                        <Typography variant="h6" component="h3">
                            {rating.source} {rating.avgRating * rating.weight}/5
                        </Typography>
                        <Rating
                            name={rating.source + "-rating"}
                            size="large"
                            value={rating.avgRating * rating.weight}
                            max={5}
                            precision={0.1}
                            readOnly
                        />
                    </Grid>
                )
            }
            return null
        })
    }

    useEffect(() => {
        let url = baseUrl + 'movie/' + props.match.params.id
        let params = {}
        if (localStorage.getItem('username')) {
            params.sessionId = localStorage.getItem('sessionId')
        }
        axios.get(url, { params: params }).then((data) => {
            if (data.data.success) {
                setMovie(data.data.response)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }, [props.match.params.id])

    return (
        <BasicPage history={props.history}>
            {
                !movie ?
                    <MoviePageSkeleton />
                    :
                    <React.Fragment>
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
                                    src={movie.poster ? movie.poster : require('../assets/blank_poster.png')}
                                    style={styles.img} />
                            </Grid>
                            <Grid item xs={8}>
                                {movie.totalRating ?
                                    <Grid container>
                                        <Grid item xs={8}>
                                            {genGenreChips(movie)}
                                        </Grid>
                                        <Grid item xs={4} align="right">
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
                                {window.localStorage.getItem('username') &&
                                    <Grid container spacing={1} direction="row" alignContent="center">
                                        <Grid item>
                                            <Typography
                                                variant="body1"
                                                component="p"
                                            >
                                                <b>Your rating</b>:
                                            </Typography>
                                        </Grid>
                                        <Grid item>
                                            <UserRating
                                                movieId={movie.id}
                                                rating={movie.userRating}
                                                showDelete />
                                        </Grid>
                                    </Grid>
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
                                {movie.runtime &&
                                    <Typography
                                        variant="body1"
                                        component="p"
                                    >
                                        <b>Runtime</b>: {Math.floor(movie.runtime / 60)}h {movie.runtime % 60}m
                                        </Typography>
                                }
                                {movie.characters.length !== 0 &&
                                    <Typography
                                        variant="body1"
                                        component="p"
                                    >
                                        <b>Starring</b>: {movie.characters.map((char, i) => {
                                            var actor = char.actor.name
                                            if (char.name) {
                                                actor += " (" + char.name + ")"
                                            }
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
                                <br />

                            </Grid>
                        </Grid>
                        <br />
                        {movie.storyline &&
                            <>
                                <Typography
                                    variant="h4" component="h2">
                                    Storyline
                                    </Typography>
                                <br />
                                <Typography variant="body1" component="p">
                                    {movie.storyline}
                                </Typography>
                                <br />
                            </>
                        }
                        {
                            movie.ratings && movie.ratings.length !== 0 &&
                            <React.Fragment>
                                <Typography
                                    variant="h4" component="h2">
                                    Ratings
                                    </Typography>
                                <br />
                                <Grid container align="center">
                                    {
                                        displayRatings(movie.ratings)
                                    }
                                </Grid>
                                <br />
                            </React.Fragment>
                        }
                        {((movie.countries && movie.countries.length !== 0) ||
                            movie.originalTitle ||
                            movie.originalLanguage ||
                            movie.mpaa) &&
                            <>
                                < Typography
                                    variant="h4" component="h2">
                                    Additional details
                                </Typography>
                                {movie.countries && movie.countries.length !== 0 &&
                                    <Typography
                                        variant="body1"
                                        component="p"
                                    >
                                        {
                                            movie.countries.length === 1 &&
                                            <React.Fragment>
                                                <b>Country</b>: {movie.countries[0]}
                                            </React.Fragment>
                                        }
                                        {
                                            movie.countries.length > 1 &&
                                            <React.Fragment>
                                                <b>Countries</b>: {movie.countries.map((country, i) => {
                                                    if (i === 0) {
                                                        return country
                                                    }
                                                    return ", " + country
                                                })}
                                            </React.Fragment>
                                        }
                                    </Typography>
                                }
                                {
                                    movie.originalTitle &&
                                    <Typography variant="body1" component="p">
                                        <b>Original title</b>: {movie.originalTitle}
                                    </Typography>
                                }
                                {
                                    movie.originalLanguage &&
                                    <Typography variant="body1" component="p">
                                        <b>Original language</b>: {movie.originalLanguage}
                                    </Typography>
                                }
                            </>
                        }

                    </React.Fragment>
            }
        </BasicPage >
    )
}