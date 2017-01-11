package team419;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

import static battlecode.common.Team.NEUTRAL;

final strictfp class BotLumberjack extends Navigation {

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {
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
        GameState.senseNearbyTrees();
        if (tryChopNearbyTree()) {
            return;
        }
        if (Navigation.tryMoveInDirection(myDir)) {
            return;
        } else {
            myDir = myDir.rotateRightRads(THIRTYSECOND_TURN);
        }
    }

    private static boolean tryChopNearbyTree() throws GameActionException {
        if (rc.hasAttacked())
            return false;
        for (TreeInfo t : nearbyTrees) {
            if (t.team == NEUTRAL && rc.canChop(t.location)) {
                rc.chop(t.location);
            }
        }
        return false;
    }
}
