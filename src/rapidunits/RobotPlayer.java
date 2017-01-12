package rapidunits;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public final strictfp class RobotPlayer {

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        GameState.init(rc);

        switch(rc.getType()) {
        case ARCHON:
            BotArchon.loop();
            break;
        case GARDENER:
            BotGardener.loop();
            break;
        case SCOUT:
            BotScout.loop();
            break;
        case TANK:
            BotTank.loop();
            break;
        case LUMBERJACK:
            BotLumberjack.loop();
            break;
        case SOLDIER:
            BotSoldier.loop();
            break;
        default:
            System.out.println("FATAL! unexpected robot type: " + rc.getType());
        }
    }
}
