# AM_DB Update Client with Registry Search

This client updates the WSO2 AM_DB for the given set of APIs by modifying the API's lifecycle state.

First the APIs are queried from the registry for the lifecycle state=PUBLISHED.

And then check for the queried APIs in the AM_DB and if the lifecycle state is not equal to PUBLISHED, then it updates the AM_API_LC_EVENT table in AM_DB database.
