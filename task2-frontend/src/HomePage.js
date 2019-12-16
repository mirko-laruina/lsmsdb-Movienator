import React from 'react';
import { Component } from "react";
import {Container, Row, Col, InputGroup, FormControl, ButtonGroup, Button, Dropdown, DropdownButton} from 'react-bootstrap'
import './HomePage.css'

class HomePage extends Component {
    render() {
        return (
        <Container className="wrapper" fluid="true">
            <Row>
                <Col><h1>THE Movie Database</h1>
                </Col>
            </Row>
            <Row>
                <Col></Col>
                <Col sm={6}>
                    <h2>SearchDaMovie</h2>
                    <InputGroup className="mb-3">
                        <FormControl aria-label="Your search string" />
                        <InputGroup.Append>
                        <InputGroup.Text>Search</InputGroup.Text>
                        </InputGroup.Append>
                    </InputGroup>
                </Col>
                <Col></Col>
            </Row>
            <Row>
                <Col></Col>
                <Col sm={6}>
                    <h2>Explore statistics</h2>
                    <ButtonGroup>
                        <ButtonGroup vertical className="stat-btn-group">
                            <Button variant="outline-info" className="stat-btn" size="lg">By Director</Button>
                            <Button variant="outline-info" className="stat-btn" size="lg">By Author</Button>
                            <Button variant="outline-info" className="stat-btn" size="lg">By Country</Button>
                        </ButtonGroup>
                        <ButtonGroup vertical className="stat-btn-group">
                            <Button variant="outline-info" className="stat-btn" size="lg">By Year</Button>
                            <Button variant="outline-info" className="stat-btn" size="lg">By Genre</Button>
                        </ButtonGroup>
                    </ButtonGroup>
                </Col>
                <Col></Col>
            </Row>
        </Container>
        )
    }
}

export default HomePage