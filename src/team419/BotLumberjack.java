package team419;

import battlecode.common.Clock;
import battlecode.common.RobotController;

final strictfp class BotLumberjack extends GameState {

    @SuppressWarnings("InfiniteLoopStatement")
    static void loop(RobotController rc) {
        while (true) {
            int begin = rc.getRoundNum();
            act();
            GameState.update();
            int end = rc.getRoundNum();
            if (begin != end) {
                System.out.println("ERROR! overflowing bytecode limit");
            }
            Clock.yield();
        }
    }

    private static void act() {

    }
}
