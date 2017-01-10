package team419;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public final strictfp class RobotPlayer {

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        switch(rc.getType()) {
        case ARCHON:
            BotArchon.loop(rc);
            break;
        case GARDENER:
            BotGardener.loop(rc);
            break;
        case SCOUT:
            BotScout.loop(rc);
            break;
        case TANK:
            BotTank.loop(rc);
            break;
        case LUMBERJACK:
            BotLumberjack.loop(rc);
            break;
        case SOLDIER:
            BotSoldier.loop(rc);
            break;
        default:
            System.out.println("FATAL! unexpected robot type: " + rc.getType());
        }
    }
}
