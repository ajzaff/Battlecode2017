package donothing;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {

    @SuppressWarnings({"unused", "InfiniteLoopStatement"})
    public static void run(RobotController rc) {
        while(true)
            Clock.yield();
    }

}
