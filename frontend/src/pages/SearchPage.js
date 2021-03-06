import React from 'react'
import { useEffect } from 'react'
import BasicPage from './BasicPage.js'
import { Typography, Grid, FormControl, InputBase } from '@material-ui/core'
import SearchIcon from '@material-ui/icons/Search'
import MyPagination from '../components/MyPagination';
import MovieListDisplay from '../components/MovieListDisplay'

import { baseUrl, errorHandler, httpErrorhandler } from '../utils.js'
import axios from 'axios';

export default function BrowsePage(props) {
    const [movies, setMovies] = React.useState([]);
    const [lastPage, setLastPage] = React.useState(true)
    const [currentPage, setCurrentPage] = React.useState(1);
    const [loading, setLoading] = React.useState(true);
    const [searchValue, setSearchValue] = React.useState("");
    const [queryValue, setQueryValue] = React.useState(props.match.params.query);
    const filmPerPage = 10;

    useEffect(() => {
        let params = {
            query: queryValue,
            n: filmPerPage,
            page: currentPage,
        }
        if (localStorage.getItem('username')) {
            params.sessionId = localStorage.getItem('sessionId')
        }
        const searchRequest = () => {
            axios.get(baseUrl + "movie/search", {
                params: params
            }).then(function (res) {
                if (res.data.success) {
                    setMovies(res.data.response.list)
                    setLastPage(res.data.response.lastPage)
                    setLoading(false);
                } else {
                    errorHandler(res.data.code, res.data.message)
                }
            }).catch((error) => httpErrorhandler(error))
        }

        setLoading(true)
        searchRequest()
    }, [currentPage, props.match.params.query, queryValue]);

    return (
        <BasicPage history={props.history}>
            <br />
            <Grid container>
                <Grid item xs={9}>
                    <Typography variant="h4">Search results for: {queryValue}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <form onSubmit={(e) => {
                        setQueryValue(searchValue)
                        props.history.push('/results/' + searchValue)
                        e.preventDefault()
                    }}>
                        <FormControl >
                            <InputBase
                                placeholder="Search movie"
                                value={searchValue}
                                color="secondary"
                                onChange={(e) => setSearchValue(e.target.value)}
                                endAdornment={
                                    <SearchIcon color="primary" style={{
                                        marginLeft: '10px'
                                    }} />
                                }
                                inputProps={{ 'aria-label': 'search' }}
                            />
                        </FormControl>
                    </form>
                </Grid>
            </Grid>
            <MovieListDisplay
                loading={loading}
                numFilm={filmPerPage}
                array={movies}
            />
            <Grid container justify="center">
                <MyPagination 
                    lastPage={lastPage}
                    currentPage={currentPage}
                    onClick={(v) => {
                        setCurrentPage(v);
                        window.scrollTo(0, 0);
                    }} />
            </Grid>
        </BasicPage >
    )
}