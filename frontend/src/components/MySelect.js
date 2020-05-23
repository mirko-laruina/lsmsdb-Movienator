import React from 'react'
import {FormControl, InputLabel, Select } from '@material-ui/core'

export default function MySelect(props) {

    return (
        <FormControl fullWidth variant="outlined" size="small" margin="normal">
            <InputLabel htmlFor={props.id}>{props.label}</InputLabel>
            <Select
                native
                value={props.selectedIndex}
                onChange={(evt) => props.changeHandler(evt.target.value)}
                label={props.label}
                inputProps={{
                    name: 'sort-order',
                    id: props.id,
                }}
            >
                {props.options.map((order, i) => <option key={i} value={i}>{order}</option>)}
            </Select>
        </FormControl>
    )
}