# Chat Server

This project is an implementation of a simple chat server, using a RESTful protocol with JSON.

## Documentation

Documentation is included in the repository in the `doc` folder.

## Running the server

The server runs as a standalone Java application. By default, it uses port 50001,
but this can be set on the command line.
To run the server from the command line, enter: <code>java -jar chat-server.jar</code>

The program can be started in the `Server.java` file. 

## Endpoints

### Ping
- GET `/ping` returns true.
- POST `/ping` requires a token, returns true.

### User
- GET `/users` returns all registered usernames.
- GET `/users/online` returns all users that are online.
- POST `/user/register` requires a username and a password, returns the username.
- POST `/user/login` requires the username and password, returns the token.
- POST `/user/logout` requires the token, returns true.
- POST `/user/online` requires the token and the username of the person being online or not, returns true.

### Chat
- POST `/chat/send`requires a token (sender), username (recepient) or a chatroomId (in case it's a chatroom) and the message, returns true.
- POST `/chat/poll` requires a token, returns the message (either private or chatroom).

### Chatroom
- GET `/chatrooms` returns all chatrooms.
- POST `/chatrooms/create` requires a token (creator), list of chatroom members (clients), and a chatroom name. Returns the auto-generated id for the chatroom.
- POST `/chatrooms/join` requires a token (person joining) and chatroomId, returns the new list of members.
- POST `/chatrooms/leave` requires a token (person leaving) and chatroomId, returns true.
- POST `/chatrooms/delete` requires a token (person that created the chatroom) and chatroomId, returns true.
- POST `/chatrooms/users` requires a token (person joining) and chatroomId, returns the list of members.
  
