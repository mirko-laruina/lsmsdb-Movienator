import React from 'react'
import { Grid } from '@material-ui/core'

import MySelect from './MySelect.js';

const sortOrders = ["Descending", "Ascending"];

export default function Sorting(props) {
    const groupByIndex = props.options.groupBy
    const sortByIndex = props.options.sortBy
    const sortOrderIndex = props.options.sortOrder

    const handler = (t, v) => {
        let options = Object.assign({}, props.options);
        if(t === "sortOrder" && v === '0'){
            options[t] = -1
        } else {
            options[t] = parseInt(v)
        }
        props.setOpts(options)
    }

    return (
        <Grid container spacing={2}>
            {!props.noGroup &&
                <Grid item xs={3}>
                    <MySelect
                        id="group-by-select"
                        label="Group by"
                        options={props.groups}
                        selectedIndex={groupByIndex}
                        changeHandler={(v) => handler('groupBy', v)}
                    />
                </Grid>
            }
            <Grid item xs={3}>
                <MySelect
                    id="sort-by-select"
                    label="Sort by"
                    options={props.sorts}
                    selectedIndex={sortByIndex}
                    changeHandler={(v) => handler('sortBy', v)}
                    />
            </Grid>
            <Grid item xs={3}>
                <MySelect
                    id="sort-order-select"
                    label="Sort order"
                    options={sortOrders}
                    selectedIndex={sortOrderIndex}
                    changeHandler={(v) => handler('sortOrder', v)}
                    />
            </Grid>
        </Grid>
    )
}