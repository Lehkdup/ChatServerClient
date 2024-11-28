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
            response.jsonOut.put("groupChats", getAllGroupChats());
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
        String groupName = readString(JSONin, "groupName");
        List<String> clients = readList(JSONin, "clients");
        Integer groupId = readInt(JSONin, "groupId");

        // If anything at all goes wrong, we throw an exception and return an error
        try {
            switch (mapping) {
                case "/chat/send" -> {
                    if (token == null || message == null) {
                        throw new Exception("Invalid parameters");
                    } else if(username == null && groupId == null){
                        throw new Exception("Define a username or a groupId to send the message");
                    }
                    else{
                        sendMessage(token, username, message, response, groupId);
                    }
                }
                case "/chat/poll" -> {
                    if (token == null) throw new Exception("Invalid parameters");
                    receiveMessages(token, response);
                }
                case "/chatroom/create" -> {
                    if (token == null || groupName == null || clients == null) {
                        throw new Exception("Invalid parameters");
                    } else {
                        createGroupChat(token, groupName, clients, response);
                    }
                }
                case "/chatroom/join" -> {
                    if (token == null || groupId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        joinGroupChat(token, groupId, response);
                    }
                }
                case "/chatroom/leave" -> {
                    if (token == null || groupId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        leaveGroupChat(token, groupId, response);
                    }
                }
                case "/chatroom/delete" -> {
                    if (token == null || groupId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        deleteGroupChat(token, groupId, response);
                    }
                }
                case "/chatroom/users" -> {
                    if (token == null || groupId == null){
                        throw new Exception("Invalid parameters");
                    } else {
                        listGroupChatMembers(token, groupId, response);
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

    private void sendMessage(String token, String username, String message, HandlerResponse response, Integer groupId) throws Exception {
        boolean success = false;
        Client sender = Client.findByToken(token);
        if (sender == null) throw new Exception("Invalid token");

        if (groupId == null){
            Client recipient = Client.findByUsername(username);
            recipient.send(sender.getName(), message);
            success = true;
        } else {
            Chatroom chatroom = Chatroom.findByChatroomId(groupId);
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

    private JSONArray getAllGroupChats() {
        JSONArray groupsArray = new JSONArray();
        for(Chatroom chatroom : Chatroom.getChatrooms()){
            JSONObject chatroomJson = new JSONObject()
                    .put("chatroomName", chatroom.getChatroomName());
                    chatroomJson.put("chatroomId", chatroom.getChatroomId());
                    chatroomJson.put("clients", new JSONArray(chatroom.getClients().stream()
                            .map(Client::getName)
                            .toList()));
                    chatroomJson.put("creator", chatroom.getCreator().getName());
            groupsArray.put(chatroomJson);
        }
        return groupsArray;
    }

    private void createGroupChat(String token, String groupName, List<String> usernames, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        List<Client> clients = new ArrayList<>();
        for (String s : usernames){
            clients.add(Client.findByUsername(s));
        }
        Chatroom chatroom = new Chatroom(groupName, clients, client);
        Chatroom.add(chatroom);
        int groupId = chatroom.getChatroomId();
        response.jsonOut.put("groupId", groupId);
    }

    private void joinGroupChat(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("groupChat", Chatroom.join(groupId, client));
    }

    private void leaveGroupChat(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        Chatroom.leaveChatroom(groupId, client);
        response.jsonOut.put("leftGroup", true);
    }

    private void deleteGroupChat(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        Chatroom.deleteChatroom(groupId, client);
        response.jsonOut.put("groupChatdeleted", true);
    }

    private void listGroupChatMembers(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("groupChatMembers", Chatroom.getChatroomMembers(groupId));
    }
}

