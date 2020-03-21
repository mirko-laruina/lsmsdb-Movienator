import React, { Component } from 'react'

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'

class ResultsPage extends Component {

    render(){
        return (
            <BasicPage>
                <br />
                <MyCard><br />Results for... I don't know</MyCard>
            </BasicPage>
        )
    }
}

export default ResultsPage;