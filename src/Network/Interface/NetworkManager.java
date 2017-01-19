package Network.Interface;

import java.net.*;
import java.io.*;

import Network.Util.Identity;

public interface NetworkManager {
	public boolean send(Serializable serializable, InetAddress addr, int port);
	public Object receive();
	
	//participate
	public boolean register();
	public boolean quit();
	
	public boolean isPrimary();
	
	public Identity getIdentity();
	
	public void notifyChange(String msg);
}