/**
 *
 * Copyright 2006
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.codehaus.plexus.swizzle;

/**
 * Author: John Tolentino
 */
public class ReportConfiguration
{
    private String projectKey;
    private String projectVersion;
    private String jiraServerUrl;
    private String template;

    public static final String RESOLVED_ISSUES_TEMPLATE = "org/codehaus/plexus/swizzle/ResolvedIssues.vm";
    public static final String VOTES_REPORT_TEMPLATE = "org/codehaus/plexus/swizzle/Votes.vm";


    public ReportConfiguration( String projectKey, String projectVersion, String jiraServerUrl, String template )
    {
        this.projectKey = projectKey;
        this.projectVersion = projectVersion;
        this.jiraServerUrl = jiraServerUrl;
        this.template = template;
    }

    public ReportConfiguration()
    {
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey( String projectKey )
    {
        this.projectKey = projectKey;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    public void setProjectVersion( String projectVersion )
    {
        this.projectVersion = projectVersion;
    }

    public String getJiraServerUrl()
    {
        return jiraServerUrl;
    }

    public void setJiraServerUrl( String jiraServerUrl )
    {
        this.jiraServerUrl = jiraServerUrl;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate( String template )
    {
        this.template = template;
    }

}