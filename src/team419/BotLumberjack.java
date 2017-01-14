package team419;

import battlecode.common.*;
import battlecode.world.GameStats;

import static battlecode.common.GameConstants.LUMBERJACK_STRIKE_RADIUS;
import static battlecode.common.RobotType.LUMBERJACK;
import static battlecode.common.Team.NEUTRAL;

final strictfp class BotLumberjack extends Navigation {

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
        GameState.senseNearbyFriends();
        tryShakeNearbyTree();
        tryMicro();
        if (tryChopNearbyTree()) {
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

    private static boolean tryMicro() throws GameActionException {

        Direction dir;

        if (!rc.hasAttacked() && rc.canStrike()) {

            RobotInfo[] enemies = GameState.senseNearbyRobots(1 + LUMBERJACK_STRIKE_RADIUS, theirTeam);

            // Try a strike attack
            // Does not check for allies in radius!
            if (enemies.length > 0) {
                rc.strike();
            }
        }

        if (!rc.hasMoved()) {

            MapLocation enemyLoc = myLoc;
            MapLocation fleeLoc = myLoc;

            // Try to pick a fight with a nearby enemy
            // Or run away if we've already attacked this turn
            // Try to move away from friendly lumberjack strike radius
            if (nearbyEnemies.length > 0) {
                for (RobotInfo r : nearbyEnemies) {
                    float targetScore = (float) targetScore(r);
                    enemyLoc = enemyLoc.add(myLoc.directionTo(r.location), targetScore);

                    if (r.type.canAttack())  // don't flee from non-attackers
                        fleeLoc = fleeLoc.add(r.location.directionTo(myLoc), 1 - targetScore);
                }
                if (rc.hasAttacked()) {
                    dir = myLoc.directionTo(fleeLoc);
                } else {
                    dir = myLoc.directionTo(enemyLoc);
                }

                RobotInfo[] friends = GameState.senseNearbyRobots(1 + LUMBERJACK_STRIKE_RADIUS, myTeam);

                // Try to move away from friendly lumberjack strike radius
                if (friends.length > 0) {
                    MapLocation friendLoc = myLoc;

                    for (RobotInfo r : friends)
                        if (r.type == LUMBERJACK)
                            friendLoc = friendLoc.add(myLoc.directionTo(r.location));

                    dir = friendLoc.directionTo(myLoc);
                    if (dir != null) {
                        Navigation.tryMoveInDirection(dir);
                    }
                }

                // Finally, move in the correct direction
                if (dir != null && Navigation.tryMoveInDirection(dir)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static double targetScore(RobotInfo r) {
        double v = 0;

        switch (r.type) {
        case ARCHON:
            v = .5 * (1 - r.health / r.type.maxHealth);
            break;
        case SOLDIER:
            v = .1 * (1 - r.health / r.type.maxHealth);
            break;
        case LUMBERJACK:
            v = 1 * (1 - r.health / r.type.maxHealth);
            break;
        case TANK:
            v = .9 * (1 - r.health / r.type.maxHealth);
            break;
        case GARDENER:
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

    private static boolean tryChopNearbyTree() throws GameActionException {
        if (rc.hasAttacked())
            return false;
        TreeInfo bestTree = null;
        for (TreeInfo t : nearbyTrees)
            if ((t.team == NEUTRAL || t.team == theirTeam) && t.getContainedBullets() == 0 && rc.canChop(t.location))
                if (bestTree == null) {
                    bestTree = t;
                } else if (t.getContainedRobot() != null) {
                    if (bestTree.getContainedRobot() == null)
                        bestTree = t;
                    else if (t.getContainedRobot().attackPower > bestTree.getContainedRobot().attackPower)
                        bestTree = t;
                }
        if (bestTree == null)
            return false;
        rc.chop(bestTree.location);
        return true;
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
