package com.sebroth.snappyrecyclerview;

/**
 * Defines the "snappiness" when scrolling through a grid.
 * <p/>
 * Original source: http://stackoverflow.com/q/26370289/375209
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public interface SnappyLayoutManager {
    /**
     * @param velocityX
     * @param velocityY
     * @return the resultant position from a fling of the given velocity.
     */
    int getPositionForVelocity(int velocityX, int velocityY);

    /**
     * @return the position this list must scroll to to fix a state where the
     * views are not snapped to grid.
     */
    int getFixScrollPos();
}
