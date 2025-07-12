# BrowserStack Cross-Browser Testing

A simple web scraper that tests article extraction across different browsers and devices using BrowserStack.

## What it does

- Scrapes articles from El País website
- Tests on 5 different browser configurations:
  - Windows Chrome
  - Windows Firefox  
  - macOS Safari
  - Android Chrome
  - iOS Safari

## Setup

1. Add your BrowserStack credentials to `src/main/resources/config.properties`
2. Run tests with: `mvn test`

## Test Results

The scraper extracts article titles and content from the "Opinión" section. Tests pass on desktop browsers and mobile devices (with some minor variations in content length).

## Notes

- Uses JavaScript clicks for better cross-browser compatibility
- Handles mobile navigation with hamburger menu
- Configured for W3C WebDriver protocol 