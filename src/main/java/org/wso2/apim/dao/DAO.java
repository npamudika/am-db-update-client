package org.wso2.apim.dao;

import org.wso2.apim.dto.API;
import org.wso2.apim.exception.DAOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

/**
 * Database Access Object used for the database client
 */
public class DAO {
    private APIMMySqlDataSource dataSource;
    private List<API> apisList = new ArrayList<API>();

    public DAO(String dataSourcePath) {
        //Initializes a hickariCP[https://github.com/brettwooldridge/HikariCP] datasource
        // from the provided data source properties file
        dataSource = new APIMMySqlDataSource(dataSourcePath);
    }

    public static String readFileAsString(String fileName) throws Exception {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    /**
     * Generates a list of API objects where the overview_state=PUBLISHED fetching data from the registry
     *
     * @return apisList
     * @throws DAOException if error occurred while generating the list of API objects
     */
    public void generateListOfAPIs() throws Exception {
        int count = 0;
        RegistrySearchClient registrySearchClient = new RegistrySearchClient();
        GenericArtifact[] genericArtifacts = registrySearchClient.findApis();
        for(GenericArtifact artifact : genericArtifacts){
            API api = new API();
            api.setApiName(artifact.getAttribute("overview_name"));
            api.setProvider(artifact.getAttribute("overview_provider"));
            api.setVersion(artifact.getAttribute("overview_version"));
            apisList.add(api);
            count++;
        }
        System.out.println("There are " + count + " API object(s) in the PUBLISHED state in the registry.");
    }

    /**
     * Insert a list of api objects to the DB if the DB state is CREATED only
     *
     * @throws DAOException if error occurred during accessing the DB
     */
    public void insertAPIObjects(String testrun) throws DAOException {
        System.out.println("Started checking the DB for the above APIs which have still been in the CREATED state:");
        System.out.println("Following APIs are still in the CREATED state:");
        int count = 0;
        int j = 1;
        for (int i = 0; i < apisList.size(); i++) {
            try (Connection connection = dataSource.getConnection()) {
                if (apisList.get(i).getProvider().contains("AT")) {
                    apisList.get(i).setProvider(apisList.get(i).getProvider().replace("-AT-", "@"));
                }
                //Select API_ID from AM_API table by providing name, provider and version
                final String selectAPIIdSql = "SELECT API_ID FROM AM_API WHERE API_NAME = '" + apisList.get(i).getApiName() + "' AND API_PROVIDER = '" +
                        apisList.get(i).getProvider() + "' AND API_VERSION = '" + apisList.get(i).getVersion() + "';";
                PreparedStatement preparedSelectIdStatement = connection.prepareStatement(selectAPIIdSql);
                ResultSet apiIdResultSet = preparedSelectIdStatement.executeQuery();
                while (apiIdResultSet.next()) {
                    apisList.get(i).setApiId(apiIdResultSet.getInt("API_ID"));
                }
                //Get the particular record from AM_API_LC_EVENT table for the API_ID descending ordering by EVENT_DATE and setting limit to 1
                final String selectAPIStateSql = "SELECT PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID FROM AM_API_LC_EVENT WHERE API_ID = " + apisList.get(i).getApiId() + " ORDER BY" +
                        " EVENT_DATE DESC LIMIT 1;";
                PreparedStatement preparedSelectStatement = connection.prepareStatement(selectAPIStateSql);
                ResultSet resultSet = preparedSelectStatement.executeQuery();
                while (resultSet.next()) {
                    apisList.get(i).setPreviousState(resultSet.getString("PREVIOUS_STATE"));
                    apisList.get(i).setNewState(resultSet.getString("NEW_STATE"));
                    apisList.get(i).setUserId(resultSet.getString("USER_ID"));
                    apisList.get(i).setTenantId(resultSet.getInt("TENANT_ID"));
                }
                String apiState = apisList.get(i).getNewState();
                if ("CREATED".equals(apiState)) {
                    if ("--dryrun=true".equals(testrun)) {
                        System.out.println(j + ". " + apisList.get(i).getApiName());
                        j = j+1;
                    } else {
                        System.out.println(j + ". " + apisList.get(i).getApiName());
                        j = j+1;
                        System.out.println("----- Starting to update the API " + apisList.get(i).getApiName() + " with the new_state.");
                        final String insertAPIObjectSql = "INSERT INTO AM_API_LC_EVENT (API_ID, PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID) VALUES"
                                + "('" + apisList.get(i).getApiId() + "','" + "CREATED" + "', '" + "PUBLISHED"
                                + "', '" + apisList.get(i).getUserId() + "', '" + apisList.get(i).getTenantId() + "');";
                        PreparedStatement preparedInsertStatement = connection.prepareStatement(insertAPIObjectSql);
                        preparedInsertStatement.executeUpdate();
                        System.out.println("----- " + apisList.get(i).getApiName() + " has been updated with the new_state as PUBLISHED.");
                        count++;
                    }
                }
            } catch (SQLException e) {
                throw new DAOException("Error while inserting API objects to the DB.", e);
            }
        }
        System.out.println(count + " API object(s) have been inserted to the DB updating the LC State.");
    }
}
