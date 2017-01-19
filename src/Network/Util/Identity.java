package Network.Util;

import java.io.Serializable;
import java.net.InetAddress;

public class Identity implements Serializable{
	public final int port;
	public final InetAddress addr;
	
	public Identity(InetAddress _addr, int _port){
		port = _port;
		addr = _addr;
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof Identity))
			return false;
		return 	((Identity) o).port == this.port &&
				((Identity) o).addr.equals(this.addr);	
	}
	
	@Override
	public String toString(){
		return addr.toString() + Res.separator + String.valueOf(port);
	}
}
