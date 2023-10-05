package editor.io;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseInput {
    private static double x, y;
    private static final boolean[] mouseButtonDown = new boolean[8];
    private static final boolean[] mouseButtonPressed = new boolean[8];


    public static void mousePos_callback(long window, double xPos, double yPos) {
        x = xPos;
        y = yPos;
    }

    public static void mouseButton_callback(long window, int button, int action, int mods) {
        if(action == GLFW_PRESS) {
            mouseButtonDown[button] = true;
            mouseButtonPressed[button] = true;
        } else if(action == GLFW_RELEASE) {
            mouseButtonDown[button] = false;
            mouseButtonPressed[button] = false;
        }
    }


    public static double getX() {
        return x;
    }

    public static double getY() {
        return y;
    }

    public static boolean getMouseButtonDown(int button) {
        return mouseButtonDown[button];
    }

    public static boolean getMouseButtonPressed(int button) {
        return mouseButtonPressed[button];
    }
    public static void resetPressed() {
        Arrays.fill(mouseButtonPressed, false);
    }

}
