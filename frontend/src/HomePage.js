import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';

/* Graphical components material-ui */
import {
  FormControl, OutlinedInput, InputAdornment, Tabs, Tab, IconButton, Typography, Button, Grid
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
import SuggestedMovies from './SuggestedMovies'


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

  return (
    <BasicPage noCard history={props.history}>
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
        <Grid container>
          <Grid item xs={9}>
            <Typography variant="h4" component='h2'>Search a movie</Typography>
          </Grid>
          <Grid item xs={3}>
            <Button
              fullWidth
              variant="outlined"
              color="primary"
              size="large"
              to="/browse"
              component={Link}>
              Browse all</Button>
          </Grid>
        </Grid>
        <br />
        <form onSubmit={() => { props.history.push('/results/' + searchValue) }}>
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
              inputProps={{
                'aria-label': 'weight',
              }}
              labelWidth={0}
            />
          </FormControl>
        </form>
        <br />
      </MyCard>
      <MyCard>
        <Typography variant="h4" component="h2">Some movies you could like</Typography>
        <br />
        <SuggestedMovies />
      </MyCard>
      <MyCard>
        <Typography variant="h4" component='h2'>Explore movie statistics</Typography>
        <br />
        <Tabs
          value={0}
          variant="fullWidth"
          aria-label="Explore movie statistics"
          classes={{
            root: classes.tabsRoot,
            indicator: classes.tabsIndicator,
          }}>
          <MyTab component={Link} to="/stats/director" label="By Director" icon={<CameraRollIcon />} />
          <MyTab component={Link} to="/stats/actor" label="By Actor" icon={<PersonIcon />} />
          <MyTab component={Link} to="/stats/country" label="By Country" icon={<LanguageIcon />} />
          <MyTab component={Link} to="/stats/year" label="By Year" icon={<DateRangeIcon />} />
          <MyTab component={Link} to="/stats/genre" label="By Genre" icon={<MovieFilterIcon />} />
        </Tabs>
      </MyCard>
    </BasicPage>
  );
}