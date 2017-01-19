package Threads.Camera;
//Date: Feb 15, 2016

import java.lang.Thread.State;
import java.util.List;

import RobotMain.SP2_v2;
import lejos.hardware.device.*;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.I2CException;
import lejos.robotics.geometry.*;
import lejos.utility.Delay;

public class CameraThread implements Runnable{
	private NXTCam cam;
	private CameraData data;
	private int delay;
	private boolean isOn = true;
	
	
	public CameraThread(NXTCam _cam, CameraData _data, int _delay){
		cam   = _cam;
		data  = _data;
		delay = _delay;
		
		cam.enableTracking(true);
	}

	public synchronized boolean isOn(){
		return isOn;
	}
	
	public synchronized void off(){
		isOn = false;
	}
	
	public synchronized void on(){
		isOn = true;
	}
	
	@Override
	public void run() {
		cam.setTrackingMode(NXTCam.OBJECT_TRACKING);
		int ct=0;
		while(true){
			if (isOn){
				//Delay.msDelay(1000);
				try{	
					Rectangle2D rec = cam.getRectangle(0);

					LCD.clear(3);
					if(cam.getNumberOfObjects() != 0){
						data.addData(rec);
						//LCD.drawString("ct:"+(ct++)+" Width=" + rec.getWidth(), 0, 3);
						//LCD.drawString("Width=" + rec.getWidth(), 0, 3);
						Thread.yield();
					}else{
						//LCD.drawString("ct: "+(ct++)+" See Nothing", 0, 3);
						//LCD.drawString("See Nothing", 0, 3);
						data.addData(null);
						Thread.yield();
					}
				}
				
				catch(I2CException e){
					LCD.drawString("(Exception) See nothing", 0, 3);
				}
			}
			else{
				LCD.drawString("Not On", 0, 3);
				// NOT ON
				synchronized(SP2_v2.getStateChangeLock()){
					try {
						SP2_v2.getStateChangeLock().wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(0*SP2_v2.camera_delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
		}

	}

}
