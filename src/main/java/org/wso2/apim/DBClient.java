package org.wso2.apim;

import org.wso2.apim.dao.DAO;

/**
 * Database Client Implementation.
 */
public class DBClient {
    private static final String SYS_PROP_ENV_CONF = "dbEnvConfig";
    private static final String SYS_PROP_ENV_CONF_DEFAULT = "conf/db-env.properties";

    public static void main(String[] args) throws Exception {
        System.out.println("Args: " + args[0]);
        //Setting environment details
        // by retrieving environment configs via system properties or using default config file
        String dbEnvProperties = System.getProperty(SYS_PROP_ENV_CONF) != null ?
                System.getProperty(SYS_PROP_ENV_CONF) : SYS_PROP_ENV_CONF_DEFAULT;

        //Create data access objects for the DB environment
        DAO dbEnvDAO = new DAO(dbEnvProperties);

        dbEnvDAO.generateListOfAPIs();
        dbEnvDAO.insertAPIObjects(args[0]);
    }
}
