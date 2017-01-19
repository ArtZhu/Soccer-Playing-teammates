package Network.Imp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Network.ClientServerMgr_client.ClientServerMgr_client;
import Network.Interface.NetworkManager;

public class StartRobot {
	
	public static void main(String[] args){
		try {
			NetworkManager network_mgr = new ClientServerMgr_client(
					InetAddress.getByName("127.0.0.1"), 8819
					);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){}
	}
}
