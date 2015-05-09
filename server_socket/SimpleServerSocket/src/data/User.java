package data;

import java.net.Socket;

public class User {
	
	private String name;
	private String id;
	private String email;
	private int lobby;
	private double locLat;
	private double locLng;
	private Socket socket;
	
	public User(String mName, String mEmail, String mId, Socket sock){
		name = mName;
		id = mId;
		socket = sock;
		email = mEmail;
	}
	
	public void setEmail(String mEmail){
		email = mEmail;
	}
	
	public String getEmail(){
		return email;
	}
	
	public void setLobby(int lob){
		lobby = lob;
	}
	
	public void setLat(double mLat){
		locLat = mLat;
	}
	
	public void setLng(double mLng){
		locLng = mLng;
	}
	
	public String getName(){
		return name;
	}
	
	public String getId(){
		return id;
	}
	
	public Socket getSocket(){
		return socket;
	}
	
}
