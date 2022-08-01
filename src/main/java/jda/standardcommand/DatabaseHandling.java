package jda.standardcommand;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

public class DatabaseHandling implements ICommand {

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        String accesskey = Config.get("MONGODBCLIENT");
        MongoClient mongo = new MongoClient("localhost");
        List<String> databases = mongo.getDatabaseNames();
        for (String dbName: databases){
            System.out.println("Database: " + dbName);
            DB db = mongo.getDB(dbName);
            Set<String> collections = db.getCollectionNames();
            for (String colName : collections){
                System.out.println("\t + Collection: " + colName);
            }
        }

        mongo.close();






    }

    @Override
    public String getName() {
        return "mongo";
    }

    @Override
    public String getHelp() {
        return "access data";
    }
}
