import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import './App.css';
import { CssBaseline, ThemeProvider } from '@material-ui/core';
import { createMuiTheme } from '@material-ui/core/styles';

import HomePage from './HomePage.js'
import ResultsPage from './ResultsPage.js'
import StatsPage from './StatsPage.js'

const theme = createMuiTheme({
  palette: {
    background: {
      default: "#717078"
    },
    primary: {
      main: "#8e1d34",
      contrastText: '#fff',
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
  //NOTICE: root route has to be the last route, otherwise it will match all the paths
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Switch>
          <Route path="/stats/:group/" component={StatsPage} />
          <Route path="/results/:value" component={ResultsPage} />
          <Route path="/" component={HomePage} />
        </Switch>
      </Router>
    </ThemeProvider>
  );
}

export default App;
