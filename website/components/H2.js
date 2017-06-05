const React = require('react');
const Header = require('Header');

const H2 = React.createClass({
  render() {
    return <Header {...this.props} level={2}>{this.props.children}</Header>;
  },
});

module.exports = H2;
