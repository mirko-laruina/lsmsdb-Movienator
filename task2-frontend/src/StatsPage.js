import React from 'react'
import BasicPage from './BasicPage'

import {
    Typography, TableContainer, Table, TableHead,
    TableRow, TableBody, TableCell, Paper
} from '@material-ui/core'

import { makeStyles } from '@material-ui/core/styles';

import FilterDisplay from './FilterDisplay'
import Sorting from './Sorting'
import { countryToFlag } from './utils.js';


const data = [
    {
        "id": "IT",
        "name": "ITA",
        "count": 550,
        "rating": 8.87
    },
    {
        "id": "US",
        "name": "USA",
        "count": 500,
        "rating": 8.87
    },
    {
        "id": "US",
        "name": "USA",
        "count": 500,
        "rating": 8.87
    },
    {
        "id": "US",
        "name": "USA",
        "count": 500,
        "rating": 8.87
    },
]

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
        tableRow:{
            '&:nth-child(even)': {
                backgroundColor: theme.palette.divider,
            }
        }
    }
));

export default function StatsPage(props) {
    const classes = useStyles()
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({})

    return (
        <BasicPage history={props.history}>
                <FilterDisplay filters={filters} setFilters={setFilters} />
                <br />
                <Typography variant="h4">Best film by  {props.match.params.group}</Typography>
                <Sorting sortOpt={sortOpt} handler={setSortOpt} />
                <br />
                <TableContainer component={Paper}>
                    <Table aria-label="simple table">
                        <TableHead classes={{ root: classes.tableHead }}>
                            <TableRow>
                                <TableCell>{props.match.params.group.replace(/^./, function (str) { return str.toUpperCase(); })}</TableCell>
                                <TableCell align="right">Count</TableCell>
                                <TableCell align="right">Rating</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {data.map(row => (
                                <TableRow key={row.id} classes={{ root: classes.tableRow }}>
                                    <TableCell component="th" scope="row">
                                    {countryToFlag(row.id)} {row.name}
                                    </TableCell>
                                    <TableCell align="right">{row.count}</TableCell>
                                    <TableCell align="right">{row.rating}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
        </BasicPage>
    )
}