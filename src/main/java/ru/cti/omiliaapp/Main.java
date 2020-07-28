package main.java.ru.cti.omiliaapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.java.ru.cti.omiliaapp.actions.GetURL;
import main.java.ru.cti.omiliaapp.actions.Request;

import javax.security.auth.callback.TextInputCallback;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        final String CONNECTION_URL = "connection_url";
        final String PROCESS_URL = "process_url";
        final String PROPERTIES_FILE = "app.properties";
        String utterance = null;
        Scanner scanner = new Scanner(System.in);
        String dialogId = null;

        //Прочитать app.properties для получения connection_url
//        GetURL appProperties = new GetURL();
//        String connectionURL = appProperties.getURL(CONNECTION_URL, PROPERTIES_FILE);
//        String processURL = appProperties.getURL(PROCESS_URL, PROPERTIES_FILE);

//        //Сделать запрос "Start new dialog"
//        Request request = new Request();
//
//        //Вывести на экран сообщения бота после Start new dialog
//        JsonObject response = request.makeRequest(connectionURL, utterance, dialogId);
//        showBotMessages(response);
//        dialogId = getDialogId(response);
///////////////////////////////////////////////////////////////////////
        //Читаем содержимое файла из resources
        ArrayList<String> sb = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader("src/resources/testSet.txt"))) {

            String line;
            while ((line = br.readLine()) != null) {
                sb.add(line);
                sb.add(System.lineSeparator());
            }
        }
///////////////////////////////////////////////////////////////////////

        //В цикле общаться с ботом
//        while (true) {
//        utterance = scanner.nextLine();
//        response = request.makeRequest(processURL, utterance, dialogId);
//        showBotMessages(response);


        for (int i=0; i < sb.size(); i+=1){
            GetURL appProperties = new GetURL();
            String connectionURL = appProperties.getURL(CONNECTION_URL, PROPERTIES_FILE);
            String processURL = appProperties.getURL(PROCESS_URL, PROPERTIES_FILE);
            //Сделать запрос "Start new dialog"
            Request request = new Request();

            //Вывести на экран сообщения бота после Start new dialog
            JsonObject response = request.makeRequest(connectionURL, utterance, dialogId);
            showBotMessages(response, utterance);
            dialogId = getDialogId(response);

            utterance = sb.get(i).trim();
//            if (!utterance.equals(""))
//                System.out.println("В работе UTTERANCE = " + utterance);

            response = request.makeRequest(processURL, utterance, dialogId);
            showBotMessages(response, utterance);

//            TimeUnit.MILLISECONDS.sleep(2);
            //закрыть чатик
            JsonElement actionType =  response.getAsJsonObject("action");
            if (!actionType.getAsJsonObject().get("type").toString().replaceAll("\"", "").equals("TRANSFER"))
                request.makeRequest(processURL, "[hup]", dialogId);
            dialogId = null;
            utterance = null;
        }
    }

    private static String getDialogId(JsonObject response) {
        return response.get("dialogId").toString();
    }

    private static void showBotMessages(JsonObject response, String utterance) throws IOException {
        String concatMessage = "";
        System.out.println("1 " + response.toString());
//        System.out.println("2 " + response.get("semantic_interpretation"));
//        System.out.println("3 " + response.getAsJsonObject("semantic_interpretation").get("n_best"));

        if (response.has("semantic_interpretation") == true) {
            if (response.getAsJsonObject("semantic_interpretation").has("n_best") == true) {
//                if (response.getAsJsonObject("semantic_interpretation").getAsJsonObject("n_best").has("entities") == true) {
//        System.out.println(response.getAsJsonArray("fields").get(4));
                boolean exist = false;
                for (int i = 0; i < response.getAsJsonObject("semantic_interpretation").getAsJsonArray("n_best").size(); i++) {
                    JsonObject messagePlay = (JsonObject) response.getAsJsonObject("semantic_interpretation").getAsJsonArray("n_best").get(i);
                    for (int k = 0; k <  messagePlay.getAsJsonArray("entities").size(); k++) {
                        JsonObject field = (JsonObject) messagePlay.getAsJsonArray("entities").get(k);
//                        System.out.println(utterance + " ----> " + field.get("name" ) + " = " + field.getAsJsonArray("instances").get(0).getAsJsonObject().get("value"));
                        if (!field.get("name").toString().equals("\"askPaymentIncomeMethod\"")){
                            System.out.println(utterance + " ---> " + field.get("name" ) + " = " + field.getAsJsonArray("instances").get(0).getAsJsonObject().get("value"));
//                            System.out.println(field.getAsJsonArray("instances").get(0).getAsJsonObject().get("value"));
//                            System.out.println("\n");
                            exist = true;
                        }
                    }
                    if (exist == false)
                        System.out.println(utterance + " ---> NO_MATCH" );
                }
            }
        }

        JsonElement actionType =  response.getAsJsonObject("action");
//        System.out.println(actionType.getAsJsonObject().get("type"));
        JsonArray messagesJSONArray = response.getAsJsonObject("action").getAsJsonObject("message").getAsJsonArray("prompts");
        for (int i = 0; i < messagesJSONArray.size(); i++) {
            JsonObject messagePlay = (JsonObject) messagesJSONArray.get(i);
            concatMessage += messagePlay.get("content").toString().replaceAll("\"", "") + " ";
        }
//        System.out.println(concatMessage);
//        if (actionType.getAsJsonObject().get("type").toString().replaceAll("\"", "").equals("TRANSFER")) {
//                        System.exit(11);
//        }
    }
}
