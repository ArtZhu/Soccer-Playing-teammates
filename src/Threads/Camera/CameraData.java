package Threads.Camera;
//Date: Mar 8th, 2016

import lejos.robotics.geometry.*;

import java.util.*;

import RobotMain.SP2_v2;


public class CameraData {
	private Rectangle2D curr = null;
	//Rectangle2D old = null;
	
	private int found_count = 0;

	public CameraData(){
	}
	
	public synchronized void addData(Rectangle2D rec){
		curr = rec;
		/*
		if(rec.getWidth() > 30)
			found_count++;
		if(rec == null || rec.getWidth() <= 30)
			found_count=0;
		
		if(found_count > 5){
			SP2_v2.negotiator.negotiate(String.valueOf(curr.getWidth()));
			found_count = 0;
		}
		*/
	}

	public synchronized Rectangle2D getData(){
		return curr;
	}
	
	public synchronized void clearData(){
		curr = null;
	}
}
