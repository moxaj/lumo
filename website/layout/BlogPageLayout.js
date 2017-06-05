/* eslint-disable max-len */

const BlogPost = require('BlogPost');
const BlogSidebar = require('BlogSidebar');
const Container = require('Container');
const MetadataBlog = require('MetadataBlog');
const React = require('react');
const Site = require('Site');

const BlogPageLayout = React.createClass({
  getPageURL(page) {
    let url = '/jest/blog/';
    if (page > 0) {
      url += 'page' + (page + 1) + '/';
    }
    return url + '#content';
  },

  render() {
    const perPage = this.props.metadata.perPage;
    const page = this.props.metadata.page;
    return (
      <Site section="blog" title="Blog">
        <div className="docMainWrapper wrapper">
          <BlogSidebar />
          <Container className="mainContainer documentContainer postContainer blogContainer">
            <div className="posts">
              {MetadataBlog.files
                .slice(page * perPage, (page + 1) * perPage)
                .map(post => {
                  return (
                    <BlogPost
                      post={post}
                      content={post.content}
                      truncate={true}
                      key={post.path + post.title}
                    />
                  );
                })}
              <div className="docs-prevnext">
                {page > 0 &&
                  <a className="docs-prev" href={this.getPageURL(page - 1)}>
                    &larr; Prev
                  </a>}
                {MetadataBlog.files.length > (page + 1) * perPage &&
                  <a className="docs-next" href={this.getPageURL(page + 1)}>
                    Next &rarr;
                  </a>}
              </div>
            </div>
          </Container>
        </div>
      </Site>
    );
  },
});

module.exports = BlogPageLayout;
