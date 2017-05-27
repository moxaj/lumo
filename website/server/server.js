/* eslint-disable max-len, sort-keys */

'use strict';
const connect = require('connect');
const serveStatic = require('serve-static');
const favicon = require('serve-favicon');
const morgan = require('morgan');
const compression = require('compression');
const errorhandler = require('errorhandler');
const convert = require('./convert.js');
const fs = require('fs');
const http = require('http');
const optimist = require('optimist');
const path = require('path');
import reactSSRMiddleware from './react-ssr-middleware';

const argv = optimist.argv;

const PROJECT_ROOT = path.resolve(__dirname, '..');
const FILE_SERVE_ROOT = path.join(PROJECT_ROOT, 'src');

let port = argv.port;
if (argv.$0.indexOf('node ./server/generate.js') !== -1) {
  // Using a different port so that you can publish the website
  // and keeping the server up at the same time.
  port = 8079;
}

const app = connect()
  .use((req, res, next) => {
    // convert all the md files on every request. This is not optimal
    // but fast enough that we don't really need to care right now.
    convert(next);
  })
  .use('/blog/feed.xml', (req, res) => {
    res.end(fs.readFileSync(path.join(FILE_SERVE_ROOT, 'blog/feed.xml')) + '');
  })
  .use('/blog/atom.xml', (req, res) => {
    res.end(fs.readFileSync(path.join(FILE_SERVE_ROOT, 'blog/atom.xml')) + '');
  })
  .use(reactSSRMiddleware)
  .use(serveStatic(FILE_SERVE_ROOT))
  .use(favicon(path.join(FILE_SERVE_ROOT, 'favicon.ico')))
  .use(morgan('combined'))
  .use(compression())
  .use(errorhandler());

const portToUse = port || 8080;
const server = http.createServer(app);
server.listen(portToUse);
console.log('Open http://localhost:' + portToUse + '/index.html');
module.exports = server;
