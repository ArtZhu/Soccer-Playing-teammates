package Network.ClientServerMgr_client;

import java.io.*;
import java.net.*;

import Network.Interface.NetworkManager;
import Network.Util.Identity;
import Network.Util.Message;
import Network.Util.Res;
import Network.Util.Tools;
import RobotMain.SP2_v2;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class ClientServerMgr_client implements NetworkManager{
	private DatagramSocket socket_udp;
	private InetAddress server_addr;
	private int server_port;
	
	private boolean is_primary = false;
	
	private Identity id;
	
	public ClientServerMgr_client(InetAddress _server_addr, int _server_port){
		try {
			socket_udp = new DatagramSocket();
			id = new Identity(Tools.trunc_addr(InetAddress.getLocalHost()), socket_udp.getLocalPort());
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		server_addr = _server_addr;
		server_port = _server_port;		

		
		SP2_v2.net_id = this.getIdentity();
		SP2_v2.local_addr = SP2_v2.net_id.addr;
		SP2_v2.local_port = SP2_v2.net_id.port;
		
		register();
		
	}

	@Override
	public boolean send(Serializable serializable, InetAddress addr, int port) {
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
		String s = Res.register_player_string + Res.separator + id.toString();
		Message msg = new Message(
				SP2_v2.local_addr,	SP2_v2.local_port,
				null, -1,
				s);

		try {
			send(msg, InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//LCD.clear(4);
		//LCD.drawString("awaiting", 0, 4);
		Message receive = (Message) receive();
		LCD.clear(4);
		LCD.drawString("got message", 0, 4);
		return receive.message.contains(Res.register_player_confirmation_string);
	}

	@Override
	public synchronized boolean quit() {
		Identity id = null;
		try {
			id = new Identity(InetAddress.getLocalHost(), socket_udp.getLocalPort());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String s = Res.unregister_player_string + Res.separator + id.toString();
		Message msg = new Message(
				SP2_v2.local_addr,	SP2_v2.local_port,
				null, -1,
				s);
		
		try {
			send(msg, InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message receive = (Message) receive();
		return receive.message.contains(Res.unregister_player_confirmation_string);
	}

	@Override
	public boolean isPrimary() {
		return is_primary;
	}

	@Override
	public void notifyChange(String msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public Identity getIdentity() {
		return id;
	}
}
