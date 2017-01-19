package Threads.Camera;
//Date: Feb 15, 2016

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.geometry.Rectangle2D;
import lejos.utility.Delay;
import Threads.Camera.*;
import RobotMain.SP2_v2;

public class CamMotorThread implements Runnable {
	//Assume 50 = 22.5 degree
	//Assume max vision_range = 60 deg;
	static final double vision_range = 60;
	
	NXTRegulatedMotor arm;
	CameraData data;
	int delay;
	final int std_middle = 100;
	final double rotate_factor = 1.5;  // changed from 1.0	
	
////////////////////////////////////////////////////////////////////////////////////////////////////
	// initialize PID constants
	//
	final double Kp = 3.0;
	final double Kd = 1.40;
	
	CamMotorData motor_data;
	
	final String leftString  = "Left";
	final String rightString = "Right";
	final String not_leftString  = "LMax";
	final String not_rightString = "RMax";
	
	private boolean isOn = false;
	
	public CamMotorThread(NXTRegulatedMotor _arm, CamMotorData _motor_data, CameraData _data, int _delay){
		arm   = _arm;
		data  = _data;
		delay = _delay;
		motor_data = _motor_data;
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
		double error = 0;
		double lastError = 0;
		double errorDiff;
		int count=0;
		while(true){
			if (isOn){
				//System.out.println("In Cam Motor Thread");
				try{
					double diff;
					Rectangle2D rec = data.getData();
					LCD.drawString(count + ": X=" + (int) rec.getCenterX(), 0, 6);
					count++;
					diff  = std_middle - rec.getCenterX();
					//LCD.drawString("diff = " + String.valueOf(diff), 0, 6);
					double angle = diff/100.0 * vision_range;
					
					// insert PID here to control jerkyness of the camera rotation...
					
					// set various error values
					lastError = error;
					error     = angle;
					errorDiff = error - lastError;

					// set PID values
					//
					double P = Kp * Math.abs(error);
					double D = Kd * errorDiff;

					double turn_angle = P + D;

					int v = (int) (rotate_factor * turn_angle / 45.0 * SP2_v2.cam_rotate_speed);
					arm.setSpeed(v); 
					
					//LCD.drawString("rf = " + String.valueOf(rotate_factor), 0, 2);
					//LCD.drawString("camv = " + String.valueOf(v), 0, 3);
					
					//LCD.drawString("P = " + String.valueOf(P), 0, 4);
					//LCD.drawString("D = " + String.valueOf(D), 0, 5);
					if (error < 0) {
						//Turn right
						if(arm.getTachoCount() < 150){
							//LCD.drawString(leftString, 0, 1);
							//arm.backward();
							arm.forward(); // had to change because of rebuild
						}else{
							//LCD.drawString(not_leftString, 0, 1);
							if(arm.isMoving())
								arm.stop();
						}
							
					} else {
						//Turn left
						
						if(arm.getTachoCount() > -130){
							//LCD.drawString(rightString, 0, 1);
							arm.backward();					
						}else{
							//LCD.drawString(not_rightString, 0, 1);
							if(arm.isMoving())
								arm.stop();
						}
					}
					motor_data.put(arm.getTachoCount());
					
				}catch(NullPointerException e){
					//LCD.clear();
					//LCD.drawString("STOP!", 0, 1);
					//arm.setSpeed(60);
					arm.stop();
				}
			}
			else{
				// is not on, do nothing and stop moving my arm. 
				// also reset the arm to face forward
				Thread.yield();
				Delay.msDelay(200);
				arm.rotate(-1 * arm.getTachoCount());
				arm.stop();
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
