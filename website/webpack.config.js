const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const nodeExternals = require('webpack-node-externals');

module.exports = {
  devtool: 'cheap-eval-source-map',
  entry: './index.js',
  output: {
    path: path.resolve('./dist'),
    filename: '[name].js',
    chunkFilename: '[name].js',
    libraryTarget: 'commonjs2',
    publicPath: '/',
  },
  target: 'node',
  externals: nodeExternals(),
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
      filename: 'css/[name].css',
      allChunks: true,
    }),
  ],
};
