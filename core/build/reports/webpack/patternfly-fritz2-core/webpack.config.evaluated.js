{
  mode: 'production',
  resolve: {
    modules: [
      '/Users/hpehl/dev/patternfly/kotlin/patternfly-fritz2/build/js/packages/patternfly-fritz2-core/kotlin-dce',
      'node_modules'
    ]
  },
  plugins: [
    ProgressPlugin {
      profile: false,
      handler: [Function: handler],
      modulesCount: 500,
      showEntries: false,
      showModules: true,
      showActiveModules: true
    }
  ],
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [
          'kotlin-source-map-loader'
        ],
        enforce: 'pre'
      },
      {
        test: /\.css$/,
        use: [
          {
            loader: 'style-loader',
            options: {}
          },
          {
            loader: 'css-loader',
            options: {}
          }
        ]
      }
    ]
  },
  entry: {
    main: [
      '/Users/hpehl/dev/patternfly/kotlin/patternfly-fritz2/build/js/packages/patternfly-fritz2-core/kotlin-dce/patternfly-fritz2-core.js'
    ]
  },
  output: {
    path: '/Users/hpehl/dev/patternfly/kotlin/patternfly-fritz2/core/build/distributions',
    filename: [Function: filename],
    library: 'core',
    libraryTarget: 'umd'
  },
  devtool: 'source-map'
}