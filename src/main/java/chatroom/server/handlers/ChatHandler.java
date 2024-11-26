package chatroom.server.handlers;

import chatroom.server.Client;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatHandler extends Handler {
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
                    if (token == null || username == null || message == null) {
                        throw new Exception("Invalid parameters");
                    } else{
                        sendMessage(token, username, message, response, groupName);
                    }
                }
                case "/chat/poll" -> {
                    if (token == null) throw new Exception("Invalid parameters");
                    receiveMessages(token, response);
                }
                case "/chatrooms" -> {
                    if (token == null) throw new Exception("Invalid parameters");
                    getAllGroupChats(token);
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

    private void sendMessage(String token, String username, String message, HandlerResponse response, String groupName) throws Exception {
        boolean success = false;
        Client sender = Client.findByToken(token);
        if (sender == null) throw new Exception("Invalid token");
        Client recipient = Client.findByUsername(username);
        if (recipient != null) {
            recipient.send(sender.getName(), message, groupName);
            success = true;
        }
        response.jsonOut.put("send", success);
    }

    private void receiveMessages(String token, HandlerResponse response) throws Exception {
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("messages", client.getMessages());
    }

    private JSONArray getAllGroupChats(String token) throws Exception{
        JSONArray groupsArray = new JSONArray();
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        for(Client.GroupChat groupChat : client.getGroups()){
            groupsArray.put(groupChat);
        }
        return groupsArray;
    }

    private void createGroupChat(String token, String groupName, List<String> clients, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        int groupId = client.createGroupChat(groupName, clients, client.getName());
        response.jsonOut.put("groupId", groupId);
    }

    private void joinGroupChat(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("groupChat", client.joinGroupChat(groupId, client.getName()));
    }

    private void leaveGroupChat(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        client.leaveGroupChat(groupId, client.getName());
        response.jsonOut.put("leftGroup", true);
    }

    private void deleteGroupChat(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        client.deleteGroupChat(groupId, client.getName());
        response.jsonOut.put("groupChatdeleted", true);
    }

    private void listGroupChatMembers(String token, int groupId, HandlerResponse response) throws Exception{
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("groupChatMembers", client.getGroupChatMembers(groupId));
    }
}

