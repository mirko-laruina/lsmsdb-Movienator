import React from 'react';
import { Link } from 'react-router-dom';

/* Graphical components material-ui */
import {
  Card, CardContent, FormControl, OutlinedInput, InputAdornment, Tabs, Tab, IconButton
} from '@material-ui/core';
/* Icon material-ui */
import CameraRollIcon from '@material-ui/icons/CameraRoll';
import DateRangeIcon from '@material-ui/icons/DateRange';
import LanguageIcon from '@material-ui/icons/Language';
import MovieFilterIcon from '@material-ui/icons/MovieFilter';
import PersonIcon from '@material-ui/icons/Person';
import SearchIcon from '@material-ui/icons/Search';

import { makeStyles } from '@material-ui/core/styles';

import './HomePage.css'
import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'

const useStyles = makeStyles(theme => (
  {
    tabsIndicator: {
      visibility: 'hidden',
    },
    tabsButton: {
      opacity: '1',
    },
    paperRoot: {
      color: theme.palette.primary.main,
    },
    iconButton: {
      '&:hover': {
        color: theme.palette.primary.main,
      }
    }
  }));

export default function HomePage() {
  const classes = useStyles()

  return (
    <BasicPage>
      <br />
      <MyCard>
        <br />
        <h1>Movienator</h1>
        <p>The movie search engine you didn't know you needed</p>
      </MyCard>
      <br />
      <MyCard>
        <h2>Search a movie</h2>
        <br />
        <form className={classes.root} noValidate autoComplete="off">
          <FormControl fullWidth variant="outlined">
            <OutlinedInput
              id="search-home"
              placeholder="Search a movie"
              endAdornment={
                <InputAdornment position="end">
                  <IconButton type="submit" className={classes.iconButton} aria-label="search" to='/results' component={Link}>
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
      </MyCard>
      <br />
      <MyCard>
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
          <Tab label="By Director" classes={{ textColorInherit: classes.tabsButton }} icon={<CameraRollIcon />} />
          <Tab label="By Author" classes={{ textColorInherit: classes.tabsButton }} icon={<PersonIcon />} />
          <Tab label="By Country" classes={{ textColorInherit: classes.tabsButton }} icon={<LanguageIcon />} />
          <Tab label="By Year" classes={{ textColorInherit: classes.tabsButton }} icon={<DateRangeIcon />} />
          <Tab label="By Genre" classes={{ textColorInherit: classes.tabsButton }} icon={<MovieFilterIcon />} />
        </Tabs>
      </MyCard>
    </BasicPage>
  );
}