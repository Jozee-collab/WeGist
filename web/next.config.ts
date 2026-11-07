import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  cacheComponents: true,
  async headers() {
    const headers = [
      { key: "X-Content-Type-Options", value: "nosniff" },
      { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
      { key: "X-Frame-Options", value: "SAMEORIGIN" },
      { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
    ]

    if (process.env.NODE_ENV === "production") {
      headers.push({ key: "Strict-Transport-Security", value: "max-age=63072000" })
    }

    return [{ source: "/:path*", headers }]
  },
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
