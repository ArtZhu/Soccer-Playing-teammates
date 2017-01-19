package Network.ClientServerMgr_server;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Network.Interface.NetworkManager;
import Network.Util.*;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class ClientServerMgr_server implements NetworkManager{
	private DatagramSocket socket_udp;
	private int port = 8819;
	
	private List<Identity> player_list;
	public List<Identity> get_player_list(){return player_list;}
	
	public ClientServerMgr_server(){
		player_list = Collections.synchronizedList(new ArrayList<Identity>());
		
		try {
			socket_udp = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized boolean send(Serializable serializable, InetAddress addr, int port) {
		return Tools.send(socket_udp, serializable, addr, port);
	}

	@Override
	public synchronized Object receive() {
		return Tools.receive(socket_udp);
	}

	@Override
	public synchronized boolean register() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean quit() {
		boolean b = true;
		for(Identity id: player_list){
			b = b && send(Res.server_quit_string, id.addr, id.port);
		}
		return b;
	}

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public void notifyChange(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Identity getIdentity() {
		// TODO Auto-generated method stub
		return null;
	}	
}
