package frc.Autonomous;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.Mechanisms.CatzShooter.ShootingMode;
import frc.robot.Robot;

/*****************************************************************************************
*
* Autonomous selections
* 
*****************************************************************************************/
public class CatzAutonomousPaths
{  
    public final SendableChooser<Boolean> chosenAllianceColor = new SendableChooser<>();
    private final SendableChooser<Integer> chosenPath         = new SendableChooser<>();

    /*------------------------------------------------------------------------------------
    *  Field Relative angles when robot is TBD - Finish Comment  
    *-----------------------------------------------------------------------------------*/
    private final double BIAS_OFFSET =   0.0;
    private final double FWD_OR_BWD  =   0.0 + BIAS_OFFSET;
    private final double RIGHT       =   90.0; 
    private final double LEFT        =   -90.0;

    private double direction         = 0.0;

    /*------------------------------------------------------------------------------------
    *  Path ID's
    *-----------------------------------------------------------------------------------*/
    

    private final int TEST                            = 100;

    private final int CENTER_SCORE_1_HIGH_BALANCE = 1;
    private final int CENTER_SCORE_1_MID_BALANCE  = 2;
    
    private final int RIGHT_SCORE_2                 = 3;
    private final int LEFT_SCORE_2                  = 4;
    private final int CENTER_SCORE_1_BALANCE        = 5;

    public static int pathID;

     /*  DRIVE STRAIGHT VALUES: 
     * if distance > 70, then FAST, else SLOW
     * 8 second maxTime is an arbitrary number, subject to change upon more testing 
     * only robot backwards movement has negative signs over distance and maxspeed
     * left and right distances and max speed aren't negative
     * TEMP_DECEL_DIST decelDistance is an arbitrary number, subject to change upon more testing
     * 
     *   *note* - autonomous is 15 seconds, meaning that all of this will have to finsih within that time
     *          - THIS CODE IS MADE FOR BLUE SIDE 
     *          - FOR RED, CHANGE LEFTS WITH RIGHTS AND RIGHTS WITH LEFTS (from blue)
     *          - movement similar to code.org level programming
    */

    /* PATH NAME:
     *    /CenterRightTunnel/
     * - CenterRight (Starting Position)
     * - Tunnel (type of movement/movement path)
     */

    /* Distances:          -______-
     * drive.DriveStraight(distance, decelDistance, maxSpeed, wheelPos, maxTime);
     *  - 224 = distance from grid to center pieces
     *                
     */
    // drive.DriveStraight(distance, decelDist, )


    public CatzAutonomousPaths()
    {
        chosenAllianceColor.setDefaultOption("Blue Alliance", Robot.constants.BLUE_ALLIANCE);
        chosenAllianceColor.addOption       ("Red Alliance",  Robot.constants.RED_ALLIANCE);
        SmartDashboard.putData              ("Alliance Color", chosenAllianceColor);

        chosenPath.addOption("Center Score High 1 Balance", CENTER_SCORE_1_HIGH_BALANCE);
        chosenPath.addOption("Center Score Mid 1 Balance", CENTER_SCORE_1_MID_BALANCE);
        chosenPath.addOption("Center Score 1 Balance", CENTER_SCORE_1_BALANCE);

        chosenPath.addOption("Right Score 2", RIGHT_SCORE_2);
        chosenPath.addOption("Left Score 2", LEFT_SCORE_2);
        

        chosenPath.addOption       ("TEST PATH",  TEST);

        SmartDashboard.putData     ("Auton Path", chosenPath);
    
    }


    public void executeSelectedPath()
    {
        pathID = chosenPath.getSelected();

        if(chosenAllianceColor.getSelected() == Robot.constants.RED_ALLIANCE)
        {
            direction = LEFT;
        }
        else
        {
            direction = RIGHT;
        }

        System.out.println("PathID: " + pathID);

        switch (pathID)
        {
            case CENTER_SCORE_1_HIGH_BALANCE: centerScore1HighBalance();
            break;

            case CENTER_SCORE_1_MID_BALANCE       : centerScore1MidBalance();
            break;

            case RIGHT_SCORE_2                : rightScore2();
            break;

            case LEFT_SCORE_2                 : leftScore2();
            break;

            case CENTER_SCORE_1_BALANCE       : centerScore1Balance();

            case TEST: testPath(); //Scores High Cone - TBD
            break;
        }

    }

    public void testPath()
    {
        Robot.auton.DriveStraight(-100,FWD_OR_BWD,3.0);
    }



    /*-----------------------------------------------------------------------------------------
    *    
    *  Auton Functions
    * 
    *----------------------------------------------------------------------------------------*/
    public void Balance()
    {
        Robot.balance.StartBalancing();
    }

    private void cubeScore(ShootingMode mode)
    {
        Robot.shooter.cubeScore(mode, 2.0);

        while(!Robot.shooter.finishedShooting())
        {
            //wait for shooting to finish
        }
    }

    private void turnIntakeSystemOn()
    {
        Robot.intake.deployIntake();
        Robot.intake.intakeRollerInward();
        Robot.indexer.indexerIntake();
    }

    private void turnIntakeSystemOff()
    {
        Robot.intake.stowIntake();
        Robot.intake.intakeRollerOff();
        Robot.indexer.indexerOff();
    }

    private void driveOutOfCommunityAndBackOnChargeStationCenter()
    {
        Robot.auton.DriveStraightOFFChargeStation(170.0, FWD_OR_BWD, 4.0);

        Timer.delay(1.0);

        Robot.auton.DriveStraightONChargeStationFromBack(-108, FWD_OR_BWD, 4.0); 
        Robot.balance.StartBalancing();
    }

    /*-----------------------------------------------------------------------------------------
    *    
    *  Auton Paths
    * 
    *----------------------------------------------------------------------------------------*/
    

    private void centerScore1HighBalance()
    {
        cubeScore(ShootingMode.HIGH);

        driveOutOfCommunityAndBackOnChargeStationCenter();
    }

    private void centerScore1MidBalance()
    {
        cubeScore(ShootingMode.MID);

        driveOutOfCommunityAndBackOnChargeStationCenter();
    }

    private void centerScore1Balance()
    {
        cubeScore(ShootingMode.HIGH);
        Robot.auton.DriveStraight(184, FWD_OR_BWD, 4.0);
        turnIntakeSystemOn();

        Robot.auton.DriveStraight(40, FWD_OR_BWD, 2.0);
        Timer.delay(1.0);
        turnIntakeSystemOff();

        Robot.auton.DriveStraight(-90, FWD_OR_BWD, 4.0); 
        Balance();
    }

    private void rightScore2()
    {
        cubeScore(ShootingMode.HIGH);

        turnIntakeSystemOn();


        Robot.auton.DriveStraight(200, FWD_OR_BWD, 5.0);

        Timer.delay(1.0);

        CompletableFuture.runAsync(()->{
            turnIntakeSystemOff();
            Robot.shooter.revUpShootMotor(ShootingMode.MID);
        });

        Robot.auton.DriveStraight(-200, FWD_OR_BWD, 5.0);
        Robot.shooter.shoot();
    }

    private void leftScore2()
    {
        cubeScore(ShootingMode.HIGH);

        CompletableFuture.runAsync(()->{
            turnIntakeSystemOn();
        });

        Robot.auton.DriveStraight(-200, FWD_OR_BWD, 5.0);

        Timer.delay(1.0);

        CompletableFuture.runAsync(()->{
            turnIntakeSystemOff();
            Robot.shooter.revUpShootMotor(ShootingMode.MID);
        });

        Robot.auton.DriveStraight(200, FWD_OR_BWD, 5.0);
        Robot.shooter.shoot();
    }
}