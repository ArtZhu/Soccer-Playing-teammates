package Network.Util;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {
	public final InetAddress 	source_addr;
	public final int			source_port;	
	public final String			message;
	public final InetAddress 	dest_addr;
	public final int			dest_port;
	
	public Message(
			InetAddress _source_addr,
			int			_source_port,
			InetAddress _dest_addr,
			int			_dest_port,
			String		_message
			){
		source_addr = 	_source_addr;
		source_port = 	_source_port;
		dest_addr 	= 	_dest_addr;
		dest_port	=	_dest_port;
		message		=	_message;
	}
}
