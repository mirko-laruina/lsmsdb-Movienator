import React, { useEffect } from 'react'
import { Typography, FormControl, OutlinedInput, InputAdornment, IconButton, Grid } from '@material-ui/core'
import Pagination from '@material-ui/lab/Pagination'
import RestrictedPage from './RestrictedPage'
import HistoryTable from '../components/HistoryTable'
import ControlPanelSkeleton from '../skeletons/ControlPanelSkeleton'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import axios from 'axios'
import FullWidthSearchBar from '../components/FullWidthSearchBar'
import MyPagination from '../components/MyPagination'

export default function ControlPanel(props) {
    const [loading, setLoading] = React.useState(true)
    const [ratings, setRatings] = React.useState([])
    const [currentPage, setCurrentPage] = React.useState(1)
    const [lastPage, setLastPage] = React.useState(true)
    const ratingPerPage = 10


    const search = (query) => {
        props.history.push('/search/' + query)
    }

    useEffect(() => {
        axios.get(baseUrl + "/ratings", {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                n: ratingPerPage,
                page: currentPage
            }
        }).then((data) => {
            if (data.data.success) {
                setRatings(data.data.response.list)
                setLastPage(data.data.response.lastPage)
                setLoading(false)
            } else {
                errorHandler(data.data.code, data.data.message);
            }
        }).catch((error) => httpErrorhandler(error))
    }, [currentPage])

    return (
        <RestrictedPage history={props.history}>
            {
                loading ?
                    <ControlPanelSkeleton />
                    :
                    <React.Fragment>
                        <Typography
                            component="h1"
                            variant="h3" >
                            Admin control panel
                </Typography>
                        <br />
                        <Typography
                            component="h2"
                            variant="h4" >
                            Find user
                </Typography>
                        <br />
                        <FullWidthSearchBar 
                            onSubmit={search}
                        />
                        <br />
                        <Typography
                            component="h2"
                            variant="h4" >
                            Rating history
                        </Typography>
                        <br />
                        <HistoryTable
                            data={ratings}
                            showUser
                            showDelete
                            readOnly
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
        </RestrictedPage>
    )
}