package Network.ClientServerMgr_server;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import Network.Interface.NetworkManager;
import Network.Util.*;


public class MimicMulticastServer implements Runnable {
	public static final int port = 8819;
	public static final String addr = "localhost";
	
	private DatagramSocket socket;
	
	/*	from NetworkManager Class	*/
	private List<Identity> player_list;
	private NetworkManager server_mgr;
	
	private boolean DEBUG = false;
	
	public MimicMulticastServer(List<Identity> _player_list, ClientServerMgr_server _server_mgr){	
		player_list = _player_list;
		server_mgr = _server_mgr;
	}

	@Override
	public void run() {

		while(true){			
			DatagramPacket dp = (DatagramPacket) server_mgr.receive();
			System.out.println("Server Received Something");
			try{
				Message m = (Message) Tools.convertToObject(dp.getData());

				String s = m.message;
				if(DEBUG)
					System.out.printf("Received String %s\n", s);
				int i = s.indexOf(Res.separator);
				String request = Tools.takeBefore(s);
				s = Tools.takeAfter(s);
				s = Tools.takeAfter(s, "/");

				// parsing 
				if(DEBUG)
					System.out.printf("Original Addr:port_%s\n", s);

				
				InetAddress source_addr = m.source_addr;//InetAddress.getByName(Tools.takeBefore(s));
				int source_port = m.source_port;//Integer.parseInt(Tools.takeAfter(s));
				
				if(DEBUG)
					System.out.printf("FROM %s:%d\n", source_addr, source_port);
				
				//cases
				Identity identity;
				switch(request){
				case Res.register_player_string:
					identity = new Identity(
							source_addr, 
							source_port);

					System.out.printf("Player with IP %s, port %d joined!\n", identity.addr.toString(), identity.port);
					player_list.add(identity);

					// Send confirmation
					if(DEBUG)
						System.out.printf("Sent confirmation register to %s:%d\n", source_addr.toString(), source_port);
					server_mgr.send(new Message(
								InetAddress.getLocalHost(), 8819,
								identity.addr, identity.port,
								Res.register_player_confirmation_string + Res.separator) ,
							identity.addr, identity.port);

					System.out.println(player_list);
					
					break;

				case Res.unregister_player_string:

					identity = new Identity(
							source_addr,
							source_port);

					System.out.printf("Player with IP %s, port %d quited!\n", identity.addr.toString(), identity.port);
					player_list.remove(identity);

					// Send confirmation
					if(DEBUG)
						System.out.printf("Sent confirmation unregister to %s:%d\n", source_addr.toString(), source_port);
					server_mgr.send(
							new Message(
									InetAddress.getLocalHost(), 8819,
									identity.addr, identity.port,
									Res.unregister_player_confirmation_string + Res.separator),
							identity.addr, identity.port);

					System.out.println(player_list);
					
					break;
					
				default:
					System.out.printf("\nBroadCasting %s\n from %s:%d\n to %s:%d\n", 
							m.message, m.source_addr, m.source_port, m.dest_addr, m.dest_port);
					if(m.dest_addr != null){
						server_mgr.send(m, m.dest_addr, m.dest_port);
						System.out.printf("to destination\n");
					}
					else{
						for(int j = 0; j < player_list.size(); j++){
							Identity id = player_list.get(j);
							server_mgr.send(m, id.addr, id.port);
							System.out.printf("Sent %s to %s:%d\n", m.message, id.addr, id.port);
						}
					}
				}	

			} catch (NumberFormatException e) {
				System.out.println("MimicMulticastServerThread invalid port number!");
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
}
