import React from 'react'
import { Grid, TextField } from '@material-ui/core'
import Autocomplete from '@material-ui/lab/Autocomplete';

import MySelect from './MySelect.js';

const groups = ["Country", "Year", "Director", "Actor"];
const sorts = ["Count", "Rating", "Alphabetic"];
const sortOrders = ["Ascending", "Descending"];

export default function Grouping() {

    return (
        <Grid container spacing={2}>
            <Grid item xs={3}>
                <MySelect
                    id="group-by-select"
                    label="Group by"
                    options={groups}
                    defaultIndex={0}
                />
            </Grid>
            <Grid item xs={3}>
                <Autocomplete
                    id="sort-by"
                    autoHighlight
                    size="small"
                    options={sorts}
                    renderInput={params => (
                        <TextField {...params} label="Sort by" margin="normal" variant="outlined" />
                    )}
                />
            </Grid>
            <Grid item xs={3}>
                <MySelect
                    id="sort-order-select"
                    label="Sort order"
                    options={sortOrders}
                    defaultIndex={0}
                />

            </Grid>
        </Grid>
    )
}