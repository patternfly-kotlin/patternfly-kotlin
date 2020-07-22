config.module.rules.push({
    test: /\.css$/i,
    loader: 'style-loader!css-loader'
});

config.module.rules.push({
    test: /\.s[ac]ss$/i,
    loader: 'style-loader!css-loader!sass-loader'
});
