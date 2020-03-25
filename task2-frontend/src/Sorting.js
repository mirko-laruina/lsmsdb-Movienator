import React, { useEffect } from 'react'
import { Grid } from '@material-ui/core'

import MySelect from './MySelect.js';

const groups = ["Country", "Year", "Director", "Actor"];
const sorts = ["Release", "Rating", "Title"];
const sortOrders = ["Ascending", "Descending"];


export default function Sorting(props) {
    const [sortByValue, setSortBy] = React.useState(0);
    const [groupByValue, setGroupBy] = React.useState(0);
    const [sortOrderValue, setSortOrder] = React.useState(0);
    const handler = props.handler;

    useEffect(() => {
        handler({
            sortBy: sortByValue,
            groupBy: groupByValue,
            sortOrder: sortOrderValue,
        })
    }, [sortByValue, groupByValue, sortOrderValue, handler])

    return (
        <Grid container spacing={2}>
            {!props.noGroup &&
                <Grid item xs={3}>
                    <MySelect
                        id="group-by-select"
                        label="Group by"
                        options={groups}
                        selectedIndex={groupByValue}
                        changeHandler={setGroupBy}
                    />
                </Grid>
            }
            <Grid item xs={3}>
                <MySelect
                    id="sort-by-select"
                    label="Sort by"
                    options={sorts}
                    selectedIndex={sortByValue}
                    changeHandler={setSortBy}
                />
            </Grid>
            <Grid item xs={3}>
                <MySelect
                    id="sort-order-select"
                    label="Sort order"
                    options={sortOrders}
                    selectedIndex={sortOrderValue}
                    changeHandler={setSortOrder}
                />
            </Grid>
        </Grid>
    )
}