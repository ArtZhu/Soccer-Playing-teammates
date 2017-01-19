package RobotTests;

import java.util.*;

import Threads.*;
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
//import lejos.robotics.navigation.DifferentialPilot;
import Prof.*;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;

public class TurnTester {
	/*******************
	 *  debug
	 */	
		//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
		static boolean Debug = true;
		static boolean PRINT = true;
		static boolean network = true;
		public static boolean camera_motor_print	 = false;
		public static boolean robot_heading_print	 = true;
		
		//Friend PORT AND IP  -----------------------------------------------------------------------------------------------------------
		static final String IPAddress = "10.200.11.166";
		static final int port = 6983;
		
		//Camera
		static NXTCam camera       = new NXTCam(SensorPort.S2);
		static CameraData cam_data = new CameraData();
		static int camera_delay    = 20;
		
		// Sonic
		static NXTUltrasonicSensor ultrasonic_sensor = new NXTUltrasonicSensor(SensorPort.S3);
		static UltrasonicData ultrasonic_data = new UltrasonicData();
		
		//foot
		static NXTRegulatedMotor foot = Motor.C;
		public final static int foot_angle = 90;
			
	/*******************
	 * Robot setups
	 */	
		//Robot Statistics -------------------------------------------------------------------------------------------------------
		
		//turning
		static final double turnConst = 1.0;  // 0.84 1.0
			
		static final double robotLength   = 15.0;
		static final double wheelDiameter = 5.6;
		static final double wheelCircumference = wheelDiameter * Math.PI;
		static final double robotTrack    = 14.5 * turnConst;  //14.5	

			
		//Robot Controller -------------------------------------------------------------------------------------------------------

		//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
		@SuppressWarnings("deprecation")
		static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
		static Navigator nav = new Navigator(pilot);
		static PoseProvider poseProvider = nav.getPoseProvider();
		public static final int stdSpeed = 15;
		public static final int stdRotateSpeed = 60;
	
	public static void main(String[] args){		
		pilot.setAngularSpeed(stdRotateSpeed);
		pilot.rotate(90);
	}

}
