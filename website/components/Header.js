/* eslint-disable sort-keys */

import React, { Component } from 'react';
import toSlug from './toSlug';

export default class Header extends Component {
  render() {
    const slug = toSlug(this.props.toSlug || this.props.children);
    const Heading = 'h' + this.props.level;

    return (
      <Heading {...this.props}>
        <a className="anchor" name={slug} />
        {this.props.children}
        {' '}<a className="hash-link" href={'#' + slug}>#</a>
      </Heading>
    );
  }
}
