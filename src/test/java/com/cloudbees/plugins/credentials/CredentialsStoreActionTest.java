package com.cloudbees.plugins.credentials;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.gargoylesoftware.htmlunit.WebResponse;
import hudson.ExtensionList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CredentialsStoreActionTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void smokes() throws Exception {
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(
                SystemCredentialsProvider.ProviderImpl.class);

        CredentialsStore systemStore = system.getStore(j.getInstance());

        List<Domain> domainList = new ArrayList<Domain>(systemStore.getDomains());
        domainList.remove(Domain.global());
        for (Domain d : domainList) {
            systemStore.removeDomain(d);
        }

        List<Credentials> credentialsList = new ArrayList<Credentials>(systemStore.getCredentials(Domain.global()));
        for (Credentials c : credentialsList) {
            systemStore.removeCredentials(Domain.global(), c);
        }

        JenkinsRule.WebClient wc = j.createWebClient();
        WebResponse response = wc.goTo("credentials/store/system/api/xml?depth=5", "application/xml").getWebResponse();
        assertThat(response.getContentAsString(), is("<userFacingAction>"
                + "<domains>"
                + "<_>"
                + "<description>"
                + "Credentials that should be available irrespective of domain specification to requirements "
                + "matching."
                + "</description>"
                + "<displayName>Global credentials (unrestricted)</displayName>"
                + "<fullDisplayName>System » Global credentials (unrestricted)</fullDisplayName>"
                + "<fullName>system/_</fullName>"
                + "<global>true</global>"
                + "<urlName>_</urlName>"
                + "</_>"
                + "</domains>"
                + "</userFacingAction>"));

        Random entropy = new Random();
        String domainName = "test" + entropy.nextInt();
        String domainDescription = "test description " + entropy.nextLong();
        String credentialId = "test-id-" + entropy.nextInt();
        String credentialDescription = "test-account-" + entropy.nextInt();
        String credentialUsername = "test-user-" + entropy.nextInt();
        systemStore.addDomain(new Domain(domainName, domainDescription, Collections.<DomainSpecification>emptyList()),
                new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialId,
                        credentialDescription, credentialUsername, "test-secret"));
        response = wc.goTo("credentials/store/system/api/xml?depth=5", "application/xml").getWebResponse();
        assertThat(response.getContentAsString(), is("<userFacingAction>"
                + "<domains>"
                + "<_>"
                + "<description>"
                + "Credentials that should be available irrespective of domain specification to requirements "
                + "matching."
                + "</description>"
                + "<displayName>Global credentials (unrestricted)</displayName>"
                + "<fullDisplayName>System » Global credentials (unrestricted)</fullDisplayName>"
                + "<fullName>system/_</fullName>"
                + "<global>true</global>"
                + "<urlName>_</urlName>"
                + "</_>"
                + "<" + domainName + ">"
                + "<credential>"
                + "<description>" + credentialDescription + "</description>"
                + "<displayName>" + credentialUsername + "/****** (" + credentialDescription + ")</displayName>"
                + "<fullName>system/" + domainName + "/" + credentialId + "</fullName>"
                + "<id>" + credentialId + "</id>"
                + "<typeName>Username with password</typeName>"
                + "</credential>"
                + "<description>"
                + domainDescription
                + "</description>"
                + "<displayName>" + domainName + "</displayName>"
                + "<fullDisplayName>System » " + domainName + "</fullDisplayName>"
                + "<fullName>system/" + domainName + "</fullName>"
                + "<global>false</global>"
                + "<urlName>" + domainName + "</urlName>"
                + "</" + domainName + ">"
                + "</domains>"
                + "</userFacingAction>"));

    }
}
