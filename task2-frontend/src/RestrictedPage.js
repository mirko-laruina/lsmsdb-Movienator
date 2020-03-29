import React, { useEffect } from 'react'
import { Typography } from '@material-ui/core'
import BasicPage from './BasicPage'

export default function UserSearchPage(props) {
    const [authorized, setAuthorized] = React.useState(false)

    useEffect(() => {
        if(props.customAuthorization){
            setAuthorized(props.customAuthorization())
            return
        }
        setAuthorized(localStorage.getItem('is_admin') === 'true')
    }, [])

    return (
        <BasicPage history={props.history}>
            {
                !authorized ?
                    <React.Fragment>
                        <br />
                        <Typography
                            variant="h4"
                            component="h1"
                        >
                            Error: unauthorized
                        </Typography>
                        <br />
                        <Typography variant="body1" component="p">
                            You are not authorized to access this page
                        </Typography>
                    </React.Fragment>
                    :
                    props.children
            }
        </BasicPage>
    )
}