import React from 'react';
import Marked from './Marked';

export default class Doc extends React.Component {
  render() {
    return (
      <div className="post">
        <header className="postHeader">
          <a
            className="edit-page-link button"
            href={
              'https://github.com/anmonteiro/lumo/edit/master/docs/' +
              this.props.source
            }
            target="_blank"
          >
            Edit this Doc
          </a>
          <h1>{this.props.title}</h1>
        </header>
        <article>
          <Marked>{this.props.content}</Marked>
        </article>
      </div>
    );
  }
}
