package com.aldrineeinsteen.fun.options.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

class MousePositionTrackerTest {

    private MousePositionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new MousePositionTracker();
    }

    @Test
    void testInitialState() {
        assertNull(tracker.getLastAutoPosition());
        assertNull(tracker.getLastKnownPosition());
    }

    @Test
    void testUpdatePositionFirstTime() {
        Point position = new Point(100, 100);
        boolean userMoved = tracker.updatePosition(position);
        
        assertFalse(userMoved);
        assertNotNull(tracker.getLastKnownPosition());
        assertEquals(100, tracker.getLastKnownPosition().x);
        assertEquals(100, tracker.getLastKnownPosition().y);
    }

    @Test
    void testUpdatePositionNoUserMovement() {
        Point position1 = new Point(100, 100);
        tracker.updatePosition(position1);
        tracker.recordAutoPosition(position1);
        
        Point position2 = new Point(100, 100);
        boolean userMoved = tracker.updatePosition(position2);
        
        assertFalse(userMoved);
    }

    @Test
    void testUpdatePositionWithUserMovement() {
        Point position1 = new Point(100, 100);
        tracker.updatePosition(position1);
        tracker.recordAutoPosition(position1);
        
        Point position2 = new Point(150, 150);
        boolean userMoved = tracker.updatePosition(position2);
        
        assertTrue(userMoved);
    }

    @Test
    void testCheckAndResetUserMovement() {
        Point position1 = new Point(100, 100);
        tracker.updatePosition(position1);
        tracker.recordAutoPosition(position1);
        
        Point position2 = new Point(150, 150);
        tracker.updatePosition(position2);
        
        assertTrue(tracker.checkAndResetUserMovement());
        assertFalse(tracker.checkAndResetUserMovement()); // Should be reset
    }

    @Test
    void testRecordAutoPosition() {
        Point autoPosition = new Point(200, 200);
        tracker.recordAutoPosition(autoPosition);
        
        assertNotNull(tracker.getLastAutoPosition());
        assertEquals(200, tracker.getLastAutoPosition().x);
        assertEquals(200, tracker.getLastAutoPosition().y);
    }

    @Test
    void testCalculateNewPositionWithinBounds() {
        Point newPosition = tracker.calculateNewPosition(100, 100, 500, 500);
        
        assertNotNull(newPosition);
        // Should be 100 +/- 1
        assertTrue(newPosition.x == 99 || newPosition.x == 101);
        assertTrue(newPosition.y == 99 || newPosition.y == 101);
    }

    @Test
    void testCalculateNewPositionOutOfBoundsX() {
        Point newPosition = tracker.calculateNewPosition(600, 100, 500, 500);
        
        assertNotNull(newPosition);
        assertEquals(450, newPosition.x); // Should be constrained to screenWidth - 50
        assertEquals(100, newPosition.y); // Y should not be incremented when X is out of bounds
    }

    @Test
    void testCalculateNewPositionOutOfBoundsY() {
        Point newPosition = tracker.calculateNewPosition(100, 600, 500, 500);
        
        assertNotNull(newPosition);
        assertEquals(100, newPosition.x); // X should not be incremented when Y is out of bounds
        assertEquals(450, newPosition.y); // Should be constrained to screenHeight - 50
    }

    @Test
    void testCalculateNewPositionNegativeX() {
        Point newPosition = tracker.calculateNewPosition(-10, 100, 500, 500);
        
        assertNotNull(newPosition);
        assertEquals(50, newPosition.x); // Should be constrained to 50
    }

    @Test
    void testCalculateNewPositionNegativeY() {
        Point newPosition = tracker.calculateNewPosition(100, -10, 500, 500);
        
        assertNotNull(newPosition);
        assertEquals(50, newPosition.y); // Should be constrained to 50
    }

    @Test
    void testCalculateNewPositionAlternatingIncrement() {
        // Even x should increment by 1
        Point newPosition1 = tracker.calculateNewPosition(100, 100, 500, 500);
        assertEquals(101, newPosition1.x);
        assertEquals(101, newPosition1.y);
        
        // Odd x should decrement by 1
        Point newPosition2 = tracker.calculateNewPosition(101, 101, 500, 500);
        assertEquals(100, newPosition2.x);
        assertEquals(100, newPosition2.y);
    }

    @Test
    void testMultipleUpdatesTracking() {
        Point pos1 = new Point(100, 100);
        tracker.updatePosition(pos1);
        tracker.recordAutoPosition(pos1);
        
        Point pos2 = new Point(101, 101);
        tracker.updatePosition(pos2);
        tracker.recordAutoPosition(pos2);
        
        Point pos3 = new Point(200, 200); // User moved
        boolean userMoved = tracker.updatePosition(pos3);
        
        assertTrue(userMoved);
        assertEquals(200, tracker.getLastKnownPosition().x);
        assertEquals(101, tracker.getLastAutoPosition().x);
    }
}

// Made with Bob
