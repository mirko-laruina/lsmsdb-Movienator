import React, { useEffect } from 'react'
import { Pagination } from '@material-ui/lab'
import BasicPage from './BasicPage'
import MyCard from './MyCard'
import HistoryTable from './HistoryTable'
import { baseUrl } from './utils'
import { Typography, Grid } from '@material-ui/core'
import axios from 'axios'

const styles = {
    cardRoot: {
        padding: '1em 3em',
    }
}

export default function HistoryPage(props) {
    const [authorized, setAuthorized] = React.useState(false)
    const [movies, setMovies] = React.useState([]);
    const [pageCount, setPageCount] = React.useState(0);
    const [currentPage, setCurrentPage] = React.useState(1);
    const [loading, setLoading] = React.useState(true);
    const filmPerPage = 10;


    useEffect(() => {
        if (!localStorage.getItem('username')) {
            //not logged in
            return;
        }
        setLoading(true)
        setAuthorized(true)
        let url = baseUrl + "/user/"
            + localStorage.getItem('username')
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
            }
        })
    }, [currentPage])


    return (
        <BasicPage history={props.history}>
            <MyCard style={styles.cardRoot}>
                <Typography variant="h3" component="h1">
                    Rating history
                </Typography>
                <br />
                {
                    !authorized ?
                        <Typography variant="body1" component='p'>
                            You need to be logged in to access this page.
                        </Typography>
                        :
                        !loading &&
                        <React.Fragment>
                            <HistoryTable
                                subject='Movie'
                                data={movies}
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
            </MyCard>
        </BasicPage>
    )
}