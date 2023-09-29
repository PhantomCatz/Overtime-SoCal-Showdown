// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import frc.Mechanisms.intake.CatzIntake;
import frc.Mechanisms.shooter.CatzShooter;
import frc.Utils.CatzConstants;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends LoggedRobot {
  /*
   *  DUMMY CONTROLS:
   *    - Right Trigger: preroll
   *    - A: shoot 
   */

   public static final CatzIntake intake = CatzIntake.getInstance();
   //public static final CatzIndexer indexer = new CatzIndexer();
   public static final CatzShooter shooter = CatzShooter.getIntstance();

   private final int XBOX_AUX_PORT = 1;
   public static XboxController xboxAux;


   @Override
   public void robotInit()
   {
      Logger logger = Logger.getInstance();

      // Record metadata
      logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
      logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
      logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
      logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
      logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
      switch (BuildConstants.DIRTY) 
      {
        case 0:
          logger.recordMetadata("GitDirty", "All changes committed");
          break;
        case 1:
          logger.recordMetadata("GitDirty", "Uncomitted changes");
          break;
        default:
          logger.recordMetadata("GitDirty", "Unknown");
          break;
      }
  
      // Set up data receivers & replay source
      switch (CatzConstants.currentMode) 
      {
        // Running on a real robot, log to a USB stick
        case REAL:
          logger.addDataReceiver(new WPILOGWriter("/media/sda1/Robotdata"));
          logger.addDataReceiver(new NT4Publisher());
         // new PowerDistribution(1, ModuleType.kRev);
          break;
  
        // Running a physics simulator, log to local folder
        case SIM:
          logger.addDataReceiver(new WPILOGWriter("F:/robotics code projects/loggingfiles/"));
          logger.addDataReceiver(new NT4Publisher());
          break;
  
        // Replaying a log, set up replay source
        case REPLAY:
          setUseTiming(false); // Run as fast as possible
          String logPath = LogFileUtil.findReplayLog();
          logger.setReplaySource(new WPILOGReader(logPath));
          logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
          break;
      }
      // Start AdvantageKit logger
      logger.start();
  
      xboxAux = new XboxController(XBOX_AUX_PORT);
      shooter.printTemperatures();
   }

   @Override
   public void robotPeriodic()
   {
    if(!DriverStation.isAutonomousEnabled() && !DriverStation.isTeleopEnabled())
    {
      shooter.shooterPeriodic();
      intake.intakePeriodic();//TBD add indexer periodic
    }
      shooter.smartdashboardShooter();
   }

   @Override
   public void autonomousPeriodic()
   {
      shooter.shooterPeriodicUpdate();
   }

   @Override
   public void teleopInit()
   {

   }

   @Override
   public void teleopPeriodic()
   {
    shooter.shooterPeriodic();
    intake.intakePeriodic();//TBD add indexer periodic


      shooter.cmdProcShooter(xboxAux.getYButtonPressed(), 
                             xboxAux.getXButtonPressed(), 
                             xboxAux.getAButtonPressed(), 
                             xboxAux.getBButtonPressed(),
                             xboxAux.getStartButtonPressed());
      
      shooter.shooterPeriodicUpdate();

      intake.cmdProcIntake(xboxAux.getRightStickButton(), xboxAux.getLeftTriggerAxis(), xboxAux.getRightTriggerAxis());

   }
}