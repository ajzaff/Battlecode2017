package team419;

import battlecode.common.*;

import static battlecode.common.RobotType.GARDENER;
import static battlecode.common.Team.NEUTRAL;

final strictfp class BotArchon extends Navigation {

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {

        GameState.init(rc);
        FastMath.initRand(rc);
        myDir = Navigation.getRandomDirection();

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
        tryShakeNearbyTree();
        if (roundNum % 20 == 0) {
            tryHireGardener();
        }
        if (Navigation.tryMoveInDirection(myDir)) {
            return;
        } else {
            myDir = myDir.rotateRightRads(THIRTYSECOND_TURN);
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
           n = .0105f * rc.getTeamBullets(); // nest egg
       else
           n = 0; // too soon for charity

        int donation = (int) (n / 10) * 10;

        if (donation == 0)
            return false;

        rc.donate(donation);
        return true;
    }

    private static boolean tryHireGardener() throws GameActionException {
        if (!rc.isBuildReady() || !rc.hasRobotBuildRequirements(GARDENER)) {
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
