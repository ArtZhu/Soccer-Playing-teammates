package Threads.Camera;
//Date: Feb 15, 2016

import java.util.LinkedList;
import java.util.List;

public class CamMotorData {
	private final int buf_size = 10;
	private LinkedList<Integer> buf = new LinkedList<Integer>();
	private Integer angle;
	
	public CamMotorData(){
		angle = 0;
	}
	
	public synchronized void put(int angle){
		angle = angle / 2;
		if(!(buf.size()<buf_size))
			buf.pop();
		buf.add(angle);
	}
	
	public synchronized LinkedList<Integer> getTrack(){
		return buf;
	}

}
