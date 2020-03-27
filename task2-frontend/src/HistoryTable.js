import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import {getDate} from './utils'

const useStyles = makeStyles((theme) => (
    {
        tableHead: {
            backgroundColor: theme.palette.primary.light,
            color: 'white',
            '& th': {
                color: 'white',
                fontWeight: 'bold'
            },
        },
        tableRow:{
            '&:nth-child(even)': {
                backgroundColor: theme.palette.divider,
            }
        }
    }
));


export default function HistoryTable(props) {
    const classes = useStyles();

    return (
        <TableContainer component={Paper}>
            <Table className={classes.table} aria-label="caption table">
                <TableHead classes={{ root: classes.tableHead }}>
                    <TableRow>
                        <TableCell>{props.subject}</TableCell>
                        <TableCell align="right">Year</TableCell>
                        <TableCell align="right">Rated on</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {props.data && props.data.map((row, i) => (
                        <TableRow key={i} classes={{ root: classes.tableRow }}>
                            <TableCell component="th" scope="row">
                                {row.title}
                            </TableCell>
                            <TableCell align="right">{row.year}</TableCell>
                            <TableCell align="right">{getDate(row.date)}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}