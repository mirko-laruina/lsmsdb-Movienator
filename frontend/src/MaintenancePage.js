import React from 'react'
import BasicPage from "./BasicPage";
import MyCard from "./MyCard";
import { Typography } from "@material-ui/core";

export default function MaintenancePage() {
    return (
        <BasicPage>
            <Typography variant="h3" component="h1">
                Maintenance
            </Typography>
            <Typography variant="body1" component="p">
                We are currently under maintenance. Please, come back later.
            </Typography>
        </BasicPage>
    )
}