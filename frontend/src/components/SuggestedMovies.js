import React, { useEffect } from 'react'
import Skeleton from '@material-ui/lab/Skeleton'
import MovieCarousel from './MovieCarousel'
import axios from 'axios';
import { baseUrl, errorHandler, httpErrorhandler } from '../utils';

export default function SuggestedMovies() {
    const [loading, setLoading] = React.useState(true)
    const [movies, setMovies] = React.useState([])
    const movieNumber = 12;

    useEffect(() => {
        axios.get(baseUrl + "movie/suggestion", {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                n: movieNumber
            }
        })
            .then(function (res) {
                if (res.data.success) {
                    setMovies(res.data.response.list)
                    setLoading(false)
                } else {
                    errorHandler(res.data.code, res.data.message)
                }
            }).catch((response) => httpErrorhandler(response))
    }, [])

    return (
        !loading ?
            <MovieCarousel movies={movies} />
            :
            <Skeleton width='100%' height="240px" />
    )
}