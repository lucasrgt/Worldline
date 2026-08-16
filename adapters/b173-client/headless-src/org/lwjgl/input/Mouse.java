package org.lwjgl.input;

import java.util.ArrayDeque;
import java.util.Deque;

/** Empty deterministic mouse event source for the initial reusable adapter. */
public final class Mouse {
    private static final Deque<Event> EVENTS = new ArrayDeque<>();
    private static final boolean[] DOWN = new boolean[8];
    private static Event current;

    private Mouse() {}

    public static boolean next() {
        current = EVENTS.pollFirst();
        if (current == null) return false;
        if (current.button >= 0 && current.button < DOWN.length) DOWN[current.button] = current.pressed;
        return true;
    }

    public static boolean isButtonDown(int button) {
        return button >= 0 && button < DOWN.length && DOWN[button];
    }
    public static int getEventDWheel() { return current == null ? 0 : current.wheel; }
    public static boolean getEventButtonState() { return current != null && current.pressed; }
    public static int getEventButton() { return current == null ? -1 : current.button; }
    public static int getEventX() { return current == null ? 0 : current.x; }
    public static int getEventY() { return current == null ? 0 : current.y; }
    public static void setGrabbed(boolean grabbed) {}

    public static void worldlinePush(int button, boolean pressed, int wheel, int x, int y) {
        EVENTS.addLast(new Event(button, pressed, wheel, x, y));
    }

    public static void worldlineReset() {
        EVENTS.clear();
        current = null;
        for (int index = 0; index < DOWN.length; index++) DOWN[index] = false;
    }

    private static final class Event {
        private final int button;
        private final boolean pressed;
        private final int wheel;
        private final int x;
        private final int y;
        private Event(int button, boolean pressed, int wheel, int x, int y) {
            this.button = button;
            this.pressed = pressed;
            this.wheel = wheel;
            this.x = x;
            this.y = y;
        }
    }
}
