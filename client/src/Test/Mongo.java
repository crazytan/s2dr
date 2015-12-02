package Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.*;

/**
 * A helper class for accessing Mongodb
 */
public class Mongo {

    public static String getSignatureByUID(String uid) {
        MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:8889"));
        MongoDatabase db = client.getDatabase("s2dr");
        Document doc = db.getCollection("meta").find(eq("UID", uid)).first();
        return (String) doc.get("signature");
    }
}
