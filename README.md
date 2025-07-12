# BrowserStack Cross-Browser Testing Assignment

A comprehensive web scraping solution that demonstrates skills in Selenium automation, API integration, and cross-browser testing using BrowserStack.

## Technical Assignment Overview

This project implements a complete solution for:
- **Web Scraping**: Extracting articles from El País Spanish news website
- **API Integration**: Using Rapid Translate API for text translation
- **Text Processing**: Analyzing translated content for repeated words
- **Cross-Browser Testing**: Running tests across 5 different browser configurations

## Features

### Web Scraping
- Navigates to El País website (Spanish news outlet)
- Scrapes articles from the "Opinión" (Opinion) section
- Extracts titles and content in Spanish
- Downloads cover images for each article
- Handles both desktop and mobile navigation

### Translation & Analysis
- Translates article titles from Spanish to English using Rapid Translate API
- Analyzes translated headers to find repeated words (occurring more than twice)
- Prints word frequency analysis

### Cross-Browser Testing
Tests across 5 different browser configurations:
- Windows Chrome
- Windows Firefox  
- macOS Safari
- Android Chrome
- iOS Safari

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ishivamkunal/Browserstack_Assignment.git
   cd Browserstack_Assignment
   ```

2. **Configure credentials**
   - Copy `config.sample.properties` to `src/main/resources/config.properties`
   - Add your credentials:
     ```
     RAPID_API_KEY=your_rapid_api_key_here
     BROWSERSTACK_USERNAME=your_browserstack_username_here
     BROWSERSTACK_ACCESS_KEY=your_browserstack_access_key_here
     ```

3. **Run tests**
   ```bash
   mvn test
   ```

## Test Results

The scraper successfully:
- Extracts 5 articles from the Opinion section
- Translates titles to English
- Analyzes word frequency in translated headers
- Downloads article images
- Runs across all browser configurations

## Technical Implementation

- **Selenium WebDriver**: For web automation
- **TestNG**: For parallel test execution
- **BrowserStack**: For cross-browser testing
- **Rapid Translate API**: For text translation
- **Maven**: For dependency management

## Notes

- Uses JavaScript clicks for better cross-browser compatibility
- Handles mobile navigation with hamburger menu
- Configured for W3C WebDriver protocol
- Sensitive credentials are excluded from Git tracking 