/* eslint-disable max-len */

const BlogPost = require('BlogPost');
const BlogSidebar = require('BlogSidebar');
const Container = require('Container');
const React = require('react');
const Site = require('Site');

const BlogPostLayout = React.createClass({
  render() {
    return (
      <Site
        className="sideNavVisible"
        section="blog"
        url={'blog/' + this.props.metadata.path}
        title={this.props.metadata.title}
        description={this.props.children.trim().split('\n')[0]}
      >
        <div className="docMainWrapper wrapper">
          <BlogSidebar current={this.props.metadata} />
          <Container className="mainContainer documentContainer postContainer blogContainer">
            <div className="lonePost">
              <BlogPost
                post={this.props.metadata}
                content={this.props.children}
              />
            </div>
          </Container>
        </div>
      </Site>
    );
  },
});

module.exports = BlogPostLayout;
