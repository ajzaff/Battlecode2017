package team419;

import battlecode.common.*;

import static battlecode.common.GameConstants.VICTORY_POINTS_TO_WIN;

strictfp class GameState {

    // constants
    static RobotController rc;
    static Team myTeam;
    static Team theirTeam;
    static float sensorRadius;
    static RobotType myType;
    static int myId;
    static int roundLimit;
    static MapLocation[] myInitArchonLocs;
    static MapLocation[] theirInitArchonLocs;
    static float myStride;

    static MapLocation myLoc;
    static int roundNum;
    static RobotInfo[] nearbyRobots;
    static RobotInfo[] nearbyFriends;
    static TreeInfo[] nearbyTrees;
    static RobotInfo[] nearbyEnemies;
    static BulletInfo[] nearbyBullets;
    static int robotCount;
    static int age;

    static void init(RobotController rc) {
        GameState.rc = rc;
        myTeam = rc.getTeam();
        theirTeam = myTeam.opponent();
        myType = rc.getType();
        myLoc = rc.getLocation();
        myId = rc.getID();
        sensorRadius = myType.sensorRadius;
        roundLimit = rc.getRoundLimit();
        age = 1;
        myInitArchonLocs = rc.getInitialArchonLocations(myTeam);
        theirInitArchonLocs = rc.getInitialArchonLocations(theirTeam);
        myStride = myType.strideRadius;
    }

    static void update() {
        myLoc = rc.getLocation();
        roundNum = rc.getRoundNum();
        robotCount = rc.getRobotCount();
        age++;
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

    static boolean tryDonateBullets() throws GameActionException {
        float teamBullets = rc.getTeamBullets();

        // Try for instant win
        if (teamBullets / 10 + rc.getTeamVictoryPoints() >= VICTORY_POINTS_TO_WIN) {
            rc.donate(teamBullets);
            return true;
        }

        // If it's a close call - donate all!
        if (rc.getRoundLimit() - roundNum < 50) {
            rc.donate(teamBullets);
        }

        return false;
    }
}