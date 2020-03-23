import React from 'react'
import MyCard from './MyCard'
import BasicPage from './BasicPage'

import { Typography } from '@material-ui/core'

export default function StatsPage(props) {
    return (
        <BasicPage history={props.history}>
            <MyCard>
                <br />
                <Typography variant="h4">Best film by  {props.match.params.group}</Typography>
            </MyCard>
        </BasicPage>
    )
}