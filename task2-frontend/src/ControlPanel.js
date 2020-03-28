import React, { useEffect } from 'react'
import { Typography, FormControl, OutlinedInput, InputAdornment, IconButton, Grid } from '@material-ui/core'
import Pagination from '@material-ui/lab/Pagination'
import SearchIcon from '@material-ui/icons/Search'
import BasicPage from './BasicPage'
import MyCard from './MyCard'
import HistoryTable from './HistoryTable'
import { baseUrl } from './utils'
import axios from 'axios'

const styles = {
    cardRoot: {
        padding: '1em 3em',
    }
}

export default function ControlPanel(props) {
    const [searchValue, setSearch] = React.useState("")
    const [ratings, setRatings] = React.useState([])
    const [currentPage, setCurrentPage] = React.useState(1)
    const [pageCount, setPageCount] = React.useState(0)
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
                setPageCount(Math.ceil(parseInt(data.data.response.totalCount) / ratingPerPage))
            }
        })
    }, [currentPage])

    return (
        <BasicPage history={props.history}>
            <MyCard style={styles.cardRoot}>
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
            <br />
        </BasicPage>
    )
}