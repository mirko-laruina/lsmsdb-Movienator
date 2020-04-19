import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Paper, Typography
} from '@material-ui/core';

import Skeleton from '@material-ui/lab/Skeleton'

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
        tableRow: {
            '&:nth-child(even)': {
                backgroundColor: theme.palette.divider,
            }
        }
    }
));


export default function MostLikedTable(props) {
    const classes = useStyles();
    return (
        !props.loading ?
            <React.Fragment>
                {
                    !props.data || props.data.length === 0 ?
                        <Typography variant="body1" component="p">
                            No data to display
                        </Typography>
                        :
                        <TableContainer component={Paper}>
                            <Table className={classes.table} aria-label="caption table">
                                <TableHead classes={{ root: classes.tableHead }}>
                                    <TableRow>
                                        <TableCell>{props.subject}</TableCell>
                                        <TableCell align="right">Average Rating</TableCell>
                                        <TableCell align="right">Times rated</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {props.data && props.data.map((row, i) => (
                                        <TableRow key={i} classes={{ root: classes.tableRow }}>
                                            <TableCell component="th" scope="row">
                                                {row.aggregator.name}
                                            </TableCell>
                                            <TableCell align="right">{Math.round(row.avgRating * 100) / 100}</TableCell>
                                            <TableCell align="right">{row.movieCount}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                }
            </React.Fragment>
            :
            <React.Fragment>
                <Skeleton />
                <Skeleton />
                <Skeleton />
            </React.Fragment>
    );
}