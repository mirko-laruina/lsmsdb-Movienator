import React, { useEffect } from 'react'
import { Typography, FormControl, OutlinedInput, InputAdornment, IconButton, Grid } from '@material-ui/core'
import Pagination from '@material-ui/lab/Pagination'
import SearchIcon from '@material-ui/icons/Search'
import RestrictedPage from './RestrictedPage'
import HistoryTable from '../components/HistoryTable'
import ControlPanelSkeleton from '../skeletons/ControlPanelSkeleton'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import axios from 'axios'
import MyPagination from '../components/MyPagination'

export default function ControlPanel(props) {
    const [loading, setLoading] = React.useState(true)
    const [searchValue, setSearch] = React.useState("")
    const [ratings, setRatings] = React.useState([])
    const [currentPage, setCurrentPage] = React.useState(1)
    const [lastPage, setLastPage] = React.useState(true)
    const ratingPerPage = 10

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
                        <form onSubmit={() => { props.history.push('/admin/search/' + searchValue) }}>
                            <FormControl fullWidth variant="outlined">
                                <OutlinedInput
                                    id="search-user"
                                    placeholder="Search a user"
                                    value={searchValue}
                                    onChange={(e) => setSearch(e.target.value)}
                                    endAdornment={
                                        <InputAdornment position="end">
                                            <IconButton
                                                type="submit"
                                                aria-label="search"
                                            >
                                                <SearchIcon />
                                            </IconButton>
                                        </InputAdornment>
                                    }
                                    inputProps={{
                                        'aria-label': 'weight',
                                    }}
                                    labelWidth={0}
                                />
                            </FormControl>
                        </form>
                        <br />
                        <Typography
                            component="h2"
                            variant="h4" >
                            Rating history
                        </Typography>
                        <br />
                        <HistoryTable
                            data={ratings}
                            adminView
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