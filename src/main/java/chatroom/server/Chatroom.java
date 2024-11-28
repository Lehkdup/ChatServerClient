package chatroom.server;

import java.util.ArrayList;
import java.util.List;

public class Chatroom {

    private final String chatroomName;
    private final Integer chatroomId;
    private static Integer idOfLastGroupChat = 0;
    private List<Client> clients;
    private Client creator;
    private static ArrayList<Chatroom> chatrooms = new ArrayList<>();
    //private List<Message> messages = new ArrayList<>();
    public record Message(String username, String message) {
    }

    public Chatroom(String chatroomName, List<Client> clients, Client creator) {
        this.chatroomName = chatroomName;
        this.clients = clients;
        this.creator = creator;
        synchronized (idOfLastGroupChat){
            chatroomId = ++idOfLastGroupChat;
            for(Client client : clients){
                client.addToChatroom(chatroomId);
            }
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
     * Join an existing chatroom by inserting the groupId and client reference.
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
     * Leave an existing chatroom. Insert groupId and client reference of the person leaving the group.
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
     * Delete a group chat only if the call comes from the creator and has a valid groupId.
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

    public static Chatroom findByChatroomId(int chatroomId){
        synchronized (chatrooms){
            for (Chatroom chatroom : chatrooms){
                if (chatroom.chatroomId == chatroomId) return chatroom;
            }
        }
        return null;
    }

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

//    public String chatroomIdToNameMapper (int chatroomId){
//        synchronized (chatrooms){
//            for (Chatroom chatroom : chatrooms){
//                if (chatroom.chatroomId == chatroomId) {
//                    return chatroom.chatroomName;
//                }
//            }
//        }
//        return null;
//    }

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

//    private void distributeChatroomMessage(String username, String message){
//        synchronized (clients){
//            for (Client client : clients){
//                client.addChatroomMessage(chatroomId, username, message);
//            }
//        }
//    }

//    public boolean clientHasChatroomMessage (Client client){
//        synchronized (messages){
//             boolean result = messages.stream()
//                    .flatMap(message -> chatrooms.stream()
//                            .filter(chatroom -> message.chatroomId == chatroom.chatroomId)
//                            .flatMap(chatroom -> chatroom.clients.stream()))
//                    .anyMatch(c -> c.getName().equals(client.getName()));
//             return result;
//        }
//    }

//    public List<Message> getMessages() {
//        return messages;
//    }
}
