package RobotMain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import Threads.*;
import Threads.Camera.CamMotorThread;
import Threads.Camera.CameraData;
import Threads.Camera.CameraThread;
import lejos.hardware.*;
import lejos.hardware.device.NXTCam;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.localization.PoseProvider;
import Threads.Camera.CamMotorData;
import Network.ClientServerMgr_client.ClientServerMgr_client;
import Network.ClientServerMgr_client.ListenThread_client;
import Network.Interface.Negotiator;
import Network.Interface.NetworkManager;
import Network.Util.Identity;
import Network.Util.Tools;
import Network.negotiate.Negotiator_client;
//import lejos.robotics.navigation.DifferentialPilot;
import Prof.*;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;

public class SP2_v2 {
	/*******************
	 *  debug
	 */	
		//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
		static boolean v1 = true;
		static boolean Debug = true;
		static boolean PRINT = true;
		static boolean network = true;
		public static boolean camera_motor_print	 = false;
		public static boolean robot_heading_print	 = false;

		/*******************
		 * Robot setups
		 */	
		//Robot Statistics -------------------------------------------------------------------------------------------------------

		//turning
		public static final double turnConst = 1.0;  // 0.84 1.0

		public static final double robotLength   		= 15.0;
		public static final double wheelDiameter 		= 5.6;
		public static final double wheelCircumference 	= wheelDiameter * Math.PI;
		public static final double robotTrack         	= 14.5 * turnConst;  //14.5	


		//Robot Controller -------------------------------------------------------------------------------------------------------

		//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
		@SuppressWarnings("deprecation")
		public static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
		public static Navigator nav = new Navigator(pilot);
		public static PoseProvider poseProvider = nav.getPoseProvider();
		public static final int stdSpeed = 20;
		public static final int stdRotateSpeed = 60;

		//Robot Network -------------------------------------------------------------------------------------------------------

		//Server PORT AND IP  -----------------------------------------------------------------------------------------------------------
		public static final String server_addr = "136.167.109.216"; // current IP
		public static final int    server_port = 8819;

		public static String myIP;    // = "10.200.9.181";
		public static String other_IP;// = "10.200.11.180";
		
		public static InetAddress 	local_addr;
		public static int			local_port;

		public static NetworkManager network_mgr;

		public static Negotiator negotiator;

		public static Identity net_id;


		//Camera
		public static NXTCam     camera   = new NXTCam(SensorPort.S2);
		public static CameraData cam_data = new CameraData();
		public static int        camera_delay      = 100;
		public static final int  cam_rotate_speed  = 60;
		public static NXTRegulatedMotor cam_motor  = Motor.C;
		public static CamMotorData      motor_data = new CamMotorData();

		// Sonic
		public static NXTUltrasonicSensor ultrasonic_sensor = new NXTUltrasonicSensor(SensorPort.S3);
		public static UltrasonicData      ultrasonic_data   = new UltrasonicData();
		
		//foot
		public static NXTRegulatedMotor foot = Motor.D;
		public final static int foot_angle_v1   = 65;
		public final static int foot_angle_v2   = 85;
		
		// Runnables
		static RobotHeadingThread_v1            rht_v1 = new RobotHeadingThread_v1(pilot, motor_data, ultrasonic_data, camera_delay);
		static RobotHeadingThread_v2            rht_v2 = new RobotHeadingThread_v2(pilot, cam_data,camera_delay);
		static CameraThread         cam_runnable = new CameraThread(camera,cam_data,camera_delay);
		static CamMotorThread cam_motor_runnable = new CamMotorThread(cam_motor,motor_data,cam_data,camera_delay);
		static SearchThread      search_runnable = new SearchThread(pilot);
		static UltrasonicThread   sonic_runnable = new UltrasonicThread(ultrasonic_data,ultrasonic_sensor);
		
		static Thread track            ;
		static Thread thread_camera    = new Thread(cam_runnable);
		static Thread thread_cam_motor = new Thread(cam_motor_runnable);
		static Thread thread_search    = new Thread(search_runnable);
		static Thread thread_sonic     = new Thread(sonic_runnable);
			
	
		private static int state;
		public synchronized static int getState(){	return state;	}

		private static final Object state_change_main_lock = new Object();
		private static final Object state_change_lock = new Object();
		/**
		 * Don't call notifyAll() on this lock, set state does it
		 */
		public static synchronized Object getStateChangeLock() { return state_change_lock; }
		
		public synchronized static void setState(int _state){
			state = _state;
			synchronized(state_change_main_lock){
				state_change_main_lock.notifyAll();
			}
		}
		
		public static final int STATE_searching = 0;
		public static final int STATE_tracking = 1;
		public static final int STATE_pass = 2;
		public static final int STATE_moving = 3;
		public static final int STATE_catching = 4;
		public static final int STATE_waiting = 5;
		public static final int STATE_shooting = 6;
		
	public static void main(String[] args){	
		if(v1)
			track = new Thread(rht_v1);
		else
			track = new Thread(rht_v2);
		
		           track.setDaemon(true);
		   thread_camera.setDaemon(true);
		   if(v1)
			   thread_cam_motor.setDaemon(true);
		   thread_search.setDaemon(true);
		    thread_sonic.setDaemon(true);
		    
		    //cam_data.initCleaner();
		
		/**
		 * BEGIN SETTING UP NETWORK STUFF
		 */
		
		// Initialize the network stuff

		LCD.drawString("Init part",0,0);
		negotiator = new Negotiator_client();
		try {
			network_mgr = new ClientServerMgr_client(
					InetAddress.getByName(server_addr), // server address is my computer
					server_port); 
			new Thread(new ListenThread_client()).start();
			LCD.drawString("registered",0,0);
		} catch (UnknownHostException e) {
			LCD.drawString("Unknown Host!", 0, 6);
		}
		
		if(v1){   
			poseProvider.setPose(new Pose(Param.start_x1, Param.start_y1, Param.start_heading));
		}else{
			poseProvider.setPose(new Pose(Param.start_x2, Param.start_y2, Param.start_heading));
		}
		/**
		 * DONE SETTING UP NETWORK STUFF
		 */
		

		/**
		 *  4/28/2016 
		 *  
		 *  Changed the next 20-30lines
		 */
		
		LCD.clear();
		LCD.drawString("Press a button",0,0);
		Button.waitForAnyPress();
		LCD.clear();
		
		track.start();
		if(v1){
			LCD.drawString("Im v1", 0, 0);
			Delay.msDelay(500);
			thread_cam_motor.start();
		}
	    thread_camera.start();


		LCD.drawString("Press a button",0,0);
		Button.waitForAnyPress();
		
	
		thread_search.start();
	
		thread_sonic.start();
		LCD.clear();
		
		
		setState(0);
		
		
		/**
		 * Runnables:
		 * static RobotHeadingThread            rht = new RobotHeadingThread(pilot,motor_data,ultrasonic_data,camera_delay);
		 * static CameraThread         cam_runnable = new CameraThread(camera,cam_data,camera_delay);
		 * static CamMotorThread cam_motor_runnable = new CamMotorThread(cam_motor,motor_data,cam_data,camera_delay);
		 * static SearchThread      search_runnable = new SearchThread(pilot);
		 * static UltrasonicThread   sonic_runnable = new UltrasonicThread(ultrasonic_data,ultrasonic_sensor);
		 */
		
		setState(STATE_searching);
		camera.enableTracking(true);
		clearData();
		// for now call these methods to simulate
		// having already been thru the search state
		/*
		search_runnable.off();
		while(!Button.LEFT.isDown()){
			if(v1)
				rht_v1.on();
			else
				rht_v2.on();
			
			if(v1)
				cam_motor_runnable.on();  // turn on
			sonic_runnable.on();      // turn on
			cam_runnable.on();
			search_runnable.off();
			Delay.msDelay(1000);
		}
		 */
		
		int i=0;

		while(true){

			Delay.msDelay(camera_delay);
			//System.out.println("Checking state");
			/*
			LCD.clear();
			LCD.drawString("State="+state,0,0);
			LCD.drawString("cam running:"+cam_runnable.isOn, 0,1);
			if(v1)
				LCD.drawString("RHT running:"+rht_v1.isOn, 0, 2);
			else
				LCD.drawString("RHT running:"+rht_v2.isOn, 0, 2);
			LCD.drawString("Search running:"+search_runnable.isOn, 0,3);
			if(v1)
				LCD.drawString("Cam_motor:"+cam_motor_runnable.isOn, 0,4);
			LCD.drawString("Sonic running:"+sonic_runnable.isOn, 0,5);
			 */

			switch(getState()) {

			case(STATE_searching):
			{
				// Turn off all threads but search & camera
				if(v1)
					cam_motor_runnable.off();
				if(v1)
					rht_v1.off();
				else
					rht_v2.off();

				sonic_runnable.off(); // turn off

				cam_runnable.on();
				search_runnable.on();

				break;
			}
			case(STATE_tracking):
			{
				//robot_heading
				//sonic
				//cam
				//cam_motor
				if(v1)
					rht_v1.on();
				else
					rht_v2.on();
				if(v1)
					cam_motor_runnable.on();  // turn on
				sonic_runnable.on();      // turn on
				cam_runnable.on();
				search_runnable.off();
	
				break;
			}
			case(STATE_pass):
			{
				//
				if(v1)
					cam_motor_runnable.off();
				if(v1)
					rht_v1.off();
				else
					rht_v2.off();
	
				sonic_runnable.off();
				cam_runnable.off();
				search_runnable.off();
	
				break;
			}
			case(STATE_moving):
			{
				//
				if(v1)
					cam_motor_runnable.off();
				if(v1)
					rht_v1.off();
				else
					rht_v2.off();
	
				sonic_runnable.off();
				cam_runnable.off();
				search_runnable.off();
	
				break;
			}
			case(STATE_waiting):
				//
			{
				if(v1)
					rht_v1.off();
				else
					rht_v2.off();

				if(v1)
					cam_motor_runnable.off();
				
				sonic_runnable.off();
				cam_runnable.off();
				search_runnable.off();

				break;
			}
			case(STATE_catching):
			{
				break;
			}
			case(STATE_shooting):
			{
				//We should let the ultrasonic thread set state to this
				// when sonic locates the ball.
				if(v1)
					cam_motor_runnable.off();
				if(v1)
					rht_v1.off();
				else
					rht_v2.off();

				sonic_runnable.off();
				cam_runnable.off();
				search_runnable.off();
				
				//Shoot
				SP2_v2.nav.stop();
				face(Param.goal_x, Param.goal_y);
				kick();

				Delay.msDelay(10000);
				System.exit(0);
				break;
			}


			}
			
			synchronized(state_change_lock){
				state_change_lock.notifyAll();
			}
			LCD.drawString(i + ": State: " + getState(), 0, 1);
			LCD.drawString(search_runnable.isOn() + ":" + cam_runnable.isOn(), 0, 5);
			//wait for state_change_signal;
			i++;
			try {
				synchronized(state_change_main_lock){
					state_change_main_lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/*
			try {
				track.sleep(200);
				if(v1)
					thread_cam_motor.sleep(200);
			//	thread_camera.sleep(camera_delay);
				thread_search.sleep(200);
				thread_sonic.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 */

		
		
		//while(!network_mgr.quit());
	}
	
	public static void setShooter(){
		setState(STATE_tracking);
		SP2_v2.sonic_runnable.setShooter();
	}
	
	public static void face(float x, float y){
		LCD.clear(5); LCD.clear(6);
		
		Pose p = SP2_v2.poseProvider.getPose();
		LCD.drawString(String.format("I'm facing %f\n", p.getHeading()), 0, 5);
		double dy = y - p.getY();
		double dx = x - p.getX();
		double angle = Math.atan(dy/dx) * 180 / Math.PI;
		
		int quadrant = 0;
		if(dy < 0 && dx > 0)
			quadrant = 4;
		if(dy > 0 && dx > 0)
			quadrant = 1;
		if(dy < 0 && dx < 0)
			quadrant = 3;
		if(dy > 0 && dx < 0)
			quadrant = 2;
		
		switch(quadrant){
		case 1:
			
			break;
		case 2:
			angle += 180;
			break;
		case 3:
			angle += 180;
			break;
		case 4:
			angle += 360;
			break;
		}

		SP2_v2.nav.stop();
		LCD.clear();
		LCD.drawString("Rotating to " + angle, 0, 6);
		Delay.msDelay(1000);
		SP2_v2.pilot.setLinearSpeed(180);
		SP2_v2.pilot.setAngularSpeed(45);
		
		SP2_v2.nav.rotateTo(angle);
		LCD.clear(5);
		LCD.drawString(String.format("Rotating to %f\n", angle), 0, 5);
		Delay.msDelay(1000);
		
		while(SP2_v2.nav.isMoving()){}
	}
	
	public static void kick(){
		// 	need to pick a better kick speed
		foot.setSpeed(700);
		//
		if(v1){
			foot.rotate(SP2_v2.foot_angle_v1);

			//grabbed = true;
			Delay.msDelay(500);
			foot.rotate(-SP2_v2.foot_angle_v1);
		}else{
			foot.rotate(SP2_v2.foot_angle_v2);

			//grabbed = true;
			Delay.msDelay(500);
			foot.rotate(-SP2_v2.foot_angle_v2);
		}
	}
	
	private static void clearData(){
		ultrasonic_data.setSonicValue(0);
		SP2_v2.cam_data.clearData();
	}
}
