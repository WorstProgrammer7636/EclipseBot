package jda.standardcommand.School;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetStudentInfo implements ICommand {
    private final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final String TOKENS_DIRECTORY_PATH = "tokens";
    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GetStudentInfo.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        String student = "";
        if (ctx.getArgs().isEmpty()){
            ctx.getChannel().sendMessage("Please type the name of a student you want to search for").queue();
            return;
        } else if (ctx.getArgs().size() > 2){
            ctx.getChannel().sendMessage("Please enter the first and last name of the student you want to search for. Example: " +
                    "?studentinfo John Doe").queue();
            return;
        } else {
            student = ctx.getArgs().get(0) + " " + ctx.getArgs().get(1);
        }
        System.out.println(student);

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        /**
         * control panel
         */
        final String spreadsheetId = Config.get("SPREADSHEETID");
        final String range = Config.get("SPREADSHEETRANGE");


        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();


        List<List<Object>> values = response.getValues();
        ArrayList<ArrayList<String>> stringValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++){
            ArrayList<String> studentRow = new ArrayList<>();
            stringValues.add(i, studentRow);
            for (int j = 0; j < values.get(i).size(); j++){
                stringValues.get(i).add(String.valueOf(values.get(i).get(j)));
            }
        }

        if (values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            try {
                int studentIndex = 0;
                for (int i = 0; i < stringValues.size(); i++){
                    if (stringValues.get(i).get(0).trim().equalsIgnoreCase(student)){
                        studentIndex = i;
                        break;
                    }
                }

                if (studentIndex == 0){
                    ctx.getChannel().sendMessage("That student doesn't exist on our database!").queue();
                    return;
                }
                String fallClasses = "Period 1: " + stringValues.get(studentIndex).get(1) + "\nPeriod 2: "
                        + stringValues.get(studentIndex).get(2) + "\nPeriod 3: " +
                        stringValues.get(studentIndex).get(3) + "\nPeriod 4: " + stringValues.get(studentIndex).get(4) + "\nHomeroom: " +
                        stringValues.get(studentIndex).get(5);

                String springClasses = "Period 1: " + stringValues.get(studentIndex).get(6) + "\nPeriod 2: "
                        + stringValues.get(studentIndex).get(7) + "\nPeriod 3: " +
                        stringValues.get(studentIndex).get(8) + "\nPeriod 4: " + stringValues.get(studentIndex).get(9);
                EmbedBuilder studentInfo = new EmbedBuilder();
                studentInfo.addField("STUDENT NAME", stringValues.get(studentIndex).get(0), true);
                studentInfo.addField("FALL CLASSES", fallClasses, true);
                studentInfo.addField("SPRING CLASSES", springClasses, true);
                studentInfo.setDescription("[Not in the database? Add your name and classes here!](" +
                        "https://docs.google.com/spreadsheets/d/1jpQPXNnInJMPh7DJMQ4IOwoEj1b7AzCBKBVbcoieDZs/edit#gid=218595227)");
                studentInfo.setColor(Color.BLUE);

                ctx.getChannel().sendMessage(studentInfo.build()).queue();
            } catch (Exception exception){
                ctx.getChannel().sendMessage("That student was not found!").queue();
            }
        }
    }

    @Override
    public String getName() {
        return "studentinfo";
    }

    @Override
    public String getHelp() {
        return "return student info(classes, periods, and teachers) by name";
    }
}
