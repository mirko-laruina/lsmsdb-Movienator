import React from 'react'
import { useEffect } from 'react'
import BasicPage from './BasicPage.js'
import { Typography, Grid } from '@material-ui/core'

import Pagination from '@material-ui/lab/Pagination';

import FilterDisplay from './FilterDisplay.js';
import Sorting from './Sorting.js';
import MovieListDisplay from './MovieListDisplay'

import { baseUrl } from './utils.js'
import axios from 'axios';

export default function BrowsePage(props) {
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({});
    const [movies, setMovies] = React.useState([]);
    const [pageCount, setPageCount] = React.useState(0);
    const [currentPage, setCurrentPage] = React.useState(1);
    const [loading, setLoading] = React.useState(true);
    const filmPerPage = 10;
    const sorts = ["Realease", "Title", "Rating"];

    useEffect(() => {
        const browseRequest = () => {
            var reqParams = {
                ...filters,
                sortBy: sortOpt.sortBy ? sorts[sortOpt.sortBy].toLowerCase() : undefined,
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
            })
                .then(function (res) {
                    console.log(res)
                    if (res.data.success) {
                        setMovies(res.data.response.list)
                        setPageCount(Math.ceil(parseInt(res.data.response.totalCount) / filmPerPage))
                        setLoading(false);
                        console.log(res.data)
                    }
                })
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
            <Grid container justify="center">
                <Pagination shape="rounded"
                    showFirstButton
                    showLastButton
                    color="primary"
                    size="large"
                    count={pageCount}
                    page={currentPage}
                    onChange={(e, v) => setCurrentPage(v)} />
            </Grid>
        </BasicPage >
    )
}