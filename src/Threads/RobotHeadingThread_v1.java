package Threads;

import java.util.LinkedList;

import RobotMain.SP2_v2;
import Threads.*;
import Threads.Camera.CamMotorData;
//import lejos.robotics.navigation.DifferentialPilot;
import Prof.*;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class RobotHeadingThread_v1 implements Runnable{

	/**
	 * Control thread to point robot in direction of the ball
	 * 
	 */
	
	private int                 rotateSpeed = 10; /**Degrees per second... */
	private CamMotorData        _data;
	private LinkedList<Integer> _buf;
	private DifferentialPilot   _pilot;
	private UltrasonicData      _eopdData;
	private int                 distance, grabDist, turnRate; // devise some formula for this to insert into STEER
	private int                 _camDelay;
	private boolean isOn = false;
	private double 				rotate_factor = 3.0;
	private int 				steer_rate = 90; // used to be 70
	
	public RobotHeadingThread_v1(DifferentialPilot pilot, CamMotorData data, UltrasonicData eopdData, int camDealy){
		_data     = data;
		_pilot    = pilot;
		_eopdData = eopdData;
		_pilot.setRotateSpeed(rotateSpeed); 
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
	
	/*
	public void kill(){
		Thread.currentThread().interrupt();
		return;
	}
	*/
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		//_pilot.setAngularSpeed(SoccerPlayer.stdRotateSpeed);
		_pilot.setLinearSpeed(SP2_v2.stdSpeed/2); // PUT / 3 HERE FOR TESTING!!!
		
		//_pilot.setRotateSpeed(SoccerPlayer.stdRotateSpeed);
		_pilot.setTravelSpeed(SP2_v2.stdSpeed/2);
		_pilot.setAngularSpeed(500); // used to be 700
		
		_buf = _data.getTrack();
		
		boolean steer = true;
		boolean version1 = false;
		while(true){
			if (isOn){
				
				LCD.drawString("In RHT tracking", 0, 0);
				try{
					this.distance = _eopdData.getValue();
					_buf = _data.getTrack(); // get the track again....
					if ( _buf.size() > 0){
						
						// Steer towards the ball.... Not entirely sure if this works yet
						// So maybe we'll go with a simple rotate for now
						// _pilot.steer(50,(int) _buf.get(_buf.size()-1), true);
						
						int angle;
						if ( _buf.size() > 1 ){
							angle = -1 * _buf.get(_buf.size()-1); // get the last angle put into the LL
						}
						else
							angle = -1 * _buf.get(_buf.size()-1) + _buf.get(_buf.size()-2); // not sure what this does..
						
						// Use steer to move bot
						if(steer){
							//version1 is false rn
							if(version1){
								if(angle > 0)
									_pilot.steer(angle*1.50, angle, true);//, true); // gets the last item in the buffer
								else
									_pilot.steer(-1 * angle*1.50, angle, true);
							}else{
								// this factor of 1.60 seems to work pretty well. 
								_pilot.steer((-1)*angle*1.60);
							}
							Delay.msDelay(_camDelay);
						}
						// Not using steer to move robot
						else{
							_pilot.setAngularSpeed(Math.abs(angle) * rotate_factor);
							_pilot.rotate(Math.signum(angle) * 90, true);
							
							//_pilot.forward();
							
							double dist = SP2_v2.stdSpeed / 100 * _camDelay;
							_pilot.travel(dist, true);
						}
					
					}
					else{
						_buf = _data.getTrack();
					}
					
				}
				catch(Exception e){
					continue;
				}
			}
			else{
				// NOT ON
				Thread.yield();
//				Delay.msDelay(200);
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
