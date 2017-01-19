package Threads;

import java.util.List;

import Network.ClientServerMgr_client.ListenThread_client;
import RobotMain.SP2_v2;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class UltrasonicThread implements Runnable {
	private NXTUltrasonicSensor sonic;
	private UltrasonicData data;
	private int grab_dist = 18; // 20 works too....
	private boolean waiting = false;
	private boolean isOn = false;
	
	private boolean isShooting = false;
	public synchronized void setShooter(){ isShooting = true; }
	public synchronized boolean Shooter(){ return isShooting; }
	
	public UltrasonicThread(UltrasonicData data, NXTUltrasonicSensor sonic) {
		this.data  = data;
		this.sonic = sonic;
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void run(){
		SampleProvider sonicSampleProvider = sonic.getDistanceMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
		while (true) {
			if (isOn){
				//System.out.println("In UltaSonic Thread");
				
				try {
					sonicSampleProvider.fetchSample(s1, 0);
				}
				catch (Exception ex) {}
				
				data.setSonicValue((int)(s1[0]*100));
				
				//Delay.msDelay(50);
				
				// NEW CODE!?!? NOt even sure it works. 
				if ( s1[0]*100 < 15){
					if(!Shooter()){
						LCD.drawString("Ultra_sonic Val:"+(int)(s1[0]*100),0,7);

						SP2_v2.setState(SP2_v2.STATE_waiting);
						ListenThread_client.setIArrived();
					}else{
						SP2_v2.setState(SP2_v2.STATE_shooting);
					}
				}
				Thread.yield();
			}
			else{
				// NOT ON
				Delay.msDelay(500);
				synchronized(SP2_v2.getStateChangeLock()){
					try {
						SP2_v2.getStateChangeLock().wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Thread.yield();
			}
			

			try {
				Thread.sleep(SP2_v2.camera_delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
		}
	}
}
