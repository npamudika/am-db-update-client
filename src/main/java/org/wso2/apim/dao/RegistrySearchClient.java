package org.wso2.apim.dao;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.client.WSRegistrySearchClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Registry Search Client used to fetch APIs from the Registry.
 */
public class RegistrySearchClient {
    private static ConfigurationContext configContext = null;
    private static String cookie = null;
    private static Properties prop;

    private static String CARBON_HOME = null;
    private static String axis2Repo = null;
    private static String axis2Conf = ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private static String username = null;
    private static String password = null;
    private static String serverURL = null;
    private static int pagination;
    private static Registry registry;

    public RegistrySearchClient() throws IOException {
        prop = new Properties();
        InputStream input = new FileInputStream("conf/reg.properties");
        prop.load(input);

        CARBON_HOME = prop.getProperty("carbon-home");
        axis2Repo = CARBON_HOME + File.separator + "repository" +
                File.separator + "deployment" + File.separator + "client";
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        serverURL = prop.getProperty("serverURL");
        pagination = Integer.parseInt(prop.getProperty("pagination"));
    }

    private static WSRegistryServiceClient initialize() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                prop.getProperty("javax.net.ssl.trustStore"));
        System.setProperty("javax.net.ssl.trustStorePassword", prop.getProperty("javax.net.ssl.trustStorePassword"));
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        return new WSRegistryServiceClient(serverURL, username, password, configContext);
    }

    public GenericArtifact[] findApis() throws Exception {
        try {
            registry = initialize();
            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, username);
            //Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);
            //Initialize the pagination context.
            PaginationContext.init(0, pagination, "", "", 10);
            WSRegistrySearchClient wsRegistrySearchClient = new WSRegistrySearchClient();
            cookie = wsRegistrySearchClient.authenticate(configContext, serverURL, username, password);
            //This should be execute to initialize the AttributeSearchService.
            wsRegistrySearchClient.init(cookie, serverURL, configContext);
            //Initialize the GenericArtifactManager
            GenericArtifactManager artifactManager = new GenericArtifactManager(gov, "api");
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            //Create the search attribute map
            listMap.put("overview_status", new ArrayList<String>() {{
                add("PUBLISHED");
            }});
            //Find the results
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
            System.out.println("APIs in the PUBLISHED state:");
            int i = 1;
            for(GenericArtifact artifact : genericArtifacts){
                System.out.println(Integer.toString(i).concat(". ").concat(artifact.getQName().getLocalPart()));
                i = i+1;
            }
            return genericArtifacts;
        } finally {
            PaginationContext.destroy();
            ((WSRegistryServiceClient)registry).logut();
        }
    }
}
