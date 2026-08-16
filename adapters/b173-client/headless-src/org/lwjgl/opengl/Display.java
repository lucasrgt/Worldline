package org.lwjgl.opengl;

/** Deterministic window boundary: the controlled client never creates one. */
public final class Display {
    private Display() {}

    public static boolean isCreated() { return false; }
    public static boolean isActive() { return false; }
}
