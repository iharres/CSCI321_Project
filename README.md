# BrickFolio — LEGO Portfolio & Financial Tracking

Track the value of your LEGO collection the way you'd track a stock portfolio — real-time pricing, condition-aware valuations, and gains/losses at a glance.

## The Problem

LEGO collecting has grown into a serious asset class, and many collections are now worth thousands of dollars. But there's no good way to actually track what that collection is worth:

* Prices are scattered across eBay, BrickLink, PriceCharting, and other sites, with no single source of truth.
* Collectors have to manually search multiple marketplaces just to estimate what a set is worth.
* Condition makes a huge difference in price, but most pricing tools don't separate condition in enough detail to be useful.

## Our Solution

We built a tool that makes tracking LEGO value feel like tracking a stock portfolio: add your sets, and get real-time, condition-aware valuations pulled automatically from multiple marketplaces.

**How it works:**

1. We pull recent sales listings from eBay and filter them using keyword matching to determine the condition of each sale (new/sealed, used, incomplete, etc.).
2. We combine this with sales data from BrickLink to cross-reference and determine the most accurate current market price.
3. We filter out obvious outliers (mispriced or bundled listings) before calculating a final value.
4. Your full collection is displayed in one portfolio view, so you can track total value and gains/losses over time.

## Key Features

* **Real-time tracking** — pulls live sales data instead of relying on stale, manually-updated price guides.
* **Collection in one place** — no more checking eBay, BrickLink, and PriceCharting separately.
* **Condition-aware pricing** — values reflect the actual condition of your sets, not a single blended average.
* **Gains/losses at a glance** — see how your collection's value has changed over time, just like an investment portfolio.
* **Simple, familiar interface** — designed to feel like a stock/investment tracking app.

## Roadmap

Planned expansions beyond the initial release:

* Support for tracking minifigures in addition to sets.
* Price alerts (e.g. notify when a set crosses a target value).
* Expansion to other collectibles beyond LEGO.

## Team
Ian Harres - Backend
Immanuel Pitts - Github and where ever else help is needed
Will Stallworth - Frontend

## Status

This project is under active development as part of a semester-long team project.
