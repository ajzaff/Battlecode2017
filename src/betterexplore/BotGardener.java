package betterexplore;

import battlecode.common.*;
import battlecode.common.RobotType;

import static battlecode.common.RobotType.*;
import static battlecode.common.Team.NEUTRAL;

final strictfp class BotGardener extends Navigation {

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {
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
        tryBuildRobot();
        GameState.senseNearbyTrees();
        tryShakeNearbyTree();
        if (tryPlantTreeRandom()) {
            GameState.senseNearbyFriends();
            tryWaterNearbyTrees();
            return;
        }
        GameState.senseNearbyFriends();
        if (tryWaterNearbyTrees()) {
            return;
        }
        for (int i = 0; i < 6 && !Navigation.tryMoveInDirection(exploreDir); i++) {
            exploreDir = exploreDir.rotateRightRads(EIGHTH_TURN);
        }
    }

    private static boolean tryBuildRobot() throws GameActionException {
        RobotType r = null;

        if (!rc.isBuildReady())
            return false;

        if (roundNum % 20 == 5 && rc.hasRobotBuildRequirements(LUMBERJACK)) {
            r = LUMBERJACK;
        }
        if (r == null)
            return false;
        Direction dir = Navigation.getRandomDirection();
        for (int i=0; i < 8; i++) {
            if (rc.canBuildRobot(r, dir)) {
                rc.buildRobot(r, dir);
                return true;
            }
            dir = dir.rotateRightRads(EIGHTH_TURN);
        }
        return false;
    }

    private static boolean tryRouteToNearbyTrees() throws GameActionException {
        if (rc.hasMoved())
            return false;
        TreeInfo worstTree = null;
        for (TreeInfo t : nearbyTrees)
            if (t.team == myTeam)
                if (worstTree == null)
                    worstTree = t;
                else if (t.getHealth() < worstTree.getHealth())
                    worstTree = t;
        if (worstTree == null)
            return false;
        Direction directionToWorstTree = myLoc.directionTo(worstTree.location);
        if (rc.canMove(directionToWorstTree)) {
            rc.move(directionToWorstTree);
            return true;
        }
        return false;
    }

    private static boolean tryWaterNearbyTrees() throws GameActionException {
        if (!rc.canWater())
            return false;
        TreeInfo worstTree = null;
        for (TreeInfo t : nearbyTrees)
            if (t.team == myTeam && rc.canWater(t.location))
                if (worstTree == null)
                    worstTree = t;
                else if (t.getHealth() < worstTree.getHealth())
                    worstTree = t;
        if (worstTree == null)
            return false;

        // This is a lottery to minimize tree-nanny gardeners
        int gardenerIdsHigherThanMine = 0;
        for (RobotInfo r : nearbyFriends)
            if (r.type == GARDENER && r.getID() > myId)
                gardenerIdsHigherThanMine++;
        if (gardenerIdsHigherThanMine < 3) {
            rc.water(worstTree.location);
            return true;
        }

        return false;
    }

    private static boolean tryPlantTreeRandom() throws GameActionException {
        if (!rc.hasTreeBuildRequirements())
            return false;
        for (int i=0; i < 8; i++) {
            Direction dir = new Direction(Navigation.getRandomRadians());
            if (!rc.canPlantTree(dir))
                continue;
            if (nearbyTrees.length < 2) {
                rc.plantTree(dir);
                exploreDir = Navigation.getRandomDirection();
            }
            return true;
        }
        return false;
    }

    private static boolean tryPlantTreeOrchard() {
        if (!rc.hasTreeBuildRequirements())
            return false;
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
