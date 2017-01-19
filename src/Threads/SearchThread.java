package Threads;

import java.util.ArrayList;
import java.util.List;

import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Rectangle2D;
import lejos.utility.Delay;
import Network.ClientServerMgr_client.ListenThread_client;
import Prof.DifferentialPilot;
import RobotMain.SP2_v2;

public class SearchThread implements Runnable{
	private DifferentialPilot pilot;
	
	private boolean isOn = false;
	public boolean found = false;
	private final double std_center = 100.0;
	
	
	public SearchThread(DifferentialPilot _pilot){
		pilot = _pilot;
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
		List<Double> width_s = new ArrayList<Double>();
		
		while(true){
			
			if (isOn){
				//LCD.drawString(i + ": In Search Thread", 0, 1);
				//SP2_v2.state=1; // forceably switch to tracking
				//SP2_v2.remain=false;
				//Delay.msDelay(2000);
				//System.out.println("Swithcing state");
				//found = true;
				int count = 0;
				while (!found) { // keep searching
					//Delay.msDelay(1000);
					//Thread.yield();
					//pilot.rotate(5);
					//LCD.clear();
					//LCD.drawString("Incoming camera reading, X pos of ball:",0,0);
					//LCD.drawString("Incoming camera reading, width of ball:",0,1);
					try{
						/*
						 * Note to self:
						 * These methods below do a good job of printing out information
						 * about what the camera sees to the user, but its not very consistent.
						 * We need to develop a good condition for the found flag to be set to true...
						 * Maybe we could ask signoirle about this??? 
						 * Anyway, the camera oftern reports that it is seeing RED when
						 * there is really nothing there, and it hard to tell the robot to 
						 * stop rotating when it sees red quickly. I.e. its hard to know
						 * whether or not there is a ball there when the camera reports that it 
						 * sees red. 
						 */
						
						/**
						 * TRY TO RECEIVE MESSAGE FROM OTHER ROBOT, IF HE HAS IT,
						 * CHANGE STATE TO MOVING
						 */
						Rectangle2D rec = SP2_v2.cam_data.getData();
						//LCD.drawString("No data " + (rec == null), 0, 4);
						double width = rec.getWidth();
						//LCD.drawString("x:"+(std_center-SP2_v2.cam_data.getData().getCenterX())+"",0,2);
						//LCD.drawString("width:"+width+"",0,5);
						
						count++;
						width_s.add(width);
						if ( width < 40 ){
							count = 0;
							width_s.clear();
						}
						/* If consistently seeing a red object, set found == true. 
						 * This isnt that great because its hard to get 5 readings
						 * so quickly because the robot has to keep moving...
						 * A solution would be to have the robot turn VERY VERY slowly...
						 * but this is bad...
						 * 
						 * 
						 * 4/9/2016 -> It actually kinda works well
						 * If we just have the robot turn very slowly...
						 */
					if ( count >= 5 || width > 90)
							found = true;
					}
					catch(NullPointerException e){
						//LCD.clear();
						LCD.drawString("Dont see the ball",0,6);
						count = 0;
					}
				
				}
				pilot.stop();
				LCD.clear();
				LCD.drawString("FOUND THE BALL",0,1);
				Delay.msDelay(1000);
				
				Double sum = 0.0;
				for(Double d: width_s)
					sum +=d;
				sum = sum/5;
				
				//ListenThread_client.negotiating_size = (int) sum.doubleValue();
				SP2_v2.setState(SP2_v2.STATE_waiting);
				
				/*
				LCD.clear(5);
				LCD.drawString(String.format("average = %f", sum), 0, 5);
				LCD.clear(6);
				LCD.drawString("negotiating", 0, 6);
				*/
				SP2_v2.negotiator.negotiate(String.valueOf((int) sum.doubleValue()));
				
				Delay.msDelay(SP2_v2.camera_delay);
				try {
					Thread.sleep(SP2_v2.camera_delay);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				SP2_v2.setState(SP2_v2.STATE_waiting);
				off(); // NEED TO TURN MYSELF OFF, OR I MIGHT KEEP TELLING 
				// THE MAIN FSM TO KEEP SWITCH THREADS ON AND OFF
				LCD.clear();
			}
			else{
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

			Delay.msDelay(SP2_v2.camera_delay);
			try {
				Thread.sleep(SP2_v2.camera_delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
		}
	}
}
