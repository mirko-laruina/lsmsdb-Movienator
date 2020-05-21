import React from 'react';
import { Link } from 'react-router-dom'
import { makeStyles } from '@material-ui/core/styles';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Paper, Typography
} from '@material-ui/core';
import { getDate } from './utils'

import UserRating from './UserRating'


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


export default function HistoryTable(props) {
    const classes = useStyles();

    return (
        !props.data || props.data.length === 0 ?
            <Typography variant="body1" component="p">
                No data to display
            </Typography>
            :
            <TableContainer component={Paper}>
                <Table className={classes.table} aria-label="caption table">
                    <TableHead classes={{ root: classes.tableHead }}>
                        <TableRow>
                            {props.adminView &&
                                <TableCell>Username</TableCell>
                            }
                            <TableCell>Movie</TableCell>
                            <TableCell align="center">Year</TableCell>
                            <TableCell align="center">Rating</TableCell>
                            <TableCell align="center">Rated on</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {props.data && props.data.map((row, i) => {
                            return (
                                <TableRow key={i} classes={{ root: classes.tableRow }}>
                                    {
                                        props.adminView &&
                                        <TableCell component='th' scope="row">
                                            <Link to={"/profile/" + row.username}>
                                                <Typography variant="body1" color="primary">
                                                    {row.username}
                                                </Typography>
                                            </Link>
                                        </TableCell>
                                    }
                                    <TableCell align="left">
                                        <Link to={"/movie/" + row.movieId}>
                                            <Typography variant="body1" color="primary">
                                                {row.title}
                                            </Typography>
                                        </Link>
                                    </TableCell>
                                    <TableCell align="center">{row.year}</TableCell>
                                    <TableCell align="center">
                                        <UserRating
                                            showDelete
                                            readOnly={props.readOnly}
                                            rating={row.rating}
                                            user={props.adminView ? row.username : false}
                                            movieId={row.movieId}
                                        />
                                    </TableCell>
                                    <TableCell align="center">{getDate(row.date)}</TableCell>
                                </TableRow>
                            )
                        }
                        )}
                    </TableBody>
                </Table>
            </TableContainer>
    );
}