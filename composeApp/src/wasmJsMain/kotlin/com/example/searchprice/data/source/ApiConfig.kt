package com.example.searchprice.data.source

// Cloudflare Worker proxy — handles HTTPS + CORS for the browser.
// Deploy the worker from cloudflare-proxy/worker.js and paste the URL here.
actual val apiBaseUrl: String =
    "https://searchprice-proxy.YOUR_SUBDOMAIN.workers.dev"
