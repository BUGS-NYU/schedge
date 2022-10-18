module.exports = {
  trailingSlash: true,

  // @NOTE: These aren't applied when exporting, but
  // that's fine. It doesn't matter.
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
