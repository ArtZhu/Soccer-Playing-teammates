package __Network.NoLaptop;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import Network.Interface.ElectionParticipant;
import Network.Interface.NetworkManager;
import Network.Util.Identity;
import Network.Util.Tools;

public class Player implements NetworkManager, ElectionParticipant{
	private DatagramSocket socket_udp;
	private Identity id;
	//private InetAddress server_addr;
	//private int server_port;
	
	private boolean is_primary = false;
	
	public Player(){//InetAddress _server_addr, int _server_port){
		try {
			socket_udp = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		try {
			id = new Identity(InetAddress.getLocalHost(), socket_udp.getLocalPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		//server_addr = _server_addr;
		//server_port = _server_port;		
	}

	@Override
	public synchronized boolean send(Serializable serializable, InetAddress addr, int port) {
		return Tools.send(socket_udp,
				serializable, 
				addr, 
				port);
	}

	@Override
	public Object receive() {
		DatagramPacket receive_packet = Tools.receive(socket_udp);
		return Tools.convertToObject(receive_packet.getData());
	}

	@Override
	public boolean register() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean quit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrimary() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void notifyChange(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean elect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Identity getIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

}
