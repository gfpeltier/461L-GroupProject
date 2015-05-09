package processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ServerSocket.CcsClient;
import ServerSocket.CcsMessage;

public class UserSearchProcessor implements PayloadProcessor{

	@Override
	public void handleMessage(CcsMessage msg) {
		PseudoDao dao = PseudoDao.getInstance();
		String search = msg.getPayload().get("email");
		//List<String> userMatches = dao.getAllRegistrationIdsForAccount(search);
		//CcsClient.logger.log(Level.INFO, "Number of user matches: " + userMatches.size());
		List<String> matches = dao.getAllRegistrationIdsForAccount(search);
		if(matches != null && !matches.isEmpty()){
			CcsClient client = CcsClient.getInstance();
	        String msgId = dao.getUniqueMessageId();
	        Map<String, String> newPayload = new HashMap<String, String>();
	        newPayload.put("result", "user_found");
	        String jsonRequest = 
	                CcsClient.createJsonMessage(
	                        msg.getFrom(), 
	                        msgId, 
	                        newPayload, 
	                        null, 
	                        null, // TTL (null -> default-TTL) 
	                        false);
	        client.send(jsonRequest);
	        //client.logger.log(Level.INFO, "Successful find: " + jsonRequest);
	        Map<String, String> requestPayload = new HashMap<String, String>();
	        requestPayload.put("request_type", "location");
	        requestPayload.put("requester", msg.getPayload().get("fromName"));
	        String jsonRequestToOther = 
	        		CcsClient.createJsonMessage(
	        				matches.get(0), 
	        				msgId, 
	        				requestPayload, 
	        				null, 
	        				null, 
	        				false);
	        client.send(jsonRequestToOther);
	        //client.logger.log(Level.INFO, "Successful request: " + jsonRequestToOther);
		}else{
			CcsClient client = CcsClient.getInstance();
			client.logger.log(Level.INFO, "No user found protocol");
	        String msgId = dao.getUniqueMessageId();
	        Map<String, String> newPayload = new HashMap<String, String>();
	        newPayload.put("result", "no_match");
	        String jsonRequest = 
	                CcsClient.createJsonMessage(
	                        msg.getFrom(), 
	                        msgId, 
	                        newPayload, 
	                        null, 
	                        null, // TTL (null -> default-TTL) 
	                        false);
	        client.send(jsonRequest);
		}
        
		
	}

}
