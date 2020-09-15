package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
//import java.lang.*;
/*import com.google.firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;*/
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.SetOptions;

import com.google.cloud.firestore.WriteResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    /*private final Firestore db;

    Function(Firestore db) {
    this.db = db;
  }*/
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception{
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("objectId");
        final String oid = request.getBody().orElse(query);
        System.out.println("OID : " + oid);
        if (oid == null) {
            System.out.println("Null OID :" + HttpStatus.BAD_REQUEST);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a valid oid on the query string or in the request body").build();
        } else {
            String uuidDate=updateDocumentArray(oid);
            System.out.println("uuidDate : " + uuidDate);
            return request.createResponseBuilder(HttpStatus.OK).body(uuidDate).build();
        }
    }
    /** Update array fields in a document. **/
  public static String updateDocumentArray(String oid) throws Exception {
    /*if(firebase.apps.length()==0){
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).setDatabaseUrl("https://barqat-786kmr.firebaseio.com/").build();

    // Initialize the default app
    FirebaseApp defaultApp = FirebaseApp.initializeApp(options);
    }
    db=FirestoreClient.getFirestore();*/
    // [START fs_initialize_project_id]
    FirestoreOptions firestoreOptions =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId("barqat-786kmr")
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build();
            Firestore db = firestoreOptions.getService();
    // [END fs_initialize_project_id]
    // [START fs_update_document_array]
    DocumentReference usersRef = db.collection("users").document(oid);
    String uuidDate=getUuidDate();
    // asynchronously retrieve the document
    ApiFuture<DocumentSnapshot> future = usersRef.get();
    // ...
    // future.get() blocks on response
    DocumentSnapshot document = future.get();
    if (document.exists()) {
        System.out.println("Document data: " + document.getData());
        if (document.get("contacts_temp")!=null){
            // Atomically add a new region to the "contacts_temp" array field.
            ApiFuture<WriteResult> arrayUnion = usersRef.update("contacts_temp",
            FieldValue.arrayUnion(uuidDate));
            System.out.println("Update time : " + arrayUnion.get());
        }else{
            //asynchronously update doc, create the document if missing
            Map<String, Object> update = new HashMap<>();
            update.put("contacts_temp", Arrays.asList(uuidDate));
            ApiFuture<WriteResult> writeResult =
                db
                    .collection("users")
                    .document(oid)
                    .set(update, SetOptions.merge());
            // ...
            System.out.println("Update time : " + writeResult.get().getUpdateTime());
        }
    // [END fs_update_document_array]
    } else {
    System.out.println("No such document!");
    Map<String, Object> update = new HashMap<>();
        update.put("contacts_temp", Arrays.asList(uuidDate));
        ApiFuture<WriteResult> writeResult =
            db
                .collection("users")
                .document(oid)
                .set(update, SetOptions.merge());
        // ...
        System.out.println("Update time : " + writeResult.get().getUpdateTime());

    }
    return uuidDate;
}
  public static String getUuidDate() throws Exception {
    ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
    int min=(utc.getHour()*60+utc.getMinute())/10;
    String min_slot=Integer.toString(min);
    String repeated = new String(new char[3-min_slot.length()]).replace("\0", "0");
    String date=utc.toOffsetDateTime().toString().substring(0,10)+repeated+min_slot;
    UUID uuid=UUID.randomUUID(); //Generates random UUID
    String uuid1=uuid.toString();
    return uuid1.substring(0,9)+"0786"+uuid1.substring(13,uuid1.length())+date; 
  }
}
