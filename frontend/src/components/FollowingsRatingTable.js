import React, { useEffect } from 'react'
import axios from 'axios'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import HistoryTable from './HistoryTable'
import { Pagination } from '@material-ui/lab'
import { Grid } from '@material-ui/core'

export default function FollowingsRatingTable(props) {
    const ratingsPerPage = 10
    const [currentPage, setCurrentPage] = React.useState(1)
    const [pageCount, setPageCount] = React.useState(0)
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
                setPageCount(Math.ceil(parseInt(data.data.response.totalCount) / ratingsPerPage))
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
    )
}