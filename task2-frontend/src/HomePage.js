import React from 'react';
import { Link } from 'react-router-dom';

/* Graphical components material-ui */
import {
  FormControl, OutlinedInput, InputAdornment, Tabs, Tab, IconButton, Typography
} from '@material-ui/core';
/* Icon material-ui */
import CameraRollIcon from '@material-ui/icons/CameraRoll';
import DateRangeIcon from '@material-ui/icons/DateRange';
import LanguageIcon from '@material-ui/icons/Language';
import MovieFilterIcon from '@material-ui/icons/MovieFilter';
import PersonIcon from '@material-ui/icons/Person';
import SearchIcon from '@material-ui/icons/Search';

import { makeStyles, withStyles } from '@material-ui/core/styles';

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'

const useStyles = makeStyles(theme => (
  {
    tabsIndicator: {
      visibility: 'hidden',
    },
    iconButton: {
      '&:hover': {
        color: theme.palette.primary.main,
      }
    }
  }));

const MyTab = withStyles({
  textColorInherit: {
    opacity: '1',
  },
})(Tab)

export default function HomePage(props) {
  const classes = useStyles()
  const [searchValue, setSearch] = React.useState("")
  const backUrl = '/results/'
  return (
    <BasicPage history={props.history}>
      <MyCard>
        <br />
        <Typography
          variant='h2'
          align="center">Movienator</Typography>
        <Typography
          variant='body1'
          align="center">
          The movie search engine you didn't know you needed
          </Typography>
      </MyCard>
      <MyCard>
        <h2>Search a movie</h2>
        <br />
        <form onSubmit={() => { props.history.push('/results/'+searchValue) }}>
          <FormControl fullWidth variant="outlined">
            <OutlinedInput
              id="search-home"
              placeholder="Search a movie"
              value={searchValue}
              onChange={(e) => setSearch(e.target.value)}
              endAdornment={
                <InputAdornment position="end">
                  <IconButton
                    type="submit"
                    className={classes.iconButton}
                    aria-label="search"
                    >
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
          <MyTab label="By Director" icon={<CameraRollIcon />} />
          <MyTab label="By Author" icon={<PersonIcon />} />
          <MyTab label="By Country" icon={<LanguageIcon />} />
          <MyTab label="By Year" icon={<DateRangeIcon />} />
          <MyTab label="By Genre" icon={<MovieFilterIcon />} />
        </Tabs>
      </MyCard>
    </BasicPage>
  );
}