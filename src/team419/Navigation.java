package team419;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static team419.FastMath.*;

strictfp class Navigation extends GameState {

    static final float THIRTYSECOND_TURN = 0.19634955f;
    static final float SIXTEENTH_TURN = 0.3926991f;
    static final float EIGHTH_TURN = 0.7853982f;
    static final float HEX_TURN = 1.0471976f;
    static final float QUARTER_TURN = 1.5707964f;
    static final float HALF_TURN = PI;
    static final float FULL_TURN = PI2;

    static Direction exploreDir;

    /**
     * A constant equaling 1/255 radian (i.e. (float) (1f/255 * 2*PI))
     * For multiplying rand() to get a random float direction.
     */
    private static final float RADIAN_255 = 0.024639944f;

    /**
     * Gets a random direction in radians
     * @return a random direction in radians
     */
    static float getRandomRadians() {
        return rand() * RADIAN_255;
    }

    static Direction getRandomDirection() {
        return new Direction(getRandomRadians());
    }

    static boolean tryMoveRandom() throws GameActionException {
        if (rc.hasMoved())
            return false;
        for (int i=0; i < 8; i++) {
            Direction dir = new Direction(getRandomRadians());
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }
        }
        return false;
    }

    static boolean tryMoveInDirection(Direction dir, float stride) throws GameActionException {
        if (rc.hasMoved())
            return false;
        if (rc.canMove(dir, stride)) {
            rc.move(dir, stride);
            return true;
        }
        Direction left = dir.rotateLeftRads(EIGHTH_TURN);
        if (rc.canMove(left, stride)) {
            rc.move(left, stride);
            return true;
        }
        Direction right = dir.rotateRightRads(EIGHTH_TURN);
        if (rc.canMove(right, stride)) {
            rc.move(right, stride);
            return true;
        }
        Direction leftNormal = dir.rotateLeftRads(QUARTER_TURN);
        if (rc.canMove(leftNormal, stride)) {
            rc.move(leftNormal, stride);
            return true;
        }
        Direction rightNormal = dir.rotateRightRads(QUARTER_TURN);
        if (rc.canMove(rightNormal, stride)) {
            rc.move(rightNormal, stride);
            return true;
        }
        return false;
    }

    static boolean tryMoveInDirection(Direction dir) throws GameActionException {
        return tryMoveInDirection(dir, myType.strideRadius);
    }
}
