package chatroom.server.handlers;

import chatroom.server.Chatroom;
import chatroom.server.Client;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatHandler extends Handler {
    @Override
    protected void handleGet(HttpExchange httpExchange, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString();
        if (mapping.equals("/chatroom")){
            response.jsonOut.put("chatrooms", getAllChatrooms());
        } else {
            response.jsonOut.put("Error", "Invalid request");
        }
    }

    @Override
    protected void handlePost(HttpExchange httpExchange, JSONObject JSONin, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString(); // For this handler, will begin with "/user"

        // Read various strings that may be present (depending on the mapping)
        String username = readString(JSONin, "username");
        String message = readString(JSONin, "message");
        String token = readString(JSONin, "token");
        String chatroomName = readString(JSONin, "chatroomName");
        List<String> clients = readList(JSONin, "clients");
        Integer chatroomId = readInt(JSONin, "chatroomId");

        // If anything at all goes wrong, we throw an exception and return an error.
        try {
            switch (mapping) {
                case "/chat/send" -> {
                    if (token == null || message == null) {
                        throw new Exception("Invalid parameters");
                    } else if(username == null && chatroomId == null){
                        throw new Exception("Define a username or a chatroomId to send the message");
                    }
                    else{
                        sendMessage(token, username, message, response, chatroomId);
                    }
                }
                case "/chat/poll" -> {
                    if (token == null) throw new Exception("Invalid parameters");
                    receiveMessages(token, response);
                }
                case "/chatroom/create" -> {
                    if (token == null || chatroomName == null || clients == null) {
                        throw new Exception("Invalid parameters");
                    } else {
                        createChatroom(token, chatroomName, clients, response);
                    }
                }
                case "/chatroom/join" -> {
                    if (token == null || chatroomId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        joinChatroom(token, chatroomId, response);
                    }
                }
                case "/chatroom/leave" -> {
                    if (token == null || chatroomId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        leaveChatroom(token, chatroomId, response);
                    }
                }
                case "/chatroom/delete" -> {
                    if (token == null || chatroomId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        deleteChatroom(token, chatroomId, response);
                    }
                }
                case "/chatroom/users" -> {
                    if (token == null || chatroomId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        listChatroomMembers(token, chatroomId, response);
                    }
                }
                default -> {
                    throw new Exception("No such mapping");
                }
            }
        } catch (Exception e) {
            response.jsonOut.put("Error", e.getMessage());
        }
    }

    private void sendMessage(String token, String username, String message, HandlerResponse response, Integer chatroomId) throws Exception {
        boolean success = false;
        Client sender = Client.findByToken(token);
        if (sender == null) throw new Exception("Invalid token");

        if (chatroomId == null){
            Client recipient = Client.findByUsername(username);
            recipient.send(sender.getName(), message);
            success = true;
        } else {
            Chatroom chatroom = Chatroom.findByChatroomId(chatroomId);
            chatroom.send(sender.getName(), message);
            success = true;
        }
        response.jsonOut.put("send", success);
    }

    private void receiveMessages(String token, HandlerResponse response) throws Exception {
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("messages", client.getMessages());
    }

    private JSONArray getAllChatrooms() {
        JSONArray chatroomsArray = new JSONArray();
        for(Chatroom chatroom : Chatroom.getChatrooms()){
            JSONObject chatroomJson = new JSONObject()
                    .put("chatroomName", chatroom.getChatroomName());
                    chatroomJson.put("chatroomId", chatroom.getChatroomId());
                    chatroomJson.put("clients", new JSONArray(chatroom.getClients().stream()
                            .map(Client::getName)
                            .toList()));
                    chatroomJson.put("creator", chatroom.getCreator().getName());
            chatroomsArray.put(chatroomJson);
        }
        return chatroomsArray;
    }

    private void createChatroom(String token, String chatroomName, List<String> usernames, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        List<Client> clients = new ArrayList<>();
        for (String s : usernames){
            clients.add(Client.findByUsername(s));
        }
        Chatroom chatroom = new Chatroom(chatroomName, clients, client);
        Chatroom.add(chatroom);
        int chatroomId = chatroom.getChatroomId();
        response.jsonOut.put("chatroomId", chatroomId);
    }

    private void joinChatroom(String token, int chatroomId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("chatroom", Chatroom.join(chatroomId, client));
    }

    private void leaveChatroom(String token, int chatroomId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        Chatroom.leaveChatroom(chatroomId, client);
        response.jsonOut.put("leftChatroom", true);
    }

    private void deleteChatroom(String token, int chatroomId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        Chatroom.deleteChatroom(chatroomId, client);
        response.jsonOut.put("chatroomDeleted", true);
    }

    private void listChatroomMembers(String token, int chatroomId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("chatroomMembers", Chatroom.getChatroomMembers(chatroomId));
    }
}

