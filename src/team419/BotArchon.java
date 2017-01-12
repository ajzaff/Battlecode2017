package team419;

import battlecode.common.*;

import static battlecode.common.RobotType.GARDENER;
import static battlecode.common.Team.NEUTRAL;

final strictfp class BotArchon extends Navigation {

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {

        GameState.init(rc);
        FastMath.initRand(rc);
        exploreDir = Navigation.getRandomDirection();

        while (true) {
            int begin = rc.getRoundNum();
            try {
                GameState.update();
                act();
            } catch (Exception e) {
                System.out.println("EXCEPTION! thrown in act(): ");
                e.printStackTrace(System.out);
            }
            int end = rc.getRoundNum();
            if (begin != end) {
                System.out.println("ERROR! overflowing bytecode limit");
            }
            Clock.yield();
        }
    }

    private static void act() throws GameActionException {
        tryDonateBullets();
        GameState.senseNearbyTrees();
        GameState.senseNearbyRobots();
        tryShakeNearbyTree();
        tryMicro();
        tryHireGardener();
    }

    private static void tryMicro() throws GameActionException {

        if (nearbyRobots.length > 0 && !rc.hasMoved()) {

            MapLocation safeLoc = myLoc;
            MapLocation dangerLoc = myLoc;
            Direction dir = null;

            // Try to flee away from enemies in the direction of friendlies
            for (RobotInfo r : nearbyRobots) {
                if (r.team == myTeam)
                    safeLoc = safeLoc.add(myLoc.directionTo(r.location));
                else if (r.team == theirTeam && r.type.canAttack())
                    dangerLoc = dangerLoc.add(myLoc.directionTo(r.location));
            }

            dir = dangerLoc.directionTo(myLoc);
            if (dir != null && Navigation.tryMoveInDirection(dir))
                return;

            // Still in danger but unable to move, try moving toward friendly unit
            if (dir != null) {
                dir = myLoc.directionTo(safeLoc);
                if (dir != null && Navigation.tryMoveInDirection(dir))
                    return;
            }
        }

    }

    private static boolean tryDonateBullets() throws GameActionException {
       float n;

       if (robotCount < 7 && roundNum > 900)
           n = rc.getTeamBullets();  // pray for generosity
       if (roundLimit - roundNum < 5)
           n = rc.getTeamBullets();  // transfer all bullets at last instant
       else if (roundLimit - roundNum < 50)
           n = .33f * rc.getTeamBullets();
       else if (roundLimit - roundNum < 100)
           n =.2f * rc.getTeamBullets();
       else if (roundNum > 200)
           n = .05f * rc.getTeamBullets(); // nest egg
       else
           n = 0; // too soon for charity

        int donation = (int) (n / 10) * 10;

        if (donation == 0)
            return false;

        rc.donate(donation);
        return true;
    }

    private static boolean tryHireGardener() throws GameActionException {
        if (!rc.isBuildReady() || !rc.hasRobotBuildRequirements(GARDENER) || roundNum % 30 != 0) {
            return false;
        }
        Direction dir = new Direction(Navigation.getRandomRadians());
        for (int i=0; i < 8; i++) {
            if (rc.canBuildRobot(GARDENER, dir)) {
                rc.buildRobot(GARDENER, dir);
                return true;
            }
            dir = dir.rotateLeftRads(Navigation.EIGHTH_TURN);
        }
        return false;
    }

    private static boolean tryShakeNearbyTree() throws GameActionException {
        if (!rc.canShake())
            return false;

        TreeInfo bestTree = null;
        for(TreeInfo t : nearbyTrees)
            if (t.team == NEUTRAL && rc.canShake(t.location))
                if (bestTree == null)
                    bestTree = t;
                else if (t.getContainedBullets() > bestTree.getContainedBullets())
                    bestTree = t;
        if (bestTree == null)
            return false;
        rc.shake(bestTree.location);
        return true;
    }
}
