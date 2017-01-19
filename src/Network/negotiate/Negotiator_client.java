package Network.negotiate;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Network.ClientServerMgr_client.ListenThread_client;
import Network.Interface.Negotiator;
import Network.Util.Message;
import Network.Util.Res;
import RobotMain.SP2_v2;

public class Negotiator_client implements Negotiator{
	
	public Negotiator_client(){
		
	}
	
	public void negotiate(String size){
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(SP2_v2.server_addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		
		SP2_v2.network_mgr.send(
				new Message(
						SP2_v2.local_addr, SP2_v2.local_port,
						null, -1,			
						
				Res.request_to_go_string + Res.separator 
				+ size), 
				addr , SP2_v2.server_port);
		ListenThread_client.negotiating_size = Integer.parseInt(size);
	}
}
