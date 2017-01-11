package team419;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.TreeInfo;

import static battlecode.common.Team.NEUTRAL;

final strictfp class BotTank extends Navigation {

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
        tryShakeNearbyTree();
        if (Navigation.tryMoveInDirection(myDir)) {
            return;
        } else {
            myDir = myDir.rotateRightRads(THIRTYSECOND_TURN);
        }
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
