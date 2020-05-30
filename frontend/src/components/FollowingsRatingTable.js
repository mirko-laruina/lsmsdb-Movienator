import React, { useEffect } from 'react'
import axios from 'axios'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import HistoryTable from './HistoryTable'
import { Grid, Box } from '@material-ui/core'
import MyPagination from './MyPagination'

export default function FollowingsRatingTable(props) {
    const ratingsPerPage = 10
    const [currentPage, setCurrentPage] = React.useState(1)
    const [lastPage, setLastPage] = React.useState(true)
    const [ratings, setRatings] = React.useState([])

    const getRatings = () => {
        axios.get(baseUrl + "/ratings/following", {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                n: ratingsPerPage,
                page: currentPage
            }
        }).then((data) => {
            if (data.data.success) {
                setRatings(data.data.response.list)
                setLastPage(data.data.response.lastPage)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    useEffect(() => {
        getRatings()
    }, [])

    return (
        <React.Fragment>
            <HistoryTable
                data={ratings}
                readOnly
                showUser
            />
            <Grid container justify="center">
                <Box width={1} my={3}>
                <MyPagination
                    currentPage={currentPage}
                    onClick={(v) => setCurrentPage(v)}
                    lastPage={lastPage}
                />
                </Box>
            </Grid>
        </React.Fragment>
    )
}