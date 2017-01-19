package Network.Util;

import java.io.*;
import java.net.*;


public class Tools {
	
	public static final int buf_len = 1024;
	
	public static boolean send(DatagramSocket socket, Serializable s, InetAddress addr, int port){
		byte[] data = convertToByteArray(s);
		try {
			DatagramPacket send_packet = new DatagramPacket(
					data,
					data.length,
					addr,
					port
					);
			socket.send(send_packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static DatagramPacket receive(DatagramSocket socket){
		DatagramPacket receive_packet = new DatagramPacket(
				new byte[buf_len],
				buf_len);
		try {
			socket.receive(receive_packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return receive_packet;
	}
	
	public static byte[] convertToByteArray(Object o){
		byte[] data_arr = null;
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(o);
			data_arr = bos.toByteArray();
			bos.close(); oos.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return data_arr;
	}
	
	public static Object convertToObject(byte[] data){
		Object o = null;
		try{
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			o = ois.readObject();
			bis.close(); ois.close();
		}catch(IOException e1){
			e1.printStackTrace();
		}catch(ClassNotFoundException e2){
			e2.printStackTrace();
		}
		return o;
	}
	
	public static InetAddress trunc_addr(InetAddress addr) throws UnknownHostException{
		String s = addr.toString();
		int i = s.indexOf("/");
		s = s.substring(i+1);
		return InetAddress.getByName(s);
	}
	
	public static String takeBefore(String s, String separator){
		int i = s.indexOf(separator);
		return s.substring(0, i);
	}
	
	public static String takeAfter(String s, String separator){
		int i = s.indexOf(separator);
		return s.substring(i+1);
	}
	
	public static String takeBefore(String s){
		return takeBefore(s, Res.separator);
	}
	
	public static String takeAfter(String s){
		return takeAfter(s, Res.separator);
	}
}
