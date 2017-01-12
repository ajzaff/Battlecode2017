package team419;

import battlecode.common.*;
import battlecode.common.RobotType;

import static battlecode.common.RobotType.*;
import static battlecode.common.Team.NEUTRAL;

final strictfp class BotGardener extends Navigation {

    private static boolean foundOrchard;

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {
        FastMath.initRand(rc);

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
        GameState.senseNearbyFriends();
        tryShakeNearbyTree();
        tryWaterNearbyTrees();
        if (!tryRouteToOrchard()) {
            tryPlantTree();
        }
    }

    private static boolean tryRouteToOrchard() throws GameActionException {

        if (foundOrchard)
            return false;

        MapLocation newLoc = myLoc;

        if (nearbyFriends.length > 0) {

            // Find a new location ideally away from existing gardeners
            for (RobotInfo r : nearbyFriends)
                if (r.team == myTeam && r.type == GARDENER && myLoc.distanceSquaredTo(r.location) <= 64)
                    newLoc = newLoc.add(myLoc.directionTo(r.location));

            Direction dir = newLoc.directionTo(myLoc);
            if (dir != null && Navigation.tryMoveInDirection(dir)) {
                return true;
            }

            // Found a suitable location to plant
            foundOrchard = true;

        }

        return false;
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

    private static boolean tryPlantTree() throws GameActionException {
        if (!rc.hasTreeBuildRequirements())
            return false;

        // Refuse to plant on dense maps or near many trees
        if (nearbyTrees.length >= 7) {
            return false;
        }

        Direction dir = new Direction(0);
        Direction plantDir = null;
        int openDirs = 0;

        // Try to plant a tree but leave space to spawn something
        for (int i=0; i < 8; i++) {
            if (rc.canPlantTree(dir)) {
                openDirs++;
                if (plantDir == null)
                    plantDir = dir;
            }
            dir = dir.rotateRightRads(HEX_TURN);
        }

        // Refuse to block self entirely by trees
        if (openDirs < 2) {
            return false;
        }

        rc.plantTree(plantDir);
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
