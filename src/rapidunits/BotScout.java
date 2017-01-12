package rapidunits;

import battlecode.common.*;

import static battlecode.common.GameConstants.LUMBERJACK_STRIKE_RADIUS;
import static battlecode.common.RobotType.LUMBERJACK;
import static battlecode.common.Team.NEUTRAL;

final strictfp class BotScout extends Navigation {

    private static int rotateRandomness;

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {
        FastMath.initRand(rc);
        exploreDir = Navigation.getRandomDirection();
        rotateRandomness = FastMath.rand() % 2 == 0? 1 : -1;

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
        GameState.senseNearbyTrees();
        GameState.senseNearbyEnemies();
        tryShakeNearbyTree();
        tryMicro();
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

        if (!rc.hasAttacked()) {

            // Try to shoot
            if (nearbyEnemies.length > 0) {

                RobotInfo bestTarget = null;
                double bestScore = -99999;
                float distance = 0;

                for (RobotInfo r : nearbyEnemies) {
                    double score = targetScore(r);
                    if (score > bestScore) {
                        bestTarget = r;
                        bestScore = score;
                        distance = myLoc.distanceTo(r.location);
                    }
                }

                if (bestTarget != null && rc.canFireSingleShot()) {
                    rc.fireSingleShot(myLoc.directionTo(bestTarget.location));
                }
            }
        }

        if (!rc.hasMoved()) {

            MapLocation enemyLoc = myLoc;
            Direction dir = null;

            // Try to pick a fight with a nearby enemy
            // Or run away if we've already attacked this turn
            if (nearbyEnemies.length > 0) {
                for (RobotInfo r : nearbyEnemies) {
                    enemyLoc = enemyLoc.add(myLoc.directionTo(r.location));
                }
                if (rc.hasAttacked()) {
                    dir = enemyLoc.directionTo(myLoc);
                } else {
                    dir = myLoc.directionTo(enemyLoc);
                }
                if (Navigation.tryMoveInDirection(dir)) {
                    return true;
                }
            }

            RobotInfo[] friends = GameState.senseNearbyRobots(1 + LUMBERJACK_STRIKE_RADIUS, myTeam);
            MapLocation friendLoc = myLoc;

            // Try to move away from friendly lumberjack strike radius
            for (RobotInfo r : friends)
                if (r.type == LUMBERJACK)
                    friendLoc = friendLoc.add(myLoc.directionTo(friendLoc));

            dir = friendLoc.directionTo(myLoc);
            if (dir != null) {
                Navigation.tryMoveInDirection(dir);
            }
        }

        return true;
    }

    private static double targetScore(RobotInfo r) {
        double v;

        switch (r.type) {
        case GARDENER:
            v = 2 * (1 - r.health / r.type.maxHealth);
            break;
        case ARCHON:
            v = 1 * (1 - r.health / r.type.maxHealth);
            break;
        case SOLDIER:
            v = .9 * (1 - r.health / r.type.maxHealth);
            break;
        case LUMBERJACK:
            v = .8 * (1 - r.health / r.type.maxHealth);
            break;
        case TANK:
            v = .7 * (1 - r.health / r.type.maxHealth);
            break;
        case SCOUT:
            v = .1 * (1 - r.health / r.type.maxHealth);
            break;
        default:
            v = 0;
        }
        if (r.team == myTeam)
            v *= -1;
        return v;
    }
}
