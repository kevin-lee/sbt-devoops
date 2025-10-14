import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const algoliaConfig = require('./algolia.config.json');
const googleAnalyticsConfig = require('./google-analytics.config.json');

const lightCodeTheme = prismThemes.nightOwlLight;
const darkCodeTheme = prismThemes.nightOwl;

const isEmptyObject = (obj: object) => Object.keys(obj).length === 0;

const isSearchable = !isEmptyObject(algoliaConfig);
const hasGoogleAnalytics = !isEmptyObject(googleAnalyticsConfig);
const gtag = hasGoogleAnalytics ? { 'gtag': googleAnalyticsConfig } : {};

import LatestVersionImported from './latestVersion.json';
const latestVersionFound = LatestVersionImported as LatestVersion;

const websiteConfig = {
  title: 'sbt-devoops',
  tagline: 'Upload artifacts and changelog to GitHub Release',
  url: 'https://sbt-devoops.kevinly.dev',
  baseUrl: '/',
  favicon: 'img/favicon.png',
  organizationName: 'Kevin-Lee', // Usually your GitHub org/user name.
  projectName: 'sbt-devoops', // Usually your repo name.
  themeConfig: {
    prism: {
      theme: lightCodeTheme,
      darkTheme: darkCodeTheme,
      additionalLanguages: [
        'java',
        'scala',
      ],
    },
    navbar: {
      title: 'sbt-devoops',
      logo: {
        alt: 'sbt-devoops Logo',
        src: 'img/sbt-devoops-logo-32x32.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {
          type: 'docsVersionDropdown',
          position: 'right',
          dropdownActiveClassDisabled: true,
          dropdownItemsAfter: [
            {
              to: '/versions',
              label: 'All versions',
            },
          ],
        },
        {
          href: 'https://github.com/Kevin-Lee/sbt-devoops',
          className: 'header-github-link',
          'aria-label': 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Getting Started',
              to: 'docs/',
            },
            {
              label: 'DevOops - GitHub Release Plugin',
              to: 'docs/gh-release-plugin/config-and-run',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/Kevin-Lee/sbt-devoops',
            },
            {
              label: 'Blog',
              href: 'https://blog.kevinlee.io',
            },
            {
              label: 'Homepage',
              href: 'https://kevinlee.io',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} sbt-devoops, Website Built with Docusaurus.
      <br /><span style="font-size: 80%;">Some Icons made by 
      <a href="https://www.flaticon.com/authors/pixel-perfect" title="Pixel perfect">Pixel perfect</a> and 
      <a href="https://www.flaticon.com/authors/surang" title="surang">surang</a> from 
      <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a></span>`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          "lastVersion": "current",
          "versions": {
            "1.0.3": {
              "label": "1.0.3",
              "path": "1.0.3",
            },
            "2.5.0": {
              "label": "2.5.0",
              "path": "2.5.0",
            },
            "2.24.1": {
              "label": "2.24.1",
              "path": "2.24.1",
            },
            "current": {
              "label": `v${latestVersionFound.version}`,
            },
          },
        },
        blog: {
          showReadingTime: true,
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        ...gtag,
      },
    ],
  ],
};

if (isSearchable) {
  websiteConfig['themeConfig']['algolia'] = algoliaConfig;
}

module.exports = websiteConfig;
