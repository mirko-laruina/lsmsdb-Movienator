import React, {useEffect} from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import './App.css';
import { CssBaseline, ThemeProvider } from '@material-ui/core';
import { createMuiTheme } from '@material-ui/core/styles';

import HomePage from './HomePage.js'
import SearchPage from './SearchPage.js'
import StatsPage from './StatsPage.js'
import BrowsePage from './BrowsePage'
import MoviePage from './MoviePage'
import ProfilePage from './ProfilePage'
import HistoryPage from './HistoryPage'
import ControlPanel from './ControlPanel'
import UserSearchPage from './UserSearchPage'
import SocialPage from './SocialPage'

const theme = createMuiTheme({
  palette: {
    background: {
      default: "#717078"
    },
    primary: {
      main: "#8e1d34",
      contrastText: '#fff',
      light: "#b72e4a"
    },
    secondary: {
      main: "#717078",
    },
    info: {
      main: "#8e1d34",
    },
  },
  typography: {
    fontFamily: [
      'Noto Sans',
      'Ubuntu',
      'Roboto',
      'Arial'
    ].join(','),
  }
});

function App() {
  useEffect(() => {
    document.title = "Movienator"
  }, [])
  
  //NOTICE: root route has to be the last route, otherwise it will match all the paths
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Switch>
          <Route path="/browse" component={BrowsePage} />
          <Route path="/profile/:username" component={ProfilePage} />
          <Route path="/profile" component={ProfilePage} />
          <Route path="/social/:username" component={SocialPage} />
          <Route path="/social" component={SocialPage} />
          <Route path="/history/:username" component={HistoryPage} />
          <Route path="/history" component={HistoryPage} />
          <Route path="/admin/search/:query" component={UserSearchPage} />
          <Route path="/admin" component={ControlPanel} />
          <Route path="/stats/:group/" component={StatsPage} />
          <Route path="/results/:query" component={SearchPage} />
          <Route path="/movie/:id" component={MoviePage} />
          <Route path="/" component={HomePage} />
        </Switch>
      </Router>
    </ThemeProvider>
  );
}

export default App;
