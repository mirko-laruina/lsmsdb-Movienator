import React, { useEffect } from 'react'
import BasicPage from './BasicPage'

import {
    Typography, TableContainer, Table, TableHead,
    TableRow, TableBody, TableCell, Paper
} from '@material-ui/core'

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
    const [isCorrectAggr, setCorrectAggr] = React.useState(true);
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({});
    const [data, setData] = React.useState([]);

    const sorts = ["Rating", "Count", "Alphabetic"];


    useEffect(() => {
        let options = Object.assign({}, sortOpt);
        options.groupBy = aggregation_fields.findIndex((e) => {
            return e.toLowerCase() == props.match.params.group.toLowerCase()
        })
        setSortOpt(options)
    }, [props.match.params.group])

    useEffect(() => {
        if (typeof (sortOpt.groupBy) !== 'undefined'
            &&
            props.match.params.group.toLowerCase() != aggregation_fields[sortOpt.groupBy].toLowerCase()) {
            props.history.push('/stats/' + aggregation_fields[sortOpt.groupBy].toLowerCase())
        } else if(typeof(sortOpt.groupBy) !== 'undefined'){
            axios.get(baseUrl + "movie/statistics", {
                params: {
                    groupBy: typeof (sortOpt.groupBy) !== 'undefined' ? aggregation_fields[sortOpt.groupBy].toLowerCase() : undefined,
                    sortBy: typeof (sortOpt.sortBy) !== 'undefined' ? sorts[sortOpt.sortBy].toLowerCase() : undefined,
                    sortOrder: sortOpt.sortOrder,
                    ...filters
                }
            })
                .then(function (res) {
                    console.log(res.data)
                    if (res.data.success) {
                        setData(res.data.response.list)
                    }
                })
        }
    }, [sortOpt, filters])

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
                            <Table aria-label="simple table">
                                <TableHead classes={{ root: classes.tableHead }}>
                                    <TableRow>
                                        <TableCell>{props.match.params.group.replace(/^./, function (str) { return str.toUpperCase(); })}</TableCell>
                                        <TableCell style={{width: '30%'}} align="right">Count</TableCell>
                                        <TableCell style={{width: '30%'}} align="right">Rating</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {data.map((row, i) => (
                                        <TableRow key={i} classes={{ root: classes.tableRow }}>
                                            <TableCell component="th" scope="row">
                                                {row.aggregator.name || row.aggregator.year || row.aggregator.id}
                                            </TableCell>
                                            <TableCell align="right">{row.movieCount}</TableCell>
                                            <TableCell align="right">{parseFloat(row.avgRating).toFixed(2)}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
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