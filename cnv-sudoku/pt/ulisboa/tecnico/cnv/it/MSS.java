/*
 * Copyright 2012-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package pt.ulisboa.tecnico.cnv.it;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class MSS {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    static AmazonDynamoDB dynamoDB;
    static final String TABLE_NAME = "metrics";

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.ProfilesConfigFile
     * @see com.amazonaws.ClientConfiguration
     */
    public static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-east-1")
            .build();

                // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = createTable(TABLE_NAME, 1L, 1L,
        "key", "S");

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);

        // Describe our new table
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(TABLE_NAME);
        TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
        System.out.println("Table Description: " + tableDescription);
    }

    public static UpdateItemRequest UpdateMetricLogRequest(String key, Long dyn_method_count, Long fieldloadcount) {

        Map<String,AttributeValue> pkey = new HashMap<>();
        pkey.put("key",new AttributeValue().withS(key));

        Map<String,AttributeValueUpdate> updates = new HashMap<>();

        updates.put("dyn_method_count", new AttributeValueUpdate(new AttributeValue()
                .withN(Long.toString(dyn_method_count)), AttributeAction.PUT));
        updates.put("fieldloadcount", new AttributeValueUpdate(new AttributeValue()
                .withN(Long.toString(fieldloadcount)), AttributeAction.PUT));

        
        return new UpdateItemRequest(TABLE_NAME, pkey, updates);
    }

    public static UpdateItemRequest UpdateMetricFinalRequest(String key, Long dyn_method_count, Long fieldloadcount, String ts) {

        UpdateItemRequest request = UpdateMetricLogRequest(key, dyn_method_count, fieldloadcount);

        request.addAttributeUpdatesEntry("ts", new AttributeValueUpdate(new AttributeValue()
            .withS(ts), AttributeAction.PUT));
        request.addAttributeUpdatesEntry("running", new AttributeValueUpdate(new AttributeValue()
            .withBOOL(false), AttributeAction.PUT));
        
        return request;
    }

    public static void update(UpdateItemRequest request){
        try {
            UpdateItemResult updateItemResult = dynamoDB.updateItem(request);
            System.err.println(updateItemResult);
        } catch (AmazonServiceException expected) {
            // Failed the criteria as expected
            System.err.println("Error updating item!");
        }
    }

    public static void putMetric(String key, long dyn_method_count, long fieldloadcount, 
                                String ts, Long Un, String server, String label, 
                                String N1, Boolean running, String N2) {

        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("key", new AttributeValue(key));
        item.put("dyn_method_count", new AttributeValue().withN(Long.toString(dyn_method_count)));
        item.put("fieldloadcount", new AttributeValue().withN(Long.toString(fieldloadcount)));
        item.put("ts", new AttributeValue(ts));
        item.put("Un", new AttributeValue().withN(Long.toString(Un)));
        item.put("server", new AttributeValue(server));
        item.put("label", new AttributeValue(label));
        item.put("N1", new AttributeValue(N1));
        item.put("running", new AttributeValue().withBOOL(running));
        item.put("N2", new AttributeValue(N2));

        PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        System.err.println("Result: " + putItemResult);
    }

    private static CreateTableRequest createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
        String partitionKeyName, String partitionKeyType) {

        return createTable(tableName, readCapacityUnits, writeCapacityUnits, partitionKeyName, partitionKeyType, null, null);
    }

    private static CreateTableRequest createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
        String partitionKeyName, String partitionKeyType, String sortKeyName, String sortKeyType) {

        try {

            ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH)); // Partition
                                                                                                                 // key

            ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions
                .add(new AttributeDefinition().withAttributeName(partitionKeyName).withAttributeType(partitionKeyType));

            if (sortKeyName != null) {
                keySchema.add(new KeySchemaElement().withAttributeName(sortKeyName).withKeyType(KeyType.RANGE)); // Sort
                                                                                                                 // key
                attributeDefinitions
                    .add(new AttributeDefinition().withAttributeName(sortKeyName).withAttributeType(sortKeyType));
            }

            CreateTableRequest request = new CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                    .withWriteCapacityUnits(writeCapacityUnits));

            request.setAttributeDefinitions(attributeDefinitions);

            return request;
            // System.out.println("Issuing CreateTable request for " + tableName);
            // Table table = dynamoDB.createTable(request);
            // System.out.println("Waiting for " + tableName + " to be created...this may take a while...");
            // table.waitForActive();

        }
        catch (Exception e) {
            System.err.println("CreateTable request failed for " + tableName);
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    public static void deleteServerPuzzles(String server){
        // Scan items for movies with a year attribute greater than 1985
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();

        ArrayList<Puzzle> concludedPuzzles = new ArrayList<Puzzle>(); 
        Map<String, AttributeValue> key_ = new HashMap<String, AttributeValue>();

        scanFilter.put("running", 
                new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withBOOL(false)));

        scanFilter.put("server", 
        new Condition()
            .withComparisonOperator(ComparisonOperator.EQ.toString())
            .withAttributeValueList(new AttributeValue().withS(server)));
            

        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        //System.out.println("Result: " + scanResult);

        for(Map<String,AttributeValue> item : scanResult.getItems()){
                // delete items
                String key = item.get("key").getS();
                key_.put("key", new AttributeValue().withS(key));
                DeleteItemRequest deleteItemRequest = new DeleteItemRequest(TABLE_NAME, key_);
                DeleteItemResult deleteItemResult = dynamoDB.deleteItem(deleteItemRequest);
                System.err.println("Deleted: " + deleteItemResult);
        }
    }

    public static HashMap<String,HashMap<Long,Puzzle>> getRunningPuzzles(){
        // Scan items for movies with a year attribute greater than 1985
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();

        HashMap<String,HashMap<Long,Puzzle>> activePuzzles = new HashMap<String,HashMap<Long,Puzzle>>(); 
        
        scanFilter.put("running", 
                new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withBOOL(true)));

        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        //System.out.println("Result: " + scanResult);

        for(Map<String,AttributeValue> item : scanResult.getItems()){
                Long id = new Long(0);
                Long fieldLoad = Long.parseLong(item.get("fieldloadcount").getN());
                Long methodcount = Long.parseLong(item.get("dyn_method_count").getN());
                Long un = Long.parseLong(item.get("Un").getN());
                String n1 = item.get("N1").getS();
                String label = item.get("label").getS();
                String strat ="";
                String server = item.get("server").getS();
                String timestamp = item.get("ts").getS();
                String key = item.get("key").getS();
                try{
                    String[] t = key.split("-"); 
                    id = Long.valueOf(t[t.length-1]);
                    strat = t[1];
                } catch(java.lang.NumberFormatException e){
                    e.printStackTrace();
                }
                // think about this code (backwards compatility with old requests)
                Puzzle puzzle = new Puzzle(label,methodcount,fieldLoad,strat,n1,un,timestamp,id);


                if(!activePuzzles.containsKey(server)){
                    activePuzzles.put(server,new HashMap<Long,Puzzle>());
                }
                activePuzzles.get(server).put(id,puzzle);
        }

        return activePuzzles;

    }

        public static ArrayList<Puzzle> getConcludedPuzzles(String ts){
        // Scan items for movies with a year attribute greater than 1985
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();

        ArrayList<Puzzle> concludedPuzzles = new ArrayList<Puzzle>(); 
        
        scanFilter.put("running", 
                new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withBOOL(false)));

        scanFilter.put("ts", 
        new Condition()
            .withComparisonOperator(ComparisonOperator.GT.toString())
            .withAttributeValueList(new AttributeValue().withS(ts)));
            

        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        //System.out.println("Result: " + scanResult);

        for(Map<String,AttributeValue> item : scanResult.getItems()){
                Long id = new Long(0);
                Long fieldLoad = Long.parseLong(item.get("fieldloadcount").getN());
                Long methodcount = Long.parseLong(item.get("dyn_method_count").getN());
                Long un = Long.parseLong(item.get("Un").getN());
                String n1 = item.get("N1").getS();
                String label = item.get("label").getS();
                String strat ="";
                String server = item.get("server").getS();
                String timestamp = item.get("ts").getS();
                String key = item.get("key").getS();
                try{
                    String[] t = key.split("-"); 
                    id = Long.valueOf(t[t.length-1]);
                    strat = t[1];
                } catch(java.lang.NumberFormatException e){
                    e.printStackTrace();
                }
                Puzzle puzzle = new Puzzle(label,methodcount,fieldLoad,strat,n1,un,timestamp,id);

                concludedPuzzles.add(puzzle);
        }

        return concludedPuzzles;

    }



    public static void main(String[] args) throws Exception {
        init();

        try {

            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = createTable(TABLE_NAME, 1L, 1L,
            "key", "S");

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(TABLE_NAME);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // Add an item

            // putMetric("SUDOKU_PUZZLE_9x9_02-BFS-ubuntu-bionic/127.0.1.1-3", 1234, 5678, "", "169",
                                                        // "ubuntu-bionic/127.0.1.1", "SUDOKU_PUZZLE_16x16_02", "16", true, "16");

            // update(UpdateMetricLogRequest("SUDOKU_PUZZLE_16x16_02-BFS-ubuntu-bionic/127.0.1.1-3", 1338, 1339));

            // update(UpdateMetricFinalRequest("SUDOKU_PUZZLE_9x9_02-BFS-ubuntu-bionic/127.0.1.1-3", 2002, 2001, "agora"));

            // // Scan items for movies with a year attribute greater than 1985
            // HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            // Condition condition = new Condition()
            //     .withComparisonOperator(ComparisonOperator.EQ.toString())
            //     .withAttributeValueList(new AttributeValue().withBOOL(true));
            // scanFilter.put("running", condition);

            // ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
            // ScanResult scanResult = dynamoDB.scan(scanRequest);
            // System.out.println("Result: " + scanResult);

            // dynamoDB.shutdown();

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            dynamoDB.shutdown();
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            dynamoDB.shutdown();
        }
    }

}


// MSS.update(MSS.UpdateMetricLogRequest(key, dyn, fieldloadcount);
// MSS.update(MSS.UpdateMetricFinalRequest(key, dyn, fieldloadcount, ts);