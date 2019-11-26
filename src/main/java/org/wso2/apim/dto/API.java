package org.wso2.apim.dto;

/**
 * API Json Object DTO.
 */
public class API {
    private int apiId;
    private String apiName;
    private String provider;
    private String version;
    private String previousState;
    private String newState;
    private String userId;
    private int tenantId;

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "API{" +
                "apiId=" + apiId +
                ", apiName='" + apiName + '\'' +
                ", provider='" + provider + '\'' +
                ", version='" + version + '\'' +
                ", previousState='" + previousState + '\'' +
                ", newState='" + newState + '\'' +
                ", userId='" + userId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
