import React from 'react'
import { Button, Grid, Typography } from '@material-ui/core'
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import { Pagination } from '@material-ui/lab';

export default function MyPagination(props) {


    const PaginationButton = (props) => {
        return (
            <Button
                fullWidth
                size="large"
                color="primary"
                {...props}
            />
        )
    }

    return (
        <Grid container justify="center" alignItems="center" spacing={4}>
            <Grid item xs={4}>
                <PaginationButton
                    disabled={props.currentPage === 1}
                    variant={ props.noBorder ? "text" : "outlined" }
                    onClick={() => {
                        window.scroll(0, 0)
                        props.onClick(props.currentPage - 1)
                    }}>
                    <ChevronLeftIcon />
                    {props.noText ? "" : "Previous page"}
                </PaginationButton>
            </Grid>
            <Grid item xs={2}>
                <Typography variant="body1" component="p" align="center">
                    {props.currentPage}
                </Typography>
            </Grid>
            <Grid item xs={4}>
                <PaginationButton
                    disabled={props.lastPage}
                    variant={ props.noBorder ? "text" : "outlined" }
                    onClick={() => {
                        window.scroll(0, 0)
                        props.onClick(props.currentPage + 1)
                    }} >
                    {props.noText ? "" : "Next page"}
                    <ChevronRightIcon />
                </PaginationButton>
            </Grid>
        </Grid>
    )
}