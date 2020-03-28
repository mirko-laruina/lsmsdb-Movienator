import React, { useEffect } from 'react'
import { makeStyles } from '@material-ui/core/styles';

import BasicPage from './BasicPage'
import { baseUrl } from './utils'
import { Typography } from '@material-ui/core'
import MostLikedTable from './MostLikedTable'
import axios from 'axios'

const useStyles = makeStyles((theme) => (
    {
        tableHead: {
            backgroundColor: theme.palette.primary.light,
            color: 'white',
            '& th': {
                color: 'white',
                fontWeight: 'bold'
            }
        },
        tableRow: {
            '&:nth-child(even)': {
                backgroundColor: theme.palette.divider,
            }
        }
    }
));


export default function ProfilePage(props) {
    const [infos, setInfos] = React.useState({})
    const [authorized, setAuthorized] = React.useState(false)

    useEffect(() => {
        if (!localStorage.getItem('username')) {
            //not logged in
            return;
        }
        setAuthorized(true)
        let url = baseUrl + "/user/" + localStorage.getItem('username')
        axios.get(url, {
            params: {
                sessionId: localStorage.getItem('sessionId')
            }
        }).then((data) => {
            if (data.data.success) {
                console.log(data.data.response)
                setInfos(data.data.response)
            }
        })
    }, [])

    return (
        <BasicPage history={props.history}>
            <Typography variant="h3" component="h1">
                Profile page
                </Typography>
            <br />
            {
                !authorized ?
                    <Typography variant="body1" component='p'>
                        You need to be logged in to access this page.
                        </Typography>
                    :
                    <React.Fragment>
                        <Typography variant="h4" component='h2'>
                            Account details
                            </Typography>
                        <br />
                        <Typography variant="h5" component='p'>
                            Username: {infos.username}
                        </Typography>
                        <Typography variant="h5" component='p'>
                            Email: {infos.email}
                        </Typography>
                        <br />
                        <Typography variant="h4" component='h2'>
                            The things you like
                            </Typography>
                        <br />
                        <Typography variant="h5" component='h3'>
                            Actors
                            </Typography>
                        <br />
                        <MostLikedTable
                            subject='Actor'
                            data={infos.favouriteActors}
                        />
                        <br />
                        <Typography variant="h5" component='h3'>
                            Directors
                            </Typography>
                        <br />
                        <MostLikedTable
                            subject='Director'
                            data={infos.favouriteDirectors}
                        />
                        <br />
                        <Typography variant="h5" component='h3'>
                            Genres
                            </Typography>
                        <br />
                        <MostLikedTable
                            subject='Genre'
                            data={infos.favouriteGenres}
                        />
                    </React.Fragment>
            }
        </BasicPage>
    )
}