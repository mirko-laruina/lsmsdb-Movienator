import React from 'react'
import { useEffect } from 'react'
import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import { Typography, Grid} from '@material-ui/core'

import Pagination from '@material-ui/lab/Pagination';

import FilterDisplay from './FilterDisplay.js';
import Sorting from './Sorting.js';
import MovieListDisplay from './MovieListDisplay'

import { baseUrl } from './utils.js'
import axios from 'axios';

const styles = {
    cardRoot: {
        padding: '1em 3em',
    }
}

export default function BrowsePage(props) {
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({});
    const [movies, setMovies] = React.useState([]);
    const [pageCount, setPageCount] = React.useState(0);
    const [currentPage, setCurrentPage] = React.useState(1);
    const [loading, setLoading] = React.useState(true);
    const filmPerPage = 10;

    useEffect(() => {
        const browseRequest = () => {
            if( typeof(sortOpt.sortBy) === 'undefined'
                ||
                typeof(sortOpt.sortOrder) === 'undefined'){
                    //sortOpt isn't updated by the child component yet
                    //we should NOT do any request
                return
            }
            var sorting = {
                sortBy: sortOpt.sortBy === '0' ? 'release' :
                    sortOpt.sortBy === '1' ? 'rating' : 'title',
                sortOrder: sortOpt.sortOrder === '0' ? -1 : 1,
            }
            var reqParams = {
                ...filters,
                ...sorting,
                page: currentPage,
                n: filmPerPage
            }

            if(localStorage.getItem('username')){
                reqParams.sessionId = localStorage.getItem('sessionId')
            }

            axios.get(baseUrl + "movie/browse", {
                params: reqParams
            })
                .then(function (res) {
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
            <MyCard style={styles.cardRoot}>
                <FilterDisplay filters={filters} setFilters={setFilters} />
                <br />
                <Typography variant="h4">Browse movies</Typography>
                <Sorting noGroup sortOpt={sortOpt} handler={setSortOpt} />
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
            </MyCard>
        </BasicPage >
    )
}