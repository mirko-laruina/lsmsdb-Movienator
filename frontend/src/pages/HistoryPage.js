import React, { useEffect } from 'react'
import { Link } from 'react-router-dom'
import RestrictedPage from './RestrictedPage'
import HistoryTable from '../components/HistoryTable'
import HistoryPageSkeleton from '../skeletons/HistoryPageSkeleton'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import { Typography, Grid, Button } from '@material-ui/core'
import axios from 'axios'
import MyPagination from '../components/MyPagination'

export default function HistoryPage(props) {
    const [movies, setMovies] = React.useState([]);
    const [lastPage, setLastPage] = React.useState(true);
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
            if (data.data.success) {
                setMovies(data.data.response.list)
                setLastPage(data.data.response.lastPage)
                setLoading(false);
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
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
                            <MyPagination
                                lastPage={lastPage}
                                currentPage={currentPage}
                                onClick={(v) => setCurrentPage(v)} />
                        </Grid>
                    </React.Fragment>

            }
            <br />
        </RestrictedPage>
    )
}