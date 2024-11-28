package chatroom.server;

import java.util.ArrayList;
import java.util.List;

public class Chatroom {

    private final String chatroomName;
    private final Integer chatroomId;
    private static Integer idOfLastChatroom = 0;
    private List<Client> clients;
    private Client creator;
    private static ArrayList<Chatroom> chatrooms = new ArrayList<>();
    public record Message(String username, String message) {
    }

    /**
     * Creates a new chatroom with auto-generated Id.
     */
    public Chatroom(String chatroomName, List<Client> clients, Client creator) {
        this.chatroomName = chatroomName;
        this.clients = clients;
        this.creator = creator;
        synchronized (idOfLastChatroom){
            chatroomId = ++idOfLastChatroom;
        }
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public Integer getChatroomId() {
        return chatroomId;
    }

    public List<Client> getClients() {
        return clients;
    }

    public Client getCreator() {
        return creator;
    }

    public static ArrayList<Chatroom> getChatrooms() {
        return chatrooms;
    }

    public static void add(Chatroom chatroom){
        synchronized (chatrooms){
            chatrooms.add(chatroom);
        }
    }

    /**
     * Join an existing chatroom by inserting the chatroomId and client reference.
     */
    public static List<String> join(int chatroomId, Client client){
        synchronized (chatrooms){
            for (Chatroom chatroom : chatrooms){
                if (chatroom.chatroomId == chatroomId){
                    chatroom.clients.add(client);
                    return chatroom.clients.stream()
                            .map(Client::getName)
                            .toList();
                }
            }
        }
        return null;
    }

    /**
     * Leave an existing chatroom. Insert chatroomId and client reference of the person leaving the chatroom.
     */
    public static void leaveChatroom(int chatroomId, Client client){
        synchronized (chatrooms){
            for (Chatroom chatroom : chatrooms){
                if (chatroom.chatroomId == chatroomId){
                    chatroom.clients.removeIf(clientToRemove ->
                            clientToRemove.getName().equals(client.getName()));
                }
            }
        }
    }

    /**
     * Delete a chatroom only if the call comes from the creator and has a valid chatroomId.
     */
    public static void deleteChatroom(int chatroomId, Client client){
        synchronized (chatrooms){
            for (Chatroom chatroom : chatrooms){
                if (chatroom.chatroomId == chatroomId && chatroom.creator.getName().equals(client.getName())){
                    chatrooms.removeIf(chatroomToRemove -> chatroomToRemove.chatroomId == chatroomId);
                }
            }
        }
    }

    /**
     * Get an instance of the Chatroom by inserting the chatroomId.
     */
    public static Chatroom findByChatroomId(int chatroomId){
        synchronized (chatrooms){
            for (Chatroom chatroom : chatrooms){
                if (chatroom.chatroomId == chatroomId) return chatroom;
            }
        }
        return null;
    }

    /**
     * Get the usernames of all members of a Chatroom.
     */
    public static List<String> getChatroomMembers (int chatroomId){
        synchronized (chatrooms){
            for (Chatroom chatroom : chatrooms){
                if (chatroom.chatroomId == chatroomId) {
                    return chatroom.clients.stream()
                            .map(Client::getName)
                            .toList();
                }
            }
        }
        return null;
    }

    /**
     * Distributes the message to all clients (members).
     */
    public void send(String username, String message){
        //synchronized (messages){
        //    messages.add(new Message(username, message));
        //}
        synchronized (clients){
            for (Client client : clients){
                client.addChatroomMessage(chatroomId, username, message);
            }
        }
    }
}
