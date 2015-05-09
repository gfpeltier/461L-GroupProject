package SimpleSocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import data.Constants;
import data.LobbyManager;
import data.User;

public class SimpleServerSocket {
	
	public static final Logger logger = Logger.getLogger(SimpleServerSocket.class.getName());
	
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static InputStreamReader inputStreamReader;
	private static BufferedReader bufferedReader;
	private static String message;
	
	
	
	private static void friendNotFound(User requester){
		JSONObject json = new JSONObject();
		try {
			json.put("action", Constants.ACTION_USER_SEARCH_RESPONSE);
			json.put("response", "no_match_found");
			Socket uSock = requester.getSocket();
			BufferedWriter os = new BufferedWriter(new OutputStreamWriter(uSock.getOutputStream()));
			os.write(json.toString());
			os.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private static void friendFound(User requester, User friend){
		
	}
 
	public static void main(String[] args) {
		
		try {
			serverSocket = new ServerSocket(4444);
 
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4444");
		}
 
		logger.log(Level.INFO, "Server started. Listening to the port 4444");
 
		while (true) {
			try {
 
				clientSocket = serverSocket.accept(); // accept the client connection
				inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
				bufferedReader = new BufferedReader(inputStreamReader); // get the client message
				message = bufferedReader.readLine();
				inputStreamReader.close();
				logger.log(Level.INFO, message);
				
				
				if(!(message == null)){
					JSONObject obj = new JSONObject(message);
					
					String action = obj.getString("action");
					
					if(action.equals(Constants.ACTION_REGISTER)){
						String name = obj.getString("name");
						String email = obj.getString("email");
						String uid = obj.getString("id");
						User newUser = new User(name, email, uid, clientSocket);
						LobbyManager.getInstance().registerUser(newUser);
						logger.log(Level.INFO, "REGISTERING new user with id: " + uid);
					}else if(action.equals(Constants.ACTION_UNREGISTER)){
						String uid = obj.getString("id");
						LobbyManager.getInstance().unregisterUser(uid);
					}else if(action.equals(Constants.ACTION_USER_SEARCH)){
						logger.log(Level.INFO, "Looking for friend");
						String requester = obj.getString("requester");
						String friend = obj.getString("friend_email");
						User uFriend = LobbyManager.getInstance().getFriend(friend);
						if(uFriend != null){
							friendFound(LobbyManager.getInstance().getFriend(requester), uFriend);
						}else{
							friendNotFound(LobbyManager.getInstance().getFriend(requester));
						}
					}
				}else{logger.log(Level.SEVERE, "message from device is null!");}
				
				
				
 
				
				
				//clientSocket.close();
 
				clientSocket.setKeepAlive(true);
				
				
			} catch (IOException ex) {
				System.out.println("Problem in message reading");
			} catch(JSONException e){
				logger.log(Level.SEVERE, "EXCEPTION while parsing: " + e.toString());
			}
		}
 
	}
}
