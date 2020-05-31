import React from 'react'
import BasicPage from "./BasicPage";
import { Typography } from "@material-ui/core";

export default function MaintenancePage() {
    return (
        <BasicPage>
            <Typography variant="h3" component="h1">
                Maintenance
            </Typography>
            <br />
            <Typography variant="body1" component="p">
                Something went wrong. Our monkeys are working hard to fix Movienator.
                Please, come back later.
            </Typography>
            <br />
            <Typography variant="body1" component="p">
                If you still have issues, contact us.
            </Typography>
        </BasicPage>
    )
}