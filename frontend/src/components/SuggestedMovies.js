import React, { useEffect } from 'react'
import Skeleton from '@material-ui/lab/Skeleton'
import MovieCarousel from './MovieCarousel'
import axios from 'axios';
import { baseUrl, errorHandler } from '../utils';

export default function SuggestedMovies() {
    const [movies, setMovies] = React.useState([])
    const movieNumber = 5;

    useEffect(() => {
        axios.get(baseUrl + "movie/suggestion", {
            params: {
                n: movieNumber
            }
        })
            .then(function (res) {
                if (res.data.success) {
                    setMovies(res.data.response.list)
                } else {
                    alert(res.data.message)
                    alert("You will be disconnected")
                    localStorage.removeItem('sessionId')
                    window.location.reload()
                }
            }).catch((response) => errorHandler(response))
    }, [])

    return (
        movies.length != 0 ?
            <MovieCarousel movies={movies} />
            :
            <Skeleton width='100%' height="240px" />
    )
}