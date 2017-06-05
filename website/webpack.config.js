const glob = require('glob');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const MergeFilesPlugin = require('merge-files-webpack-plugin');

module.exports = {
  devtool: 'cheap-eval-source-map',
  entry: glob.sync('src/**/*.js').reduce((ret, x) => {
    ret[path.relative('src', x)] = path.join(__dirname, '.', x);
    return ret;
  }, {}),
  output: {
    path: path.resolve('./dist'),
    filename: '[name]',
    libraryTarget: 'commonjs2',
    publicPath: '/',
  },
  target: 'node',
  module: {
    loaders: [
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: path.join(__dirname, '.', 'node_modules'),
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract({
          use: [
            {
              loader: 'css-loader',
              options: {
                localIdentName: '[hash:8]',
                modules: true,
                minimize: true,
              },
            },
          ],
        }),
      },
    ],
  },
  plugins: [
    new ExtractTextPlugin({
      filename: '[name].css',
      allChunks: true,
    }),
    new MergeFilesPlugin({
      filename: 'css/main.css',
      test: /\.css$/,
      deleteSourceFiles: true,
    }),
  ],
};
