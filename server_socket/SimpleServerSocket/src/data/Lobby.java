package data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Lobby {

	private int id;
	private User user1;
	private User user2;

	public Lobby(int mId){
		id = mId;
		user1 = null;
		user2 = null;
	}
	
	public void setFirstUser(User u1){
		user1 = u1;
	}
	
	public void setSecondUser(User u2){
		user2 = u2;
	}
	
	public ArrayList<User> getLobbyMembers(){
		ArrayList<User> members = new ArrayList<User>();
		if(user1 != null){
			members.add(user1);
		}
		if(user1 != null){
			members.add(user2);
		}
		return members;
	}
	
	public int getLobbyId(){
		return id;
	}
	
	public void addUser(User u){
		if(user1 == null){
			user1 = u;
		}else if(user2 == null){user2 = u;}
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof Lobby)){return false;}
		Lobby l = (Lobby) o;
		return id == l.getLobbyId();
	}
}
