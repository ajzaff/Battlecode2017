package betterexplore;

import battlecode.common.*;

strictfp class GameState {

    // constants
    static RobotController rc;
    static Team myTeam;
    static Team theirTeam;
    static float sensorRadius;
    static RobotType myType;
    static int myId;
    static int roundLimit;

    static MapLocation myLoc;
    static int roundNum;
    static RobotInfo[] nearbyRobots;
    static RobotInfo[] nearbyFriends;
    static TreeInfo[] nearbyTrees;
    static RobotInfo[] nearbyEnemies;
    static BulletInfo[] nearbyBullets;
    static int robotCount;

    static void init(RobotController rc) {
        GameState.rc = rc;
        myTeam = rc.getTeam();
        theirTeam = rc.getTeam().opponent();
        myType = rc.getType();
        myLoc = rc.getLocation();
        myId = rc.getID();
        sensorRadius = myType.sensorRadius;
        roundLimit = rc.getRoundLimit();
    }

    static void update() {
        myLoc = rc.getLocation();
        roundNum = rc.getRoundNum();
        robotCount = rc.getRobotCount();
    }

    static void senseNearbyRobots() {
        nearbyRobots = rc.senseNearbyRobots();
    }

    static RobotInfo[] senseNearbyRobots(float v, Team t) {
         return rc.senseNearbyRobots(v, t);
    }

    static RobotInfo[] senseNearbyRobots(float v) {
        return rc.senseNearbyRobots(v);
    }

    static void senseNearbyTrees() {
        nearbyTrees = rc.senseNearbyTrees();
    }

    static void senseNearbyBullets() {
        nearbyBullets = rc.senseNearbyBullets();
    }

    static void senseNearbyFriends() {
        nearbyFriends = rc.senseNearbyRobots(sensorRadius, myTeam);
    }

    static void senseNearbyEnemies() {
        nearbyEnemies = rc.senseNearbyRobots(sensorRadius, theirTeam);
    }
}