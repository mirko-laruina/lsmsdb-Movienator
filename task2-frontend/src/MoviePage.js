import React, { useEffect } from 'react'

import { Rating } from '@material-ui/lab'
import { Typography, Grid, Chip } from '@material-ui/core'
import axios from 'axios'

import { baseUrl } from './utils'
import BasicPage from './BasicPage'
import MyCard from './MyCard'
import FilmListSkeleton from './FilmListSkeleton'

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

const months = ['January', 'February', 'March', 'April',
    'May', 'June', 'July', 'August', 'September',
    'October', 'November', 'December']
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
                                            movie.ratings.map((rating, i) => {
                                                return (
                                                    <Grid item xs={12 / movie.ratings.length} key={i}>
                                                        <Typography variant="h6" component="h3">
                                                            {rating.source} {rating.avgRating}/5
                                                        </Typography>
                                                        <Rating
                                                            name={rating.source + "-rating"}
                                                            size="large"
                                                            value={rating.avgRating}
                                                            max={5}
                                                            precision={0.1}
                                                            readOnly
                                                        />
                                                    </Grid>
                                                )
                                            })
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

                        </>
                }
            </MyCard>
            <br />
        </BasicPage >
    )
}