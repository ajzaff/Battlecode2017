package team419;

import battlecode.common.*;

import static battlecode.common.GameConstants.VICTORY_POINTS_TO_WIN;
import static battlecode.common.RobotType.GARDENER;
import static battlecode.common.Team.NEUTRAL;
import static team419.FastMath.rand;

final strictfp class BotScout extends Navigation {

    private static int rotateRandomness;

    // Gardener agro
    private static RobotInfo targetGardener;
    private static float lastKnownTargetHealth = -1;

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {
        FastMath.initRand(rc);
        exploreDir = myLoc.directionTo(theirInitArchonLocs[rand() % theirInitArchonLocs.length]);
        rotateRandomness = rand() % 2 == 0? 1 : -1;

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
        GameState.senseNearbyEnemies();
        tryShakeNearbyTree();
        if (tryMicro()) {
            return;
        }
        tryExploreMap();
    }

    private static boolean tryExploreMap() throws GameActionException {
        if (rc.hasMoved())
            return false;

        for (int i = 0; i < 6; i++) {
            if (rc.canMove(exploreDir)) {
                rc.move(exploreDir);
                return true;
            }
            exploreDir = exploreDir.rotateRightRads(rotateRandomness * EIGHTH_TURN);
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

    private static boolean tryMicro() throws GameActionException {

        Direction dir;

        if (nearbyEnemies.length > 0) {
            // Try to find a gardener to agro
            boolean foundSameGardener = targetGardener == null;
            for (RobotInfo r : nearbyEnemies) {
                if (r.type == GARDENER) {
                    if (targetGardener != null && targetGardener.getID() == r.getID())
                        foundSameGardener = true;
                    targetGardener = r;
                    break;
                }
            }
            // gardener was killed or disappeared, reset target
            if (!foundSameGardener) {
                resetTargetGardener();
            }
        } else {
            resetTargetGardener();
        }

        if (targetGardener != null) {

            dir = myLoc.directionTo(targetGardener.location);

            // Can't occupy same location as target gardener!
            // Reset the target and return false.
            if (dir == null) {
                resetTargetGardener();
                return false;
            }

            // We're close enough to the gardener to micro it.
            if (myLoc.distanceTo(targetGardener.location) < 2.5) {

                // Try to skirt around the target if we're not dealing damage.
                if (lastKnownTargetHealth == targetGardener.health) {
                    Direction skirtDir = dir.rotateRightRads(Navigation.QUARTER_TURN);
                    if (rc.canMove(skirtDir))
                        rc.move(skirtDir);
                }

                lastKnownTargetHealth = targetGardener.health;
                dir = rc.getLocation().directionTo(targetGardener.location);

                // If can fire shot, then fire.
                if (rc.canFireSingleShot()) {
                    rc.fireSingleShot(dir);
                    return true;
                }
            } else {
                Navigation.tryMoveInDirection(dir);
                return true;
            }
        }

        return false;
    }

    private static void resetTargetGardener() {
        targetGardener = null;
        lastKnownTargetHealth = -1;
    }
}
