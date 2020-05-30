import React from 'react'
import { useEffect } from 'react'
import BasicPage from './BasicPage.js'
import { Typography, Grid } from '@material-ui/core'

import MyPagination from '../components/MyPagination'
import FilterDisplay from '../components/FilterDisplay.js';
import Sorting from '../components/Sorting.js';
import MovieListDisplay from '../components/MovieListDisplay'

import { baseUrl, errorHandler, httpErrorhandler } from '../utils.js'
import axios from 'axios';

export default function BrowsePage(props) {
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({});
    const [movies, setMovies] = React.useState([]);
    const [lastPage, setLastPage] = React.useState(true);
    const [currentPage, setCurrentPage] = React.useState(1);
    const [loading, setLoading] = React.useState(true);
    const filmPerPage = 10;
    const sorts = ["Realease", "Title", "Rating"];

    useEffect(() => {
        const browseRequest = () => {
            var reqParams = {
                ...filters,
                sortBy: typeof (sortOpt.sortBy) !== 'undefined' ? sorts[sortOpt.sortBy].toLowerCase() : undefined,
                sortOrder: sortOpt.sortOrder,
                page: currentPage,
                n: filmPerPage
            }

            if (localStorage.getItem('username')) {
                reqParams.sessionId = localStorage.getItem('sessionId')
            }

            console.log(reqParams)
            axios.get(baseUrl + "movie/browse", {
                params: reqParams
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
        browseRequest()
    }, [filters, sortOpt, currentPage]);

    return (
        <BasicPage history={props.history}>
            <FilterDisplay filters={filters} setFilters={setFilters} />
            <br />
            <Typography variant="h4">Browse movies</Typography>
            <Sorting noGroup sorts={sorts} options={sortOpt} setOpts={setSortOpt} />
            <MovieListDisplay
                loading={loading}
                numFilm={filmPerPage}
                array={movies}
            />
            <br />
            <MyPagination
                currentPage={currentPage}
                onClick={(v) => setCurrentPage(v)}
                lastPage={lastPage}
            />
        </BasicPage >
    )
}