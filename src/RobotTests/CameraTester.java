package RobotTests;

import java.util.LinkedList;

import Threads.Camera.*;
import lejos.hardware.*;
import lejos.hardware.device.NXTCam;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.geometry.Rectangle2D;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;

public class CameraTester {
	/*******************
	 *  debug
	 */	
		//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
		static boolean Debug = true;
		static boolean PRINT = true;
		static boolean network = true;
		
		//PORT AND IP  -----------------------------------------------------------------------------------------------------------
		static final String IPAddress = "10.200.11.166";
		static final int port = 6983;
		
		//Camera
		static NXTCam camera = new NXTCam(SensorPort.S2);
		static int camera_delay = 100;
			
	/*******************
	 * Robot setups
	 */	
		static Brick brick = BrickFinder.getDefault();
		//Robot Statistics -------------------------------------------------------------------------------------------------------
		
		//turning
		static final double turnConst = 0.83;  // 0.86 0.95
			
		static final double robotLength = 15.0;
		static final double wheelDiameter = 5.4;
		static final double wheelCircumference = wheelDiameter * Math.PI;
		static final double robotTrack = 14.5 * turnConst;  //14.5	

			
		//Robot Controller -------------------------------------------------------------------------------------------------------

		//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
		@SuppressWarnings("deprecation")
		static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
		static Navigator nav = new Navigator(pilot);
		static PoseProvider poseProvider = nav.getPoseProvider();
		static int stdSpeed = 15;
		static int stdRotateSpeed = 45;
	
	public static void main(String[] args){	
		
		CameraData data1 = new CameraData();
		Thread t = new Thread(new CameraThread(camera, data1, camera_delay));
		t.setDaemon(true);
		t.start();
		
		int i=0;
		while(true){
			Rectangle2D rec = data1.getData();
			if(rec == null){
				Delay.msDelay(camera_delay);
				System.out.println("not recognizing");
				continue;
			}
			i++;
			
			double center_x = rec.getCenterX();
			double center_y = rec.getCenterY();
			
			double X = rec.getX();
			double Y = rec.getY();
			
			double min_x = rec.getMinX();
			double min_y = rec.getMinY();
			
			double max_x = rec.getMaxX();
			double max_y = rec.getMaxY();
			
			double height = rec.getHeight();
			double width = rec.getWidth();
			
			System.out.println("------------------------------");
			
			System.out.printf("Rectangle = %d\n center_x = %f\n center_y = %f\n", i, center_x, center_y);
			System.out.printf("X = %f\n Y = %f\n", X, Y);
			
			System.out.printf("min_X = %f\n min_Y = %f\n", min_x, min_y);
			System.out.printf("max_X = %f\n max_Y = %f\n", max_x, max_y);
			
			System.out.printf("width = %f\n height = %f\n", width, height);
			
			
			Delay.msDelay(camera_delay);
		}
	}

}
