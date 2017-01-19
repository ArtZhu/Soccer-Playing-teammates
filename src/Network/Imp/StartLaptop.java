package Network.Imp;

import Network.ClientServerMgr_server.ClientServerMgr_server;
import Network.ClientServerMgr_server.MimicMulticastServer;

public class StartLaptop {
	
	//Just how we want to start the server
	public static void main(String[] args){
		ClientServerMgr_server server = new ClientServerMgr_server();
		
		Thread s = new Thread(new MimicMulticastServer(server.get_player_list(), server));
		s.setDaemon(true);
		s.start();
		
		try{
			s.join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}
