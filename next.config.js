module.exports = {
  trailingSlash: true,
  productionBrowserSourceMaps: true,

  webpack(config) {
    config.module.rules.push({
      test: /\.svg$/,
      use: ["@svgr/webpack"]
    });

    return config;
  },

  // @NOTE: These aren't applied when exporting, but
  // that's fine. The rewrites are there so that
  // when developing the frontend, we can get the features
  // of the Next.js dev server, with it correctly
  // deferring to the normal Schedge server when necessary.
  //
  //            - Albert Liu, Oct 18, 2022 Tue 00:42
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:4358/api/:path*",
      },
      {
        source: "/webjars/:path*",
        destination: "http://localhost:4358/webjars/:path*",
      },
    ];
  },
};
