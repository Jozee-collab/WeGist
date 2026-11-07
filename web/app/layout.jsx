import "./globals.css";

export const metadata = {
  title: "WeGist — Play together",
  description: "Chat, share your best plays, and find your next game on WeGist.",
  applicationName: "WeGist"
};

export const viewport = {
  width: "device-width",
  initialScale: 1,
  colorScheme: "dark",
  themeColor: "#0b0d12"
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
