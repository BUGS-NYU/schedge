module.exports = {
  trailingSlash: true,

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
