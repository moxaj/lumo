import React, { Component } from 'react';
import Site from '../components/Site';
import Container from '../components/Container';

const siteConfig = require('../siteConfig.js');

export default class UserShowcase extends Component {
  render() {
    const showcase = siteConfig.users.map(user => {
      return (
        <a href={user.infoLink} key={user.image}>
          <img src={user.image} title={user.caption} />
        </a>
      );
    });

    return (
      <Site>
        <div className="mainContainer">
          <Container padding={['bottom', 'top']}>
            <div className="showcaseSection">
              <div className="prose">
                <h1>Who's using Jest?</h1>
                <p>
                  Jest is used by teams of all sizes to test websites, mobile
                  apps, and APIs.
                </p>
              </div>
              <div className="logos">
                {showcase}
              </div>
              <p>
                Is your company using Jest?
              </p>
              <a
                href="https://github.com/facebook/jest/edit/master/website/siteConfig.js"
                className="button"
              >
                Add your company
              </a>
            </div>
          </Container>
        </div>
      </Site>
    );
  }
}
