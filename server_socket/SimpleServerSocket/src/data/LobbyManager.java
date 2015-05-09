package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LobbyManager {
	private final static LobbyManager instance = new LobbyManager();
	private final static Random sRandom = new Random();
    private final Set<Integer> mLobbyIds = new HashSet<Integer>();
    private List<Lobby> lobbies = new ArrayList<Lobby>();
    private List<User> users = new ArrayList<User>();
    
	
	private LobbyManager(){}
	
	public static LobbyManager getInstance(){
		return instance;
	}
	
	
	public void addLobby(Lobby nLobby){
		synchronized(lobbies){
			if(!lobbies.contains(nLobby)){
				lobbies.add(nLobby);
			}
		}
	}
	
	
	public void addUserToLobby(int lobbyId, User user){
		Iterator i = lobbies.iterator();
		boolean notFound = true;
		Lobby lob = null;
		while(i.hasNext() && notFound){
			Lobby next = (Lobby) i.next();
			if(lobbyId == next.getLobbyId()){
				lob = next;
				notFound = false;
			}
		}
		lob.addUser(user);
	}
	
	
	public List<Lobby> getLobbies(){
		return lobbies;
	}
	
	public User getUser(String uid){
		Iterator i = users.iterator();
		boolean notFound =true;
		while(i.hasNext() && notFound){
			User next = (User) i.next();
			if(uid.equals(next.getId())){
				return next;
			}
		}
		return null;
	}
	
	public User getFriend(String email){
		Iterator i = users.iterator();
		while(i.hasNext()){
			User next = (User) i.next();
			if(email.equals(next.getEmail())){
				return next;
			}
		}
		return null;
	}
	
	
	public void registerUser(User u){
		users.add(u);
	}
	
	public void unregisterUser(String uid){
		Iterator i = users.iterator();
		while(i.hasNext()){
			User next = (User) i.next();
			if(next.getId().equals(uid)){
				users.remove(next);
				return;
			}
		}
	}
	
	public int getUniqueLobbyId() {
        int nextRandom = sRandom.nextInt();
        while (mLobbyIds.contains(nextRandom)) {
            nextRandom = sRandom.nextInt();
        }
        return nextRandom;
    }
}
