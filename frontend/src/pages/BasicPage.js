import React from 'react';
import { Link } from 'react-router-dom';

/* Basic imports material-ui */
import { AppBar, Toolbar, Typography, Container, InputBase, FormControl, Slide, Dialog, Avatar } from '@material-ui/core';

/* Graphical components material-ui */
import { Menu, MenuItem, IconButton, Button } from '@material-ui/core';
/* Icon material-ui */
import SearchIcon from '@material-ui/icons/Search';
import Gravatar from 'react-gravatar'
import { fade, makeStyles } from '@material-ui/core/styles';

import MyCard from '../components/MyCard'
import LoginForm from '../components/LoginForm'
import '../assets/App.css'
import { baseUrl, httpErrorhandler } from '../utils'
import axios from 'axios';

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="down" ref={ref} {...props} />;
});

const useStyles = makeStyles(theme => (
    {
        root: {
            flexGrow: 1,
            color: theme.palette.primary.main,
        },
        menuButton: {
            marginRight: theme.spacing(2),
        },
        title: {
            flexGrow: 1,
            display: 'none',
            fontSize: '22px',
            [theme.breakpoints.up('sm')]: {
                display: 'block',
            },
        },
        search: {
            position: 'relative',
            borderRadius: theme.shape.borderRadius,
            backgroundColor: fade(theme.palette.common.white, 0.15),
            '&:hover': {
                backgroundColor: fade(theme.palette.common.white, 0.25),
            },
            marginLeft: 0,
            width: '100%',
            [theme.breakpoints.up('sm')]: {
                marginLeft: theme.spacing(1),
                width: 'auto',
            },
        },
        searchIcon: {
            padding: theme.spacing(0, 2),
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
        },
        inputRoot: {
            color: 'inherit',
        },
        inputInput: {
            padding: theme.spacing(1, 1, 1, 0),
            // vertical padding + font size from searchIcon
            paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
            transition: theme.transitions.create('width'),
            width: '100%',
            [theme.breakpoints.up('sm')]: {
                width: '12ch',
                '&:focus': {
                    width: '20ch',
                },
            },
        },
        cardRoot: {
            padding: '1em 3em',
        },
        browseButton: {
            color: theme.palette.primary.main,
            marginLeft: '1em',
            padding: '0.35em 1.5em'
        },
        avatarSmall: {
            width: theme.spacing(4),
            height: theme.spacing(4),
        },
    }));

export default function BasicPage(props) {
    const classes = useStyles()
    const [searchValue, setSearchValue] = React.useState("");
    const [openMenu, setOpenMenu] = React.useState(false);
    const [openLogin, setOpenLogin] = React.useState(false);
    const [anchorEl, setAnchorEl] = React.useState(null);
    const [isRegistering, setIsReg] = React.useState(false);
    const [username, setUsername] = React.useState("");

    const loginDialogHandler = (isRegistering) => {
        setOpenMenu(false);
        setOpenLogin(true);
        setIsReg(isRegistering);
    }

    const profilePopupHandler = (evt, state) => {
        if (evt)
            setAnchorEl(evt.target);
        setOpenMenu(state);
    }

    const logoutRequest = () => {
        let sid = localStorage.getItem('sessionId');
        axios.post(baseUrl + "auth/logout", null, {
            params: {
                sessionId: sid
            }
        }).catch((error) => httpErrorhandler(error))
        // Since the user wants to be disconnected
        // we can remove the infos anyway
        localStorage.removeItem('sessionId');
        localStorage.removeItem('username');
        localStorage.removeItem('is_admin');
        window.location.reload()
    }

    const notLoggedMenu = [
        {
            label: 'Login',
            handler: () => loginDialogHandler(false)
        },
        {
            label: 'Register',
            handler: () => loginDialogHandler(true)
        }
    ]
    const loggedMenu = [
        {
            label: 'Logged as ' + username,
            disabled: true
        },
        {
            label: 'Profile',
            handler: () => {
                props.history.push('/profile')
            }
        },
        {
            label: 'Social profile',
            handler: () => {
                props.history.push('/social')
            }

        },
        {
            label: 'Search user',
            handler: () => {
                props.history.push('/search')
            }
        },
        {
            label: 'Rating history',
            handler: () => {
                props.history.push('/history')
            }
        },
        {
            label: 'Logout',
            handler: () => {
                logoutRequest()
            }
        }
    ]

    React.useEffect(() => {
        setUsername(window.localStorage.getItem('username'))
    }, [openLogin, openMenu])

    return (
        <div className={classes.root}>
            <AppBar position="static">
                <Toolbar>
                    <Typography className={classes.title} variant="h6" noWrap to="/" component={Link}>
                        Movienator
                    </Typography>

                    <div className={classes.search}>
                        <div className={classes.searchIcon}>
                            <SearchIcon />
                        </div>
                        <form onSubmit={() => { props.history.push('/results/' + searchValue) }}>
                            <FormControl >
                                <InputBase
                                    placeholder="Search…"
                                    classes={{
                                        root: classes.inputRoot,
                                        input: classes.inputInput,
                                    }}
                                    value={searchValue}
                                    onChange={(e) => setSearchValue(e.target.value)}

                                    inputProps={{ 'aria-label': 'search' }}
                                />
                            </FormControl>
                        </form>
                    </div>
                    <div>
                        <Button
                            variant="contained"
                            className={classes.browseButton}
                            size="medium"
                            to="/browse"
                            component={Link}>
                            Browse all
                        </Button>
                    </div>
                    <div>
                        <IconButton
                            aria-label="account of current user"
                            aria-controls="menu-appbar"
                            aria-haspopup="true"
                            onClick={(evt) => profilePopupHandler(evt, true)}
                            color="inherit"
                        >
                            <Avatar className={classes.avatarSmall} >
                                <Gravatar default="identicon" email={username} />
                            </Avatar>
                        </IconButton>
                        <Menu
                            id="menu-appbar"
                            anchorEl={anchorEl}
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={openMenu}
                            onClose={() => profilePopupHandler(null, false)}
                        >
                            {
                                (!username ? notLoggedMenu : loggedMenu).map((item, i) => {
                                    return (
                                        <MenuItem
                                            key={i}
                                            disabled={item.disabled}
                                            onClick={() => {
                                                setOpenMenu(false)
                                                item.handler()
                                            }}
                                        >
                                            {item.label}
                                        </MenuItem>
                                    )
                                })
                            }
                            {
                                (username && localStorage.getItem('is_admin') === 'true' &&
                                    <MenuItem
                                        onClick={() => {
                                            props.history.push('/admin')
                                        }}
                                    >
                                        Admin control panel
                                    </MenuItem>
                                )
                            }
                        </Menu>
                        <Dialog
                            TransitionComponent={Transition}
                            open={openLogin}
                            PaperComponent={MyCard}
                            fullWidth={true}
                            maxWidth={'xs'}
                            onClose={() => setOpenLogin(false)}
                        >
                            <LoginForm
                                isRegistering={isRegistering}
                                setOpen={setOpenLogin} />
                        </Dialog>
                    </div>
                </Toolbar>
            </AppBar>
            <Container maxWidth="md" className="wrapper">
                {props.noCard ?
                    props.children
                    :
                    <MyCard className={classes.cardRoot}>
                        {props.children}
                    </MyCard>
                }
                <br />
            </Container>
        </div>
    );
}