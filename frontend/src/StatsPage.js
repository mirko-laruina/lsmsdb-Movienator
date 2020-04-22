import React, { useEffect } from 'react'
import BasicPage from './BasicPage'

import {
    Typography, TableContainer, Table, TableHead,
    TableRow, TableBody, TableCell, Paper, Grid
} from '@material-ui/core'
import { Pagination, Skeleton } from '@material-ui/lab'

import { makeStyles } from '@material-ui/core/styles';

import FilterDisplay from './FilterDisplay'
import Sorting from './Sorting'
import { aggregation_fields, baseUrl } from './utils.js';

import axios from 'axios'

const useStyles = makeStyles((theme) => (
    {
        tableHead: {
            backgroundColor: theme.palette.primary.light,
            color: 'white',
            '& th': {
                color: 'white',
                fontWeight: 'bold'
            }
        },
        tableRow: {
            '&:nth-child(even)': {
                backgroundColor: theme.palette.divider,
            }
        }
    }
));

export default function StatsPage(props) {
    const classes = useStyles()
    const [isCorrectAggr, setCorrectAggr] = React.useState(false);
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({});
    const [data, setData] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [currentPage, setCurrentPage] = React.useState(1);
    const [pageCount, setPageCount] = React.useState(1);

    const sorts = ["Rating", "Count", "Alphabetic"];
    const filmPerPage = 10;


    useEffect(() => {
        let aggr_index = aggregation_fields.findIndex((e) => {
            return e.toLowerCase() === props.match.params.group.toLowerCase()
        });
        if(aggr_index > -1){
            let options = Object.assign({}, sortOpt);
            options.groupBy = aggr_index
            setSortOpt(options)
            setCorrectAggr(true)
        }
    }, [props.match.params.group])

    useEffect(() => {
        setLoading(true)
        if (typeof (sortOpt.groupBy) !== 'undefined'
            &&
            props.match.params.group.toLowerCase() !== aggregation_fields[sortOpt.groupBy].toLowerCase()) {
            props.history.push('/stats/' + aggregation_fields[sortOpt.groupBy].toLowerCase())
        } else if (typeof (sortOpt.groupBy) !== 'undefined') {
            axios.get(baseUrl + "movie/statistics", {
                params: {
                    groupBy: typeof (sortOpt.groupBy) !== 'undefined' ? aggregation_fields[sortOpt.groupBy].toLowerCase() : undefined,
                    sortBy: typeof (sortOpt.sortBy) !== 'undefined' ? sorts[sortOpt.sortBy].toLowerCase() : undefined,
                    sortOrder: sortOpt.sortOrder,
                    ...filters,
                    page: currentPage,
                    n: filmPerPage
                }
            })
                .then(function (res) {
                    if (res.data.success) {
                        setData(res.data.response.list)
                        if (res.data.response.list.length === filmPerPage) {
                            setPageCount(currentPage + 1)
                        }
                        setLoading(false)
                    }
                })
        }
    }, [sortOpt, filters, currentPage])

    return (
        <BasicPage history={props.history}>
            {
                isCorrectAggr ?
                    <>
                        <FilterDisplay filters={filters} setFilters={setFilters} />
                        <br />
                        <Typography variant="h4">Best film by  {props.match.params.group}</Typography>
                        <Sorting
                            groups={aggregation_fields}
                            sorts={sorts}
                            options={sortOpt}
                            setOpts={setSortOpt}
                        />
                        <br />
                        <TableContainer component={Paper}>
                            {!loading ?
                                <Table aria-label="simple table">
                                    <TableHead classes={{ root: classes.tableHead }}>
                                        <TableRow>
                                            <TableCell>{props.match.params.group.replace(/^./, function (str) { return str.toUpperCase(); })}</TableCell>
                                            <TableCell style={{ width: '30%' }} align="right">Count</TableCell>
                                            <TableCell style={{ width: '30%' }} align="right">Rating</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {
                                            data.map((row, i) => (
                                                <TableRow key={i} classes={{ root: classes.tableRow }}>
                                                    <TableCell component="th" scope="row">
                                                        {row.aggregator.name || row.aggregator.year || row.aggregator.id}
                                                    </TableCell>
                                                    <TableCell align="right">{row.movieCount}</TableCell>
                                                    <TableCell align="right">{parseFloat(row.avgRating).toFixed(2)}</TableCell>
                                                </TableRow>
                                            ))

                                        }
                                    </TableBody>
                                </Table>
                                :
                                <>
                                { [...Array(filmPerPage + 1)].map((e, i) => <Skeleton key={i} style={{margin: '0 5%'}} height={50} />) }
                                <br />
                                </>
                            }
                        </TableContainer>
                        <br />
                        {!loading ? 
                        <Grid container justify="center">
                            <Pagination shape="rounded"
                                color="primary"
                                size="large"
                                count={pageCount}
                                page={currentPage}
                                siblingCount={0}
                                boundaryCount={1}
                                onChange={(e, v) => {
                                    setCurrentPage(v)
                                }}
                            />
                        </Grid>  
                        :
                            <Skeleton style={{margin: '0 30%'}} height={70} />
                        }
                        <br />
                    </>
                    :
                    <>
                        <Typography variant="h4">Error</Typography>
                        <br />
                        <Typography variant="body1">Statistics for "{props.match.params.group}" are not available</Typography>
                    </>
            }
        </BasicPage>
    )
}