package editor.io;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyboardInput {
    private static final boolean[] keyDown = new boolean[1024];
    private static final boolean[] keyPressed = new boolean[1024];

    public static void key_callback(long window, int key, int scancode, int action, int mode) {
        if(action == GLFW_PRESS) {
            keyDown[key] = true;
            keyPressed[key] = true;
        }
        if(action == GLFW_RELEASE) {
            keyDown[key] = false;
            keyPressed[key] = false;
        }
    }

    public static void resetPressed() {
        Arrays.fill(keyPressed, false);
    }

    public static boolean getKeyDown(int key) {
        return keyDown[key];
    }

    public static boolean getKeyPressed(int key) {
        return keyPressed[key];
    }

}
