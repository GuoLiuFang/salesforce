package com.glodon.bigdata;

import java.io.*;

import com.sforce.soap.enterprise.DeleteResult;
import com.sforce.soap.enterprise.DescribeGlobalResult;
import com.sforce.soap.enterprise.DescribeGlobalSObjectResult;
import com.sforce.soap.enterprise.DescribeSObjectResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.Field;
import com.sforce.soap.enterprise.FieldType;
import com.sforce.soap.enterprise.GetUserInfoResult;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.PicklistEntry;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.soap.enterprise.sobject.Event;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.ConnectionException;

public class QuickstartApiSample {

    private static BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));

    EnterpriseConnection connection;
    String authEndPoint = "";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: com.example.samples."
                    + "QuickstartApiSamples <AuthEndPoint>");

            System.exit(-1);
        }

        QuickstartApiSample sample = new QuickstartApiSample(args[0]);
        sample.run();
    }

    public void run() throws IOException {
        // Make a login call
        if (login()) {
            // Do a describe global
//            看看这个表输出的是什么
//            describeGlobalSample();

            // Describe an object
            describeSObjectsSample();
//搞清楚了，这是对每张表的详细解释。。。
            // Retrieve some data using a query
//            querySample();
//            querySampleTest();
//            querySampleAccount();
//            querySampleContact();
            // Log out
            logout();
        }
    }

    // Constructor
    public QuickstartApiSample(String authEndPoint) {
        this.authEndPoint = authEndPoint;
    }

    private String getUserInput(String prompt) {
        String result = "";
        try {
            System.out.print(prompt);
            result = reader.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return result;
    }

    private boolean login() {
        boolean success = false;
        String username = getUserInput("Enter username: ");
        String password = getUserInput("Enter password: ");

        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setUsername(username);
            config.setPassword(password);

            System.out.println("AuthEndPoint: " + authEndPoint);
            config.setAuthEndpoint(authEndPoint);

            connection = new EnterpriseConnection(config);
            printUserInfo(config);

            success = true;
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        return success;
    }

    private void printUserInfo(ConnectorConfig config) {
        try {
            GetUserInfoResult userInfo = connection.getUserInfo();

            System.out.println("\nLogging in ...\n");
            System.out.println("UserID: " + userInfo.getUserId());
            System.out.println("User Full Name: " + userInfo.getUserFullName());
            System.out.println("User Email: " + userInfo.getUserEmail());
            System.out.println();
            System.out.println("SessionID: " + config.getSessionId());
            System.out.println("Auth End Point: " + config.getAuthEndpoint());
            System.out
                    .println("Service End Point: " + config.getServiceEndpoint());
            System.out.println();
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    private void logout() {
        try {
            connection.logout();
            System.out.println("Logged out.");
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    /**
     * To determine the objects that are available to the logged-in user, the
     * sample client application executes a describeGlobal call, which returns
     * all of the objects that are visible to the logged-in user. This call
     * should not be made more than once per session, as the data returned from
     * the call likely does not change frequently. The DescribeGlobalResult is
     * simply echoed to the console.
     */
    private void describeGlobalSample() {
        try {
            // describeGlobal() returns an array of object results that
            // includes the object names that are available to the logged-in user.
            DescribeGlobalResult dgr = connection.describeGlobal();

            System.out.println("\nDescribe Global Results:\n");
            // Loop through the array echoing the object names to the console
            for (int i = 0; i < dgr.getSobjects().length; i++) {
                System.out.println(dgr.getSobjects()[i].getName());
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    /**
     * The following method illustrates the type of metadata information that can
     * be obtained for each object available to the user. The sample client
     * application executes a describeSObject call on a given object and then
     * echoes the returned metadata information to the console. Object metadata
     * information includes permissions, field types and length and available
     * values for picklist fields and types for referenceTo fields.
     */
    private void describeSObjectsSample() {
        String objectToDescribe = getUserInput("\nType the name of the object to "
                + "describe (try Account): ");

        try {
            // Call describeSObjects() passing in an array with one object type
            // name
            DescribeSObjectResult[] dsrArray = connection
                    .describeSObjects(new String[]{objectToDescribe});

            // Since we described only one sObject, we should have only
            // one element in the DescribeSObjectResult array.
            DescribeSObjectResult dsr = dsrArray[0];

            // First, get some object properties
            System.out.println("\n\nObject Name: " + dsr.getName());

            if (dsr.getCustom())
                System.out.println("Custom Object");
            if (dsr.getLabel() != null)
                System.out.println("Label: " + dsr.getLabel());

            // Get the permissions on the object

            if (dsr.getCreateable())
                System.out.println("Createable");
            if (dsr.getDeletable())
                System.out.println("Deleteable");
            if (dsr.getQueryable())
                System.out.println("Queryable");
            if (dsr.getReplicateable())
                System.out.println("Replicateable");
            if (dsr.getRetrieveable())
                System.out.println("Retrieveable");
            if (dsr.getSearchable())
                System.out.println("Searchable");
            if (dsr.getUndeletable())
                System.out.println("Undeleteable");
            if (dsr.getUpdateable())
                System.out.println("Updateable");

            System.out.println("Number of fields: " + dsr.getFields().length);

            // Now, retrieve metadata for each field
            for (int i = 0; i < dsr.getFields().length; i++) {
                // Get the field
                Field field = dsr.getFields()[i];

                // Write some field properties
                System.out.println("Field name: " + field.getName());
                System.out.println("\tField Label: " + field.getLabel());

                // This next property indicates that this
                // field is searched when using
                // the name search group in SOSL
                if (field.getNameField())
                    System.out.println("\tThis is a name field.");

                if (field.getRestrictedPicklist())
                    System.out.println("This is a RESTRICTED picklist field.");

                System.out.println("\tType is: " + field.getType());

                if (field.getLength() > 0)
                    System.out.println("\tLength: " + field.getLength());

                if (field.getScale() > 0)
                    System.out.println("\tScale: " + field.getScale());

                if (field.getPrecision() > 0)
                    System.out.println("\tPrecision: " + field.getPrecision());

                if (field.getDigits() > 0)
                    System.out.println("\tDigits: " + field.getDigits());

                if (field.getCustom())
                    System.out.println("\tThis is a custom field.");

                // Write the permissions of this field
                if (field.getNillable())
                    System.out.println("\tCan be nulled.");
                if (field.getCreateable())
                    System.out.println("\tCreateable");
                if (field.getFilterable())
                    System.out.println("\tFilterable");
                if (field.getUpdateable())
                    System.out.println("\tUpdateable");

                // If this is a picklist field, show the picklist values
                if (field.getType().equals(FieldType.picklist)) {
                    System.out.println("\t\tPicklist values: ");
                    PicklistEntry[] picklistValues = field.getPicklistValues();

                    for (int j = 0; j < field.getPicklistValues().length; j++) {
                        System.out.println("\t\tValue: "
                                + picklistValues[j].getValue());
                    }
                }

                // If this is a foreign key field (reference),
                // show the values
                if (field.getType().equals(FieldType.reference)) {
                    System.out.println("\tCan reference these objects:");
                    for (int j = 0; j < field.getReferenceTo().length; j++) {
                        System.out.println("\t\t" + field.getReferenceTo()[j]);
                    }
                }
                System.out.println("");
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    //这个可以重命名成别的方法啦。。
    private void querySample() {
        String soqlQuery = "SELECT FirstName, LastName FROM Contact";
        try {
            QueryResult qr = connection.query(soqlQuery);
            boolean done = false;

            if (qr.getSize() > 0) {
                System.out.println("\nLogged-in user can see "
                        + qr.getRecords().length + " contact records.");

                while (!done) {
                    System.out.println("");
                    SObject[] records = qr.getRecords();
                    for (int i = 0; i < records.length; ++i) {
                        Contact con = (Contact) records[i];
                        String fName = con.getFirstName();
                        String lName = con.getLastName();

                        if (fName == null) {
                            System.out.println("Contact " + (i + 1) + ": " + lName);
                        } else {
                            System.out.println("Contact " + (i + 1) + ": " + fName
                                    + " " + lName);
                        }
                    }

                    if (qr.isDone()) {
                        done = true;
                    } else {
                        qr = connection.queryMore(qr.getQueryLocator());
                    }
                }
            } else {
                System.out.println("No records found.");
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    private void querySampleContact() {
        String soqlQuery = "SELECT FirstName, LastName FROM Contact";
        try {
            QueryResult qr = connection.query(soqlQuery);
            boolean done = false;

            if (qr.getSize() > 0) {
                System.out.println("\nLogged-in user can see "
                        + qr.getRecords().length + " contact records.");

                while (!done) {
                    System.out.println("");
                    SObject[] records = qr.getRecords();
                    for (int i = 0; i < records.length; ++i) {
                        Contact con = (Contact) records[i];
                        String fName = con.getFirstName();
                        String lName = con.getLastName();

                        if (fName == null) {
                            System.out.println("Contact " + (i + 1) + ": " + lName);
                        } else {
                            System.out.println("Contact " + (i + 1) + ": " + fName
                                    + " " + lName);
                        }
                    }

                    if (qr.isDone()) {
                        done = true;
                    } else {
                        qr = connection.queryMore(qr.getQueryLocator());
                    }
                }
            } else {
                System.out.println("No records found.");
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    private void querySampleAccount() throws IOException {
        String soqlQuery = "SELECT Id,IsDeleted,MasterRecordId,Name,Type,RecordTypeId,ParentId,BillingStreet,BillingCity,BillingState,BillingPostalCode,BillingCountry,BillingStateCode,BillingCountryCode,BillingLatitude,BillingLongitude,BillingGeocodeAccuracy,BillingAddress,ShippingStreet,ShippingCity,ShippingState,ShippingPostalCode,ShippingCountry,ShippingStateCode,ShippingCountryCode,ShippingLatitude,ShippingLongitude,ShippingGeocodeAccuracy,ShippingAddress,Phone,Fax,Website,PhotoUrl,Industry,AnnualRevenue,NumberOfEmployees,Description,CurrencyIsoCode,OwnerId,CreatedDate,CreatedById,LastModifiedDate,LastModifiedById,SystemModstamp,LastActivityDate,LastViewedDate,LastReferencedDate,Jigsaw,JigsawCompanyId,AccountSource,SicDesc,AccountType__c,AccountLevel__c,AccountStatus__c,AccountID__c,MEContractorDiscipline__c,ContractStartDate__c,ContractEndDate__c,ContractDescription__c,Commission__c,CopyBillingAddressToShippingAddress__c,GradeElectrical__c,AccountSubType__c,Discipline__c,Grade__c,Company_Email__c,CompanyWillingness__c,PartnerType__c,ProductInterest__c,EnquiryType__c,GradeFire__c,GradeACMV__c,GradePS__c,GradeFullSpec__c,PartnerSource__c,Account_Type_for_ID__c,Account_Type_for_MY__c,DisciplineTypeMY__c,AccountCategory__c,CustomerId__c,Branch__c,CrmCustomerId__c,AccountTypeAll__c FROM Account";
        try {
            QueryResult qr = connection.query(soqlQuery);
            boolean done = false;

            if (qr.getSize() > 0) {
                System.out.println("\nLogged-in user can see "
                        + qr.getRecords().length + " Account records.");
                FileWriter fileWriter = new FileWriter("/Users/LiuFangGuo/Downloads/SalesForceAccount.data");
                while (!done) {
                    System.out.println("");
                    SObject[] records = qr.getRecords();
                    for (int i = 0; i < records.length; ++i) {
                        Account account = (Account) records[i];
                        String id = "N/A";
                        String isdeleted = "N/A";
                        String masterrecordid = "N/A";
                        String name = "N/A";
                        String type = "N/A";
                        String recordtypeid = "N/A";
                        String parentid = "N/A";
                        String billingstreet = "N/A";
                        String billingcity = "N/A";
                        String billingstate = "N/A";
                        String billingpostalcode = "N/A";
                        String billingcountry = "N/A";
                        String billingstatecode = "N/A";
                        String billingcountrycode = "N/A";
                        String billinglatitude = "N/A";
                        String billinglongitude = "N/A";
                        String billinggeocodeaccuracy = "N/A";
                        String billingaddress = "N/A";
                        String shippingstreet = "N/A";
                        String shippingcity = "N/A";
                        String shippingstate = "N/A";
                        String shippingpostalcode = "N/A";
                        String shippingcountry = "N/A";
                        String shippingstatecode = "N/A";
                        String shippingcountrycode = "N/A";
                        String shippinglatitude = "N/A";
                        String shippinglongitude = "N/A";
                        String shippinggeocodeaccuracy = "N/A";
                        String shippingaddress = "N/A";
                        String phone = "N/A";
                        String fax = "N/A";
                        String website = "N/A";
                        String photourl = "N/A";
                        String industry = "N/A";
                        String annualrevenue = "N/A";
                        String numberofemployees = "N/A";
                        String description = "N/A";
                        String currencyisocode = "N/A";
                        String ownerid = "N/A";
                        String createddate = "N/A";
                        String createdbyid = "N/A";
                        String lastmodifieddate = "N/A";
                        String lastmodifiedbyid = "N/A";
                        String systemmodstamp = "N/A";
                        String lastactivitydate = "N/A";
                        String lastvieweddate = "N/A";
                        String lastreferenceddate = "N/A";
                        String jigsaw = "N/A";
                        String jigsawcompanyid = "N/A";
                        String accountsource = "N/A";
                        String sicdesc = "N/A";
                        String accounttype__c = "N/A";
                        String accountlevel__c = "N/A";
                        String accountstatus__c = "N/A";
                        String accountid__c = "N/A";
                        String mecontractordiscipline__c = "N/A";
                        String contractstartdate__c = "N/A";
                        String contractenddate__c = "N/A";
                        String contractdescription__c = "N/A";
                        String commission__c = "N/A";
                        String copybillingaddresstoshippingaddress__c = "N/A";
                        String gradeelectrical__c = "N/A";
                        String accountsubtype__c = "N/A";
                        String discipline__c = "N/A";
                        String grade__c = "N/A";
                        String company_email__c = "N/A";
                        String companywillingness__c = "N/A";
                        String partnertype__c = "N/A";
                        String productinterest__c = "N/A";
                        String enquirytype__c = "N/A";
                        String gradefire__c = "N/A";
                        String gradeacmv__c = "N/A";
                        String gradeps__c = "N/A";
                        String gradefullspec__c = "N/A";
                        String partnersource__c = "N/A";
                        String account_type_for_id__c = "N/A";
                        String account_type_for_my__c = "N/A";
                        String disciplinetypemy__c = "N/A";
                        String accountcategory__c = "N/A";
                        String customerid__c = "N/A";
                        String branch__c = "N/A";
                        String crmcustomerid__c = "N/A";
                        String accounttypeall__c = "N/A";
                        if (account.getId() != null) {
                            id = account.getId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getIsDeleted() != null) {
                            isdeleted = account.getIsDeleted().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getMasterRecordId() != null) {
                            masterrecordid = account.getMasterRecordId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getName() != null) {
                            name = account.getName().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getType() != null) {
                            type = account.getType().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getRecordTypeId() != null) {
                            recordtypeid = account.getRecordTypeId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getParentId() != null) {
                            parentid = account.getParentId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingStreet() != null) {
                            billingstreet = account.getBillingStreet().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingCity() != null) {
                            billingcity = account.getBillingCity().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingState() != null) {
                            billingstate = account.getBillingState().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingPostalCode() != null) {
                            billingpostalcode = account.getBillingPostalCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingCountry() != null) {
                            billingcountry = account.getBillingCountry().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingStateCode() != null) {
                            billingstatecode = account.getBillingStateCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingCountryCode() != null) {
                            billingcountrycode = account.getBillingCountryCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingLatitude() != null) {
                            billinglatitude = account.getBillingLatitude().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingLongitude() != null) {
                            billinglongitude = account.getBillingLongitude().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingGeocodeAccuracy() != null) {
                            billinggeocodeaccuracy = account.getBillingGeocodeAccuracy().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBillingAddress() != null) {
                            billingaddress = account.getBillingAddress().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingStreet() != null) {
                            shippingstreet = account.getShippingStreet().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingCity() != null) {
                            shippingcity = account.getShippingCity().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingState() != null) {
                            shippingstate = account.getShippingState().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingPostalCode() != null) {
                            shippingpostalcode = account.getShippingPostalCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingCountry() != null) {
                            shippingcountry = account.getShippingCountry().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingStateCode() != null) {
                            shippingstatecode = account.getShippingStateCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingCountryCode() != null) {
                            shippingcountrycode = account.getShippingCountryCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingLatitude() != null) {
                            shippinglatitude = account.getShippingLatitude().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingLongitude() != null) {
                            shippinglongitude = account.getShippingLongitude().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingGeocodeAccuracy() != null) {
                            shippinggeocodeaccuracy = account.getShippingGeocodeAccuracy().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getShippingAddress() != null) {
                            shippingaddress = account.getShippingAddress().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getPhone() != null) {
                            phone = account.getPhone().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getFax() != null) {
                            fax = account.getFax().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getWebsite() != null) {
                            website = account.getWebsite().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getPhotoUrl() != null) {
                            photourl = account.getPhotoUrl().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getIndustry() != null) {
                            industry = account.getIndustry().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAnnualRevenue() != null) {
                            annualrevenue = account.getAnnualRevenue().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getNumberOfEmployees() != null) {
                            numberofemployees = account.getNumberOfEmployees().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getDescription() != null) {
                            description = account.getDescription().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCurrencyIsoCode() != null) {
                            currencyisocode = account.getCurrencyIsoCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getOwnerId() != null) {
                            ownerid = account.getOwnerId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCreatedDate() != null) {
                            createddate = account.getCreatedDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCreatedById() != null) {
                            createdbyid = account.getCreatedById().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getLastModifiedDate() != null) {
                            lastmodifieddate = account.getLastModifiedDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getLastModifiedById() != null) {
                            lastmodifiedbyid = account.getLastModifiedById().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getSystemModstamp() != null) {
                            systemmodstamp = account.getSystemModstamp().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getLastActivityDate() != null) {
                            lastactivitydate = account.getLastActivityDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getLastViewedDate() != null) {
                            lastvieweddate = account.getLastViewedDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getLastReferencedDate() != null) {
                            lastreferenceddate = account.getLastReferencedDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getJigsaw() != null) {
                            jigsaw = account.getJigsaw().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getJigsawCompanyId() != null) {
                            jigsawcompanyid = account.getJigsawCompanyId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountSource() != null) {
                            accountsource = account.getAccountSource().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getSicDesc() != null) {
                            sicdesc = account.getSicDesc().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountType__c() != null) {
                            accounttype__c = account.getAccountType__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountLevel__c() != null) {
                            accountlevel__c = account.getAccountLevel__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountStatus__c() != null) {
                            accountstatus__c = account.getAccountStatus__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountID__c() != null) {
                            accountid__c = account.getAccountID__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getMEContractorDiscipline__c() != null) {
                            mecontractordiscipline__c = account.getMEContractorDiscipline__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getContractStartDate__c() != null) {
                            contractstartdate__c = account.getContractStartDate__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getContractEndDate__c() != null) {
                            contractenddate__c = account.getContractEndDate__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getContractDescription__c() != null) {
                            contractdescription__c = account.getContractDescription__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCommission__c() != null) {
                            commission__c = account.getCommission__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCopyBillingAddressToShippingAddress__c() != null) {
                            copybillingaddresstoshippingaddress__c = account.getCopyBillingAddressToShippingAddress__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getGradeElectrical__c() != null) {
                            gradeelectrical__c = account.getGradeElectrical__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountSubType__c() != null) {
                            accountsubtype__c = account.getAccountSubType__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getDiscipline__c() != null) {
                            discipline__c = account.getDiscipline__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getGrade__c() != null) {
                            grade__c = account.getGrade__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCompany_Email__c() != null) {
                            company_email__c = account.getCompany_Email__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCompanyWillingness__c() != null) {
                            companywillingness__c = account.getCompanyWillingness__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getPartnerType__c() != null) {
                            partnertype__c = account.getPartnerType__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getProductInterest__c() != null) {
                            productinterest__c = account.getProductInterest__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getEnquiryType__c() != null) {
                            enquirytype__c = account.getEnquiryType__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getGradeFire__c() != null) {
                            gradefire__c = account.getGradeFire__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getGradeACMV__c() != null) {
                            gradeacmv__c = account.getGradeACMV__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getGradePS__c() != null) {
                            gradeps__c = account.getGradePS__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getGradeFullSpec__c() != null) {
                            gradefullspec__c = account.getGradeFullSpec__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getPartnerSource__c() != null) {
                            partnersource__c = account.getPartnerSource__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccount_Type_for_ID__c() != null) {
                            account_type_for_id__c = account.getAccount_Type_for_ID__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccount_Type_for_MY__c() != null) {
                            account_type_for_my__c = account.getAccount_Type_for_MY__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getDisciplineTypeMY__c() != null) {
                            disciplinetypemy__c = account.getDisciplineTypeMY__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountCategory__c() != null) {
                            accountcategory__c = account.getAccountCategory__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCustomerId__c() != null) {
                            customerid__c = account.getCustomerId__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getBranch__c() != null) {
                            branch__c = account.getBranch__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getCrmCustomerId__c() != null) {
                            crmcustomerid__c = account.getCrmCustomerId__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (account.getAccountTypeAll__c() != null) {
                            accounttypeall__c = account.getAccountTypeAll__c().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
//                        System.out.println(id + "`" + isdeleted + "`" + masterrecordid + "`" + name + "`" + type + "`" + recordtypeid + "`" + parentid + "`" + billingstreet + "`" + billingcity + "`" + billingstate + "`" + billingpostalcode + "`" + billingcountry + "`" + billingstatecode + "`" + billingcountrycode + "`" + billinglatitude + "`" + billinglongitude + "`" + billinggeocodeaccuracy + "`" + billingaddress + "`" + shippingstreet + "`" + shippingcity + "`" + shippingstate + "`" + shippingpostalcode + "`" + shippingcountry + "`" + shippingstatecode + "`" + shippingcountrycode + "`" + shippinglatitude + "`" + shippinglongitude + "`" + shippinggeocodeaccuracy + "`" + shippingaddress + "`" + phone + "`" + fax + "`" + website + "`" + photourl + "`" + industry + "`" + annualrevenue + "`" + numberofemployees + "`" + description + "`" + currencyisocode + "`" + ownerid + "`" + createddate + "`" + createdbyid + "`" + lastmodifieddate + "`" + lastmodifiedbyid + "`" + systemmodstamp + "`" + lastactivitydate + "`" + lastvieweddate + "`" + lastreferenceddate + "`" + jigsaw + "`" + jigsawcompanyid + "`" + accountsource + "`" + sicdesc + "`" + accounttype__c + "`" + accountlevel__c + "`" + accountstatus__c + "`" + accountid__c + "`" + mecontractordiscipline__c + "`" + contractstartdate__c + "`" + contractenddate__c + "`" + contractdescription__c + "`" + commission__c + "`" + copybillingaddresstoshippingaddress__c + "`" + gradeelectrical__c + "`" + accountsubtype__c + "`" + discipline__c + "`" + grade__c + "`" + company_email__c + "`" + companywillingness__c + "`" + partnertype__c + "`" + productinterest__c + "`" + enquirytype__c + "`" + gradefire__c + "`" + gradeacmv__c + "`" + gradeps__c + "`" + gradefullspec__c + "`" + partnersource__c + "`" + account_type_for_id__c + "`" + account_type_for_my__c + "`" + disciplinetypemy__c + "`" + accountcategory__c + "`" + customerid__c + "`" + branch__c + "`" + crmcustomerid__c + "`" + accounttypeall__c);
//                        if (fName == null) {
//                            System.out.println("Contact " + (i + 1) + ": " + lName);
//                        } else {
//                            System.out.println("Contact " + (i + 1) + ": " + fName
//                                    + " " + lName);
//                        }
                        fileWriter.write(id + "`" + isdeleted + "`" + masterrecordid + "`" + name + "`" + type + "`" + recordtypeid + "`" + parentid + "`" + billingstreet + "`" + billingcity + "`" + billingstate + "`" + billingpostalcode + "`" + billingcountry + "`" + billingstatecode + "`" + billingcountrycode + "`" + billinglatitude + "`" + billinglongitude + "`" + billinggeocodeaccuracy + "`" + billingaddress + "`" + shippingstreet + "`" + shippingcity + "`" + shippingstate + "`" + shippingpostalcode + "`" + shippingcountry + "`" + shippingstatecode + "`" + shippingcountrycode + "`" + shippinglatitude + "`" + shippinglongitude + "`" + shippinggeocodeaccuracy + "`" + shippingaddress + "`" + phone + "`" + fax + "`" + website + "`" + photourl + "`" + industry + "`" + annualrevenue + "`" + numberofemployees + "`" + description + "`" + currencyisocode + "`" + ownerid + "`" + createddate + "`" + createdbyid + "`" + lastmodifieddate + "`" + lastmodifiedbyid + "`" + systemmodstamp + "`" + lastactivitydate + "`" + lastvieweddate + "`" + lastreferenceddate + "`" + jigsaw + "`" + jigsawcompanyid + "`" + accountsource + "`" + sicdesc + "`" + accounttype__c + "`" + accountlevel__c + "`" + accountstatus__c + "`" + accountid__c + "`" + mecontractordiscipline__c + "`" + contractstartdate__c + "`" + contractenddate__c + "`" + contractdescription__c + "`" + commission__c + "`" + copybillingaddresstoshippingaddress__c + "`" + gradeelectrical__c + "`" + accountsubtype__c + "`" + discipline__c + "`" + grade__c + "`" + company_email__c + "`" + companywillingness__c + "`" + partnertype__c + "`" + productinterest__c + "`" + enquirytype__c + "`" + gradefire__c + "`" + gradeacmv__c + "`" + gradeps__c + "`" + gradefullspec__c + "`" + partnersource__c + "`" + account_type_for_id__c + "`" + account_type_for_my__c + "`" + disciplinetypemy__c + "`" + accountcategory__c + "`" + customerid__c + "`" + branch__c + "`" + crmcustomerid__c + "`" + accounttypeall__c + "\n");

                    }

                    if (qr.isDone()) {
                        done = true;
                    } else {
                        qr = connection.queryMore(qr.getQueryLocator());
                    }
                }
                fileWriter.close();
            } else {
                System.out.println("No records found.");
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    private void querySampleTest() {
        String soqlQuery = "SELECT Id,WhoId,WhatId,WhoCount,WhatCount,Subject,Location,IsAllDayEvent,ActivityDateTime,ActivityDate,DurationInMinutes,StartDateTime,EndDateTime,Description,AccountId,OwnerId,CurrencyIsoCode,Type,IsPrivate,ShowAs,IsDeleted,IsChild,IsGroupEvent,GroupEventType,CreatedDate,CreatedById,LastModifiedDate,LastModifiedById,SystemModstamp,IsArchived,RecurrenceActivityId,IsRecurrence,RecurrenceStartDateTime,RecurrenceEndDateOnly,RecurrenceTimeZoneSidKey,RecurrenceType,RecurrenceInterval,RecurrenceDayOfWeekMask,RecurrenceDayOfMonth,RecurrenceInstance,RecurrenceMonthOfYear,ReminderDateTime,IsReminderSet,EventSubtype FROM Event";
        try {
            QueryResult qr = connection.query(soqlQuery);
            boolean done = false;

            if (qr.getSize() > 0) {
                System.out.println("\nLogged-in user can see "
                        + qr.getRecords().length + " Event records.");

                while (!done) {
                    System.out.println("");
                    SObject[] records = qr.getRecords();
                    for (int i = 0; i < records.length; ++i) {
                        Event event = (Event) records[i];
                        String id = "N/A";
                        String whoid = "N/A";
                        String whatid = "N/A";
                        String whocount = "N/A";
                        String whatcount = "N/A";
                        String subject = "N/A";
                        String location = "N/A";
                        String isalldayevent = "N/A";
                        String activitydatetime = "N/A";
                        String activitydate = "N/A";
                        String durationinminutes = "N/A";
                        String startdatetime = "N/A";
                        String enddatetime = "N/A";
                        String description = "N/A";
                        String accountid = "N/A";
                        String ownerid = "N/A";
                        String currencyisocode = "N/A";
                        String type = "N/A";
                        String isprivate = "N/A";
                        String showas = "N/A";
                        String isdeleted = "N/A";
                        String ischild = "N/A";
                        String isgroupevent = "N/A";
                        String groupeventtype = "N/A";
                        String createddate = "N/A";
                        String createdbyid = "N/A";
                        String lastmodifieddate = "N/A";
                        String lastmodifiedbyid = "N/A";
                        String systemmodstamp = "N/A";
                        String isarchived = "N/A";
                        String recurrenceactivityid = "N/A";
                        String isrecurrence = "N/A";
                        String recurrencestartdatetime = "N/A";
                        String recurrenceenddateonly = "N/A";
                        String recurrencetimezonesidkey = "N/A";
                        String recurrencetype = "N/A";
                        String recurrenceinterval = "N/A";
                        String recurrencedayofweekmask = "N/A";
                        String recurrencedayofmonth = "N/A";
                        String recurrenceinstance = "N/A";
                        String recurrencemonthofyear = "N/A";
                        String reminderdatetime = "N/A";
                        String isreminderset = "N/A";
                        String eventsubtype = "N/A";
                        if (event.getId() != null) {
                            id = event.getId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getWhoId() != null) {
                            whoid = event.getWhoId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getWhatId() != null) {
                            whatid = event.getWhatId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getWhoCount() != null) {
                            whocount = event.getWhoCount().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getWhatCount() != null) {
                            whatcount = event.getWhatCount().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getSubject() != null) {
                            subject = event.getSubject().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getLocation() != null) {
                            location = event.getLocation().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsAllDayEvent() != null) {
                            isalldayevent = event.getIsAllDayEvent().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getActivityDateTime() != null) {
                            activitydatetime = event.getActivityDateTime().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getActivityDate() != null) {
                            activitydate = event.getActivityDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getDurationInMinutes() != null) {
                            durationinminutes = event.getDurationInMinutes().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getStartDateTime() != null) {
                            startdatetime = event.getStartDateTime().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getEndDateTime() != null) {
                            enddatetime = event.getEndDateTime().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getDescription() != null) {
                            description = event.getDescription().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getAccountId() != null) {
                            accountid = event.getAccountId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getOwnerId() != null) {
                            ownerid = event.getOwnerId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getCurrencyIsoCode() != null) {
                            currencyisocode = event.getCurrencyIsoCode().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getType() != null) {
                            type = event.getType().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsPrivate() != null) {
                            isprivate = event.getIsPrivate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getShowAs() != null) {
                            showas = event.getShowAs().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsDeleted() != null) {
                            isdeleted = event.getIsDeleted().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsChild() != null) {
                            ischild = event.getIsChild().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsGroupEvent() != null) {
                            isgroupevent = event.getIsGroupEvent().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getGroupEventType() != null) {
                            groupeventtype = event.getGroupEventType().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getCreatedDate() != null) {
                            createddate = event.getCreatedDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getCreatedById() != null) {
                            createdbyid = event.getCreatedById().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getLastModifiedDate() != null) {
                            lastmodifieddate = event.getLastModifiedDate().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getLastModifiedById() != null) {
                            lastmodifiedbyid = event.getLastModifiedById().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getSystemModstamp() != null) {
                            systemmodstamp = event.getSystemModstamp().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsArchived() != null) {
                            isarchived = event.getIsArchived().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceActivityId() != null) {
                            recurrenceactivityid = event.getRecurrenceActivityId().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsRecurrence() != null) {
                            isrecurrence = event.getIsRecurrence().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceStartDateTime() != null) {
                            recurrencestartdatetime = event.getRecurrenceStartDateTime().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceEndDateOnly() != null) {
                            recurrenceenddateonly = event.getRecurrenceEndDateOnly().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceTimeZoneSidKey() != null) {
                            recurrencetimezonesidkey = event.getRecurrenceTimeZoneSidKey().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceType() != null) {
                            recurrencetype = event.getRecurrenceType().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceInterval() != null) {
                            recurrenceinterval = event.getRecurrenceInterval().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceDayOfWeekMask() != null) {
                            recurrencedayofweekmask = event.getRecurrenceDayOfWeekMask().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceDayOfMonth() != null) {
                            recurrencedayofmonth = event.getRecurrenceDayOfMonth().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceInstance() != null) {
                            recurrenceinstance = event.getRecurrenceInstance().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getRecurrenceMonthOfYear() != null) {
                            recurrencemonthofyear = event.getRecurrenceMonthOfYear().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getReminderDateTime() != null) {
                            reminderdatetime = event.getReminderDateTime().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getIsReminderSet() != null) {
                            isreminderset = event.getIsReminderSet().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }
                        if (event.getEventSubtype() != null) {
                            eventsubtype = event.getEventSubtype().toString().replaceAll("[\r\n]+", "&&").replaceAll("`", "");
                        }

                        System.out.println(id + "`" + whoid + "`" + whatid + "`" + whocount + "`" + whatcount + "`" + subject + "`" + location + "`" + isalldayevent + "`" + activitydatetime + "`" + activitydate + "`" + durationinminutes + "`" + startdatetime + "`" + enddatetime + "`" + description + "`" + accountid + "`" + ownerid + "`" + currencyisocode + "`" + type + "`" + isprivate + "`" + showas + "`" + isdeleted + "`" + ischild + "`" + isgroupevent + "`" + groupeventtype + "`" + createddate + "`" + createdbyid + "`" + lastmodifieddate + "`" + lastmodifiedbyid + "`" + systemmodstamp + "`" + isarchived + "`" + recurrenceactivityid + "`" + isrecurrence + "`" + recurrencestartdatetime + "`" + recurrenceenddateonly + "`" + recurrencetimezonesidkey + "`" + recurrencetype + "`" + recurrenceinterval + "`" + recurrencedayofweekmask + "`" + recurrencedayofmonth + "`" + recurrenceinstance + "`" + recurrencemonthofyear + "`" + reminderdatetime + "`" + isreminderset + "`" + eventsubtype);
//                        if (fName == null) {
//                            System.out.println("Contact " + (i + 1) + ": " + lName);
//                        } else {
//                            System.out.println("Contact " + (i + 1) + ": " + fName
//                                    + " " + lName);
//                        }


                    }

                    if (qr.isDone()) {
                        done = true;
                    } else {
                        qr = connection.queryMore(qr.getQueryLocator());
                    }
                }
            } else {
                System.out.println("No records found.");
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }
}