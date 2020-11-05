package eu.revamp.practice.manager.impl;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import eu.revamp.practice.util.misc.Logger;
import lombok.Getter;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;

import java.util.Collections;

@Getter
public class MongoManager extends Manager {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoManager(ManagerHandler managerHandler) {
        super(managerHandler);

        establishConnection();
    }

    private void establishConnection() {
        String ip = managerHandler.getPlugin().getConfig().getString("mongo.ip");
        int port = managerHandler.getPlugin().getConfig().getInt("mongo.port");
        String database = managerHandler.getPlugin().getConfig().getString("mongo.database");
        boolean usePassword = managerHandler.getPlugin().getConfig().getBoolean("mongo.use-password");
        String username = managerHandler.getPlugin().getConfig().getString("mongo.username");
        String password = managerHandler.getPlugin().getConfig().getString("mongo.password");

        try {
            if (usePassword) {
                mongoClient = new MongoClient(new ServerAddress(ip, port), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())));
            } else {
                mongoClient = new MongoClient(new ServerAddress(ip, port));
            }
            mongoDatabase = mongoClient.getDatabase(database);

            mongoClient.getAddress();
            Logger.success("Successfully established Mongo connection.");
        } catch (Exception ex) {
            Logger.error("Could not establish Mongo connection.");
        }
    }

    public boolean collectionExists(String collection) {
        boolean exists = false;
        for (String collections : mongoDatabase.listCollectionNames()) {
            if (collection.equalsIgnoreCase(collections)) {
                exists = true;
                break;
            }
        }
        return exists;
    }
}
