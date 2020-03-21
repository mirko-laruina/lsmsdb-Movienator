import React from 'react';

/* Basic imports material-ui */
import { AppBar, Toolbar, Typography, Container, InputBase, CssBaseline, ThemeProvider} from '@material-ui/core';

/* Graphical components material-ui */
import {
  Card, CardContent, FormControl, OutlinedInput, InputAdornment,
  Menu, MenuItem, Tabs, Tab, IconButton
} from '@material-ui/core';
/* Icon material-ui */
import AccountCircle from '@material-ui/icons/AccountCircle';
import CameraRollIcon from '@material-ui/icons/CameraRoll';
import DateRangeIcon from '@material-ui/icons/DateRange';
import LanguageIcon from '@material-ui/icons/Language';
import MovieFilterIcon from '@material-ui/icons/MovieFilter';
import PersonIcon from '@material-ui/icons/Person';
import SearchIcon from '@material-ui/icons/Search';

import { fade, makeStyles, createMuiTheme } from '@material-ui/core/styles';

import './HomePage.css'

const ctheme = createMuiTheme({
  palette: {
    background: {
      default: "#717078"
    },
    primary: {
      main: "#8e1d34",
      contrastText: '#fff',
    },
  },
});

const useStyles = makeStyles(theme => (
  {
  root: {
    flexGrow: 1,
    color: ctheme.palette.primary.main,
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
  tabsRoot: {
    color: ctheme.palette.primary.main,
  },
  tabsIndicator: {
    visibility: 'hidden',
  },
  tabsButton: {
    opacity: '1',
  },
  paperRoot: {
    color: ctheme.palette.primary.main,
  }
}));

export default function SearchAppBar() {
  const classes = useStyles()
  const [auth, setAuth] = React.useState(true);
  const [anchorEl, setAnchorEl] = React.useState(null);
  const open = Boolean(anchorEl);

  const handleChange = event => {
    setAuth(event.target.checked);
  };

  const handleMenu = event => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  return (
    <ThemeProvider theme={ctheme}>
      <CssBaseline />
    <div className={classes.root}>
      <AppBar position="static">
        <Toolbar>
          <Typography className={classes.title} variant="h6" noWrap>
            The movie database
          </Typography>
          <div className={classes.search}>
            <div className={classes.searchIcon}>
              <SearchIcon />
            </div>
            <InputBase
              placeholder="Search…"
              classes={{
                root: classes.inputRoot,
                input: classes.inputInput,
              }}
              inputProps={{ 'aria-label': 'search' }}
            />
          </div>
          {auth && (
            <div>
              <IconButton
                aria-label="account of current user"
                aria-controls="menu-appbar"
                aria-haspopup="true"
                onClick={handleMenu}
                color="inherit"
              >
                <AccountCircle />
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
                open={open}
                onClose={handleClose}
              >
                <MenuItem onClick={handleClose}>Profile</MenuItem>
                <MenuItem onClick={handleClose}>My account</MenuItem>
              </Menu>
            </div>
          )}
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" className="wrapper">
        <br />
        <Card elevation={5} classes={{root: classes.paperRoot}}>
          <CardContent >
            <br />
            <h1>Movienator</h1>
            <p>The movie search engine you didn't know you needed</p>
          </CardContent>
        </Card>
        <br />
        <Card elevation={5} classes={{root: classes.paperRoot}}>
          <CardContent>
            <h2>Search a movie</h2>
            <br />
            <form className={classes.root} noValidate autoComplete="off">
              <FormControl fullWidth variant="outlined">
                <OutlinedInput
                  id="search-home"
                  placeholder="Search a movie"
                  endAdornment={
                    <InputAdornment position="end">
                      <IconButton type="submit" className={classes.iconButton} aria-label="search">
                        <SearchIcon />
                      </IconButton>
                    </InputAdornment>
                  }
                  aria-describedby="outlined-weight-helper-text"
                  inputProps={{
                    'aria-label': 'weight',
                  }}
                  labelWidth={0}
                />
              </FormControl>
            </form>
          </CardContent>
        </Card>
        <br />
        <Card elevation={5} classes={{root: classes.paperRoot}}>
          <CardContent>
            <h2>Explore statistics</h2>
            <br />
            <Tabs
              value={0}
              variant="scrollable"
              scrollButtons="on"
              aria-label="scrollable force tabs example"
              classes={{
                root: classes.tabsRoot,
                indicator: classes.tabsIndicator,
              }}>
              <Tab label="By Director" classes={{textColorInherit: classes.tabsButton}} icon={<CameraRollIcon />} />
              <Tab label="By Author" classes={{textColorInherit: classes.tabsButton}} icon={<PersonIcon />} />
              <Tab label="By Country" classes={{root: classes.tabsButton}} icon={<LanguageIcon />} />
              <Tab label="By Year" classes={{textColorInherit: classes.tabsButton}} icon={<DateRangeIcon />} />
              <Tab label="By Genre" classes={{textColorInherit: classes.tabsButton}} icon={<MovieFilterIcon />} />
            </Tabs>
          </CardContent>
        </Card>
      </Container>
    </div>
    </ThemeProvider>
  );
}