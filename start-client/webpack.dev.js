/* eslint-disable */

const { merge } = require('webpack-merge')
const common = require('./webpack.common.js')
const path = require('path')

const mock = require('./dev/api.mock.json')
const fs = require('fs')

const config = {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    static: path.resolve(__dirname, 'public'),
    historyApiFallback: true,
    compress: true,
    open: false
  },
}

module.exports = merge(common, config)
