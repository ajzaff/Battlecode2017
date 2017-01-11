package team419;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

final strictfp class BotTank extends GameState {

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop() {
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
        if (Navigation.tryMoveRandom()) {
            return;
        }
    }
}
