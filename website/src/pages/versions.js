/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';

import {
  useVersions,
  useLatestVersion,
} from '@docusaurus/plugin-content-docs/client';

function Version() {
  const {siteConfig} = useDocusaurusContext();
  const versions = useVersions();
  const latestVersion = useLatestVersion();
  const currentVersion = versions.find((version) => version.name === 'current');
  const pastVersions = versions.filter(
    (version) => version !== latestVersion && version.name !== 'current',
  ).concat([
    {
      "name": "2.24.1",
      "label": "2.24.1",
    },
    {
      "name": "2.24.0",
      "label": "2.24.0",
    },
    {
      "name": "2.23.0",
      "label": "2.23.0",
    },
    {
      "name": "2.22.0",
      "label": "2.22.0",
    },
    {
      "name": "2.21.0",
      "label": "2.21.0",
    },
    {
      "name": "2.20.0",
      "label": "2.20.0",
    },
    {
      "name": "2.19.0",
      "label": "2.19.0",
    },
    {
      "name": "2.18.0",
      "label": "2.18.0",
    },
    {
      "name": "2.17.0",
      "label": "2.17.0",
    },
    {
      "name": "2.16.0",
      "label": "2.16.0",
    },
    {
      "name": "2.15.0",
      "label": "2.15.0",
    },
    {
      "name": "2.14.0",
      "label": "2.14.0",
    },
    {
      "name": "2.13.0",
      "label": "2.13.0",
    },
    {
      "name": "2.12.0",
      "label": "2.12.0",
    },
    {
      "name": "2.11.0",
      "label": "2.11.0",
    },
    {
      "name": "2.10.0",
      "label": "2.10.0",
    },
    {
      "name": "2.9.0",
      "label": "2.9.0",
    },
    {
      "name": "2.8.0",
      "label": "2.8.0",
    },
    {
      "name": "2.7.0",
      "label": "2.7.0",
    },
    {
      "name": "2.6.0",
      "label": "2.6.0",
    },
    {
      "name": "2.4.1",
      "label": "2.4.1",
    },
    {
      "name": "2.4.0",
      "label": "2.4.0",
    },
    {
      "name": "2.3.0",
      "label": "2.3.0",
    },
    {
      "name": "2.2.0",
      "label": "2.2.0",
    },
    {
      "name": "2.1.0",
      "label": "2.1.0",
    },
    {
      "name": "2.0.0",
      "label": "2.0.0",
    },
  ]).sort((a, b) => {
    const [aMajor, aMinor, aPatchAndMore] = a.name.split(".");
    const [aPatch] = aPatchAndMore.split("-");
    const [bMajor, bMinor, bPatchAndMore] = b.name.split(".");
    const [bPatch] = bPatchAndMore.split("-");

    const a1 = parseInt(aMajor);
    const a2 = parseInt(aMinor);
    const a3 = parseInt(aPatch);

    const b1 = parseInt(bMajor);
    const b2 = parseInt(bMinor);
    const b3 = parseInt(bPatch);

    if (a1 > b1) {
      return -1;
    } else if (a1 === b1) {
      if (a2 > b2) {
        return -1;
      } else if (a2 === b2) {
        if (a3 > b3) {
          return -1;
        } else if (a3 === b3) {
          return 0;
        } else {
          return 1;
        }
      } else {
        return 1;
      }
    } else {
      return 1;
    }
  });
  console.log(JSON.stringify(pastVersions));
  // const stableVersion = pastVersions.shift();
  const stableVersion = currentVersion;
  const repoUrl = `https://github.com/${siteConfig.organizationName}/${siteConfig.projectName}`;

  const docLink = path => path ? <Link to={path}>Documentation</Link> : <span>&nbsp;</span>;

  const spaces = howMany => <span dangerouslySetInnerHTML={{__html: "&nbsp;".repeat(howMany)}} />;

  return (
    <Layout
      title="Versions"
      description="DevOops Versions page listing all documented site versions">
      <main className="container margin-vert--lg">
        <h1>DevOops documentation versions</h1>

        {stableVersion && (
          <div className="margin-bottom--lg">
            <h3 id="next">Current version (Stable)</h3>
            <p>
              Here you can find the documentation for current released version.
            </p>
            <table>
              <tbody>
              <tr>
                <th>{stableVersion.label}</th>
                <td>
                  <Link to={stableVersion.path}>Documentation</Link>
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v${stableVersion.label}`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        )}
        {/*
        <div className="margin-bottom--lg">
          <h3 id="latest">Next version (Unreleased)</h3>
          <p>
            Here you can find the documentation for work-in-process unreleased
            version.
          </p>
          <table>
            <tbody>
            <tr>
              <th>{latestVersion.label}</th>
              <td>
                <Link to={latestVersion.path}>Documentation</Link>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
        */}

        {pastVersions.length > 0 && (
          <div className="margin-bottom--lg">
            <h3 id="archive">Past versions (Not maintained anymore)</h3>
            <p>
              Here you can find documentation for previous versions of
              DevOops.
            </p>
            <table>
              <tbody>
              {pastVersions.map((version) => (
                <tr key={version.name}>
                  <th>{version.label}</th>
                  <td>
                    {docLink(version.path)}
                  </td>
                  <td>
                    <a href={`${repoUrl}/releases/tag/v${version.name}`}>
                      Release Notes
                    </a>
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
        {<div className="margin-bottom--lg">
          <h3 id="archive">Past versions without documentation (Not maintained anymore)</h3>
          <p>
            Here you can find documentation for previous versions of
            DevOops.
          </p>
          <table>
            <tbody>
              <tr key="1.0.2">
                <th>1.0.2</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v1.0.2`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              <tr key="1.0.1">
                <th>1.0.1</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v1.0.1`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              <tr key="1.0.0">
                <th>1.0.0</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v1.0.0`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              <tr key="0.3.1">
                <th>0.3.1</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v0.3.1`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              <tr key="0.3.0">
                <th>0.3.0</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v0.3.0`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              <tr key="0.2.0">
                <th>0.2.0</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v0.2.0`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              <tr key="0.1.0">
                <th>0.1.0</th>
                <td>
                  {spaces(27)}
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v0.1.0`}>
                    Release Notes
                  </a>
                </td>
              </tr>
            </tbody>
          </table>
        </div>}

      </main>
    </Layout>
  );
}

export default Version;