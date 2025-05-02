/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * Class and subclasses to hold information about a Jira issue to create.
 *
 * @author Tyler
 */
public class JiraCreateIssueData {

    private final JiraIssueField fields;

    /**
     * Constructor to create a representation of a Jira issue. This is supposed to be passed through Gson and to an API call.
     *
     * @param summary     The summery of the Jira issue to be created. This is the main header line in Jira.
     * @param description The description of the Jira issue. This is shown when you click on an issue to see more information.
     */
    public JiraCreateIssueData(String summary, String description) {
        fields = new JiraIssueField(summary, description);
    }

    /**
     * Subclass to make sure the JSON generated from Gson is in the right format for Jira. Basically a subsection for the issue.
     */
    private class JiraIssueField {

        private final JiraIssueIdHolder project = new JiraIssueIdHolder(ApiConstants.JIRA_PROJECT);
        private final JiraIssueIdHolder issuetype = new JiraIssueIdHolder(ApiConstants.JIRA_ISSUE_TYPE);
        private final String summary;
        private final String description;

        /**
         * Constructor to create a representation of a Jira field in JSON.
         *
         * @param sum The summery of the Jira issue. This is the header line on issues.
         * @param des The long description of a Jira issue. This is shown when you click on more details.
         */
        public JiraIssueField(String sum, String des) {
            summary = sum;
            description = des;
        }
    }

    /**
     * Subclass to hold an id param and a value. Used to format the JSON correctly.
     */
    private class JiraIssueIdHolder {

        String id;

        public JiraIssueIdHolder(String val) {
            id = val;
        }
    }
}
