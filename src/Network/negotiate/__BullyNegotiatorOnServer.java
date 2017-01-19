package Network.negotiate;

import Network.ClientServerMgr_server.ClientServerMgr_server;
import Network.Interface.Negotiator;
import Network.Interface.NetworkManager;
import Network.Util.Res;

public class __BullyNegotiatorOnServer {//implements Negotiator{
	ClientServerMgr_server network_mgr;
	int count = 0;
	private String winner = "";
	private int winning_value = 10000;
	
	public __BullyNegotiatorOnServer(ClientServerMgr_server _network_mgr){
		network_mgr = _network_mgr;
	}
	

	public String negotiate(String msg) {		
		count ++;
		int size = network_mgr.get_player_list().size();

		int i = msg.indexOf(Res.separator);
		Integer value = Integer.parseInt(msg.substring(0, i));
		String Identity = msg.substring(i+1);
		
		if(winning_value > value){
			winning_value = value;			
			winner = Identity;
		}
		
		if(count == size){
			String s = winner;
			winner = "";
			count = 1;
			winning_value = 10000;
			
			return winner;
		}
		
		return null;
	}
	
}
