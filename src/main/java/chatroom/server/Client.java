package chatroom.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class represents a client, from the perspective of the server. A client
 * is a user with a currently valid token. We record here the username, the token
 * and an array of messages that need to be sent to the user.
 */
public class Client {
	private static final Logger logger = Logger.getLogger("");
	private static final ArrayList<Client> clients = new ArrayList<>();

	private final String username;
	private final String token;
	private final List<Message> messages = new ArrayList<>();
	private final List<ChatroomMessage> chatroomMessages = new ArrayList<>();
	private final List<Integer> chatrooms = new ArrayList<>();
	//private static ArrayList<GroupChat> groups = new ArrayList<>();
	//private static Integer idOfLastGroupChat = 0;
	private Instant lastUsage = Instant.now();

	// Messages pending for this user. Chatroom is null for direct messages
	// from a user. The username is the sending user. The message is obvious.
	private record Message(String username, String message) {}
	private record ChatroomMessage(Integer groupId, String username, String message) {}

	//public record GroupChat(String groupName, Integer groupId, List<String> clients, String creator) {}

	/**
	 * Add a new client to our list of active clients.
	 */
	public static void add(String username, String token) {
		synchronized (clients) {
			clients.add(new Client(username, token));
		}
	}

	/**
	 * Create a new GroupChat with auto-generated id. Return the id.
	 */
//	public static Integer createGroupChat(String groupName, List<String> clients, String creator) {
//		synchronized (idOfLastGroupChat) {
//			idOfLastGroupChat++;
//			GroupChat groupChat = new GroupChat(groupName, idOfLastGroupChat, clients, creator);
//			synchronized (groups) {
//				groups.add(groupChat);
//			}
//			return idOfLastGroupChat;
//		}
//	}

	/**
	 * Join an existing GroupChat by inserting the groupId and username.
	 */
//	public static GroupChat joinGroupChat(int groupId, String username) {
//		synchronized (groups) {
//			for (GroupChat groupChat : groups) {
//				if (groupChat.groupId == groupId) {
//					synchronized (clients) {
//						groupChat.clients.add(username);
//					}
//					return groupChat;
//				}
//			}
//		}
//		return null;
//	}

	/**
	 * Leave an existing group chat. Insert groupId and username of the person leaving the group.
	 */
//	public static void leaveGroupChat(int groupId, String username) {
//		synchronized (groups) {
//			for (GroupChat groupChat : groups) {
//				if (groupChat.groupId == groupId) {
//					groupChat.clients.removeIf(user -> user.equals(username));
//				}
//			}
//		}
//	}

	/**
	 * Delete a group chat only if the call comes from the creator and has a valid groupId.
	 */
//	public static void deleteGroupChat(int groupId, String creator) {
//		synchronized (groups) {
//			for (GroupChat groupChat : groups) {
//				if (groupChat.groupId == groupId && groupChat.creator.equals(creator)) {
//					groups.removeIf(group -> group.groupId == groupId);
//				}
//			}
//		}
//	}

	/**
	 * Remove a client (e.g., when they logout)
	 */
	public static void remove(String token) {
		synchronized (clients) {
			clients.removeIf(c -> c.token.equals(token));
		}
	}

	/**
	 * Returns a client, found by username
	 */
	public static Client findByUsername(String username) {
		synchronized (clients) {
			for (Client c : clients) {
				if (c.username.equals(username)) return c;
			}
		}
		return null;
	}

//	public static Client findByGroupId(int groupId){
//		synchronized (clients){
//			for(Client client : clients){
//				synchronized (groups){
//					for(GroupChat groupChat : client.groups){
//						if(groupChat.groupId == groupId) return client;
//					}
//				}
//			}
//		}
//		return null;
//	}

	/**
	 * Returns a client, found by token
	 */
	public static Client findByToken(String token) {
		synchronized (clients) {
			for (Client c : clients) {
				if (c.token.equals(token)) return c;
			}
		}
		return null;
	}

	/**
	 * Clean up old clients -- called by cleanup thread
	 */
	public static void cleanupClients() {
		synchronized (clients) {
			Instant expiry = Instant.now().minusSeconds(3600); // Expiry one hour
			logger.fine("Cleanup clients: " + clients.size() + " clients registered");
			clients.removeIf(c -> c.lastUsage.isBefore(expiry));
			logger.fine("Cleanup clients: " + clients.size() + " clients registered");
		}
	}

	/**
	 * Return a list of all clients
	 */
	public static List<String> listClients() {
		return clients.stream().map(c -> c.username).collect(Collectors.toList());
	}

	/**
	 * Create a new client object, communicating over the given socket. Immediately
	 * start a thread to receive messages from the client.
	 */
	public Client(String username, String token) {
		this.username = username;
		this.token = token;
	}

	public String getName() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public Instant getLastUsage() {
		return lastUsage;
	}

	// Called when the client takes an action
	private void updateLastUsage() {
		this.lastUsage = Instant.now();
	}

	/**
	 * Send a message to this client.
	 */
	public void send(String username, String message) {
		synchronized (messages) {
			messages.add(new Message(username, message));
		}
	}

	/**
	 * Retrieve messages for this client
	 */
	public synchronized JSONArray getMessages() {
		JSONArray jsonMessages = new JSONArray();
		for (Message msg : messages) {
			JSONObject jsonMsg = (new JSONObject())
					.put("username", msg.username);
			jsonMsg.put("message", msg.message);
			jsonMessages.put(jsonMsg);
		}
		messages.clear();

		for (ChatroomMessage chatroomMessage : chatroomMessages){
			JSONObject jsonMsg = (new JSONObject())
					.put("username", chatroomMessage.username);
			jsonMsg.put("chatroomName", Chatroom.findByChatroomId(chatroomMessage.groupId).getChatroomName());
			jsonMsg.put("message", chatroomMessage.message);
			jsonMessages.put(jsonMsg);
		}
		chatroomMessages.clear();

		updateLastUsage();
		return jsonMessages;
	}

	public void addToChatroom (int chatroomId){
		synchronized (chatrooms){
			chatrooms.add(chatroomId);
		}
	}

	public void addChatroomMessage (Integer groupId, String username, String message) {
		synchronized (chatroomMessages){
			chatroomMessages.add(new ChatroomMessage(groupId, username, message));
		}
	}

//	public static ArrayList<GroupChat> getGroups() {
//		return groups;
//	}

//	public List<String> getGroupChatMembers(int groupId) {
//		synchronized (groups){
//			for (GroupChat groupChat : groups) {
//				if (groupChat.groupId == groupId) {
//					return groupChat.clients;
//				}
//			}
//		}
//		return null;
//	}

//	private String groupIdToGroupNameMapper(int groupId){
//		synchronized (groups){
//			for (GroupChat groupChat : groups){
//				if(groupChat.groupId == groupId){
//					return groupChat.groupName;
//				}
//			}
//		}
//		return null;
//	}
}
