import React, { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Pagination } from '@material-ui/lab'
import RestrictedPage from './RestrictedPage'
import HistoryTable from './HistoryTable'
import HistoryPageSkeleton from './HistoryPageSkeleton'
import { baseUrl, errorHandler, force_disconnect } from './utils'
import { Typography, Grid, Button } from '@material-ui/core'
import axios from 'axios'

export default function HistoryPage(props) {
    const [movies, setMovies] = React.useState([]);
    const [pageCount, setPageCount] = React.useState(0);
    const [currentPage, setCurrentPage] = React.useState(1);
    const [loading, setLoading] = React.useState(true);
    const filmPerPage = 10;


    useEffect(() => {
        setLoading(true)
        if(!props.match.params.username && !localStorage.getItem('username')){
            return;
        }
        let url = baseUrl + "/user/"
            + (props.match.params.username ?
                props.match.params.username :
                localStorage.getItem('username'))
            + '/ratings'
        axios.get(url, {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                n: filmPerPage,
                page: currentPage
            }
        }).then((data) => {
            console.log(data.data)
            if (data.data.success) {
                setMovies(data.data.response.list)
                setPageCount(Math.ceil(parseInt(data.data.response.totalCount) / filmPerPage))
                setLoading(false);
            } else {
                alert(data.data.message)
                force_disconnect()
            }
        }).catch((response) => errorHandler(response))
    }, [currentPage])


    return (
        <RestrictedPage
            history={props.history}
            customAuthorization={() => {
                return localStorage.getItem('is_admin') === 'true'
                    || localStorage.getItem('username') === props.match.params.username
                    || (localStorage.getItem('username') && !props.match.params.username)
            }}
        >

            <Grid container>
                <Grid item xs={9}>
                    <Typography variant="h3" component="h1">
                        {
                            props.match.params.username ?
                                "Rating history for " + props.match.params.username :
                                "Rating history"
                        }
                    </Typography>
                </Grid>
                <Grid item xs={3}>
                    {
                        localStorage.getItem('is_admin') === 'true' &&
                        <Button fullWidth
                            variant="outlined"
                            size="large"
                            color="primary"
                            component={Link}
                            to={!props.match.params.username ?
                                "/profile" :
                                "/profile/" + props.match.params.username}>
                            Profile page
                        </Button>
                    }
                </Grid>
            </Grid>
            <br />
            {
                loading ?
                    <HistoryPageSkeleton />
                    :
                    <React.Fragment>
                        <HistoryTable
                            data={movies}
                            user={props.match.params.username}
                            readOnly={props.match.params.username && localStorage.getItem('username') !== props.match.params.username}
                        />
                        <br />
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
                    </React.Fragment>

            }
            <br />
        </RestrictedPage>
    )
}