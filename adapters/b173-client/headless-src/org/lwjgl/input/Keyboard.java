package org.lwjgl.input;

import java.util.ArrayDeque;
import java.util.Deque;

/** Empty deterministic keyboard event source for the initial reusable adapter. */
public final class Keyboard {
    private static final Deque<Event> EVENTS = new ArrayDeque<>();
    private static final boolean[] DOWN = new boolean[256];
    private static Event current;

    private Keyboard() {}

    public static boolean next() {
        current = EVENTS.pollFirst();
        if (current == null) return false;
        if (current.key >= 0 && current.key < DOWN.length) DOWN[current.key] = current.pressed;
        return true;
    }

    public static int getEventKey() { return current == null ? 0 : current.key; }
    public static boolean getEventKeyState() { return current != null && current.pressed; }
    public static char getEventCharacter() { return current == null ? 0 : current.character; }
    public static boolean isKeyDown(int key) { return key >= 0 && key < DOWN.length && DOWN[key]; }
    public static String getKeyName(int key) { return Integer.toString(key); }

    public static void worldlinePush(int key, boolean pressed, char character) {
        EVENTS.addLast(new Event(key, pressed, character));
    }

    public static void worldlineReset() {
        EVENTS.clear();
        current = null;
        for (int index = 0; index < DOWN.length; index++) DOWN[index] = false;
    }

    private static final class Event {
        private final int key;
        private final boolean pressed;
        private final char character;
        private Event(int key, boolean pressed, char character) {
            this.key = key;
            this.pressed = pressed;
            this.character = character;
        }
    }
}
