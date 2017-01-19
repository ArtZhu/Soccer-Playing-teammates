package Threads;

import java.util.LinkedList;

import RobotMain.Param;
import RobotMain.SP2_v2;
import Threads.*;
import Threads.Camera.CameraData;
import Network.ClientServerMgr_client.ListenThread_client;
//import lejos.robotics.navigation.DifferentialPilot;
import Prof.*;
import lejos.hardware.lcd.LCD;
import lejos.robotics.geometry.Rectangle2D;
import lejos.utility.Delay;

public class RobotHeadingThread_v2 implements Runnable{

	/**
	 * Control thread to point robot in direction of the ball
	 * 
	 */
	
	private int                 rotateSpeed = 10; /**Degrees per second... */
	private CameraData        	camera_data;
	private DifferentialPilot   pilot;
	//private EOPDData      _eopdData;
	private int                 distance, grabDist, turnRate; // devise some formula for this to insert into STEER
	private int                 camDelay;

	private double 				rotate_factor = 1.2;
	private int 				std_angle = 70;
	private double 				jerky_range = 4.0;
	
	private int 				std_middle = 100;
	private int					vision_range = 60;
	
	public static boolean SHOOTING = false;
	private boolean isOn = false;
	
	public RobotHeadingThread_v2(DifferentialPilot _pilot, CameraData _camera_data, int _camDelay){//EOPDData eopdData, int camDealy){
		camera_data     = _camera_data;
		pilot    = _pilot;
		//camDelay = _camDelay;
		//_eopdData = eopdData;
		pilot.setRotateSpeed(rotateSpeed); 
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
		//_pilot.setAngularSpeed(SoccerPlayer.stdRotateSpeed);
		pilot.setLinearSpeed(SP2_v2.stdSpeed / 2);
		
		//_pilot.setRotateSpeed(SoccerPlayer.stdRotateSpeed);
		pilot.setTravelSpeed(SP2_v2.stdSpeed / 2);
		
		
		
		boolean cubic_model = false;

		final double Kp = 1.2;
		final double Kd = 0.085;
		
		double error = 0;
		double last_error = 0;
		double error_diff;
		
		double turnrate;
		int count = 0;
		while(true){
			//if we lost the ball when "search = true";
			/*
			if(SP2_v2.ultrasonic_data.getValue() < Param.ball_in_control_dist){
				TrackThreadFactory.interrupt();
				if(!SHOOTING){
					ListenThread_client.i_arrived = true;
					if(ListenThread_client.i_arrived && ListenThread_client.opponent_arrived){
						ListenThread_client.confirmation_thread.start();
					}
					return;
				}else{
					ShootThreadFactory.start();
				}
			}
			*/
			if(isOn){
				count ++;
				try{
					LCD.drawString("In RHT tracking", 0, 0);

					Rectangle2D rec = SP2_v2.cam_data.getData();
					LCD.drawString(count + ": no data " + (rec == null), 0, 4);
					//this.distance = _eopdData.getValue();
					if (rec != null){
						LCD.drawString(count + ": X=" + (int) rec.getCenterX(), 0, 6);
						// PD
						double diff  = std_middle - rec.getCenterX();


						// PID
						double angle = diff; // 100 * vision_range;
						// set various error values
						last_error 	= error;
						error     	= angle;
						error_diff 	= error - last_error;

						// set PD values
						//
						double P = Kp * error;
						double D;
						if(cubic_model){
							D = Kd * error_diff;
						}else{
							D = Kd * Math.pow(error_diff / jerky_range, 3);
						}

						turnrate = P + D;

						pilot.steer(-1 * turnrate);


						if(SP2_v2.robot_heading_print){
							LCD.drawString("error = " + String.valueOf(error), 0, 1);
							LCD.drawString("last_error = " + String.valueOf(last_error), 0, 2);
							LCD.drawString("error_diff = " + String.valueOf(error_diff), 0, 3);
							LCD.drawString("P = " + String.valueOf(P), 0, 4);
							LCD.drawString("D = " + String.valueOf(D), 0, 5);
						}

						if(SP2_v2.robot_heading_print){
							LCD.drawString("turnrate = " + String.valueOf(-1 * turnrate), 0, 6);
							//LCD.drawString("angle = " + String.valueOf(-1 * Math.signum(turnrate) * std_angle), 0, 5);
						}
					}else{
						pilot.stop();
					}
					
					Delay.msDelay(camDelay*2);
				}
				catch(Exception e){
					
				}

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
			

			try {
				Thread.sleep(SP2_v2.camera_delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
	}

}
