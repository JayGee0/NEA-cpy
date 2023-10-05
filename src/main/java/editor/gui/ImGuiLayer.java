package editor.gui;

import editor.io.KeyboardInput;
import editor.io.MouseInput;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

// Class mostly taken from https://github.com/codingminecraft/MarioYoutube/blob/master/src/main/java/jade/ImGuiLayer.java
public class ImGuiLayer {

    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final int windowWidth;
    private final int windowHeight;

    /**
     * Initialize all the ImGui callbacks
     * @param windowID ID of the window
     * @param width width of the screen
     * @param height height of the screen
     */
    public ImGuiLayer(long windowID, int width, int height) {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename("imgui.ini");
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setBackendPlatformName("imgui_java_imp_glfw");

        glfwSetKeyCallback(windowID, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true);
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false);
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));

            if (!io.getWantCaptureKeyboard()) {
                KeyboardInput.key_callback(w, key, scancode, action, mods);
            }
        });


        glfwSetMouseButtonCallback(windowID, (w, button, action, mods) -> {
            final boolean[] mouseDown = new boolean[5];

            mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
            mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
            mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
            mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
            mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

            io.setMouseDown(mouseDown);

            if (!io.getWantCaptureMouse() && mouseDown[1]) {
                ImGui.setWindowFocus(null);
            }

            if (!io.getWantCaptureMouse()) {
                MouseInput.mouseButton_callback(w, button, action, mods);
            }
        });

        glfwSetScrollCallback(windowID, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);

        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(windowID, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString = glfwGetClipboardString(windowID);
                if (clipboardString != null) {
                    return clipboardString;
                } else {
                    return "";
                }
            }
        });

        imGuiGlfw.init(windowID, true);
        imGuiGl3.init("#version 330");

        this.windowWidth = width;
        this.windowHeight = height;
    }


    public void startFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0,0,windowWidth,windowHeight);
        glClearColor(0,0,0,1);
        glClear(GL_COLOR_BUFFER_BIT);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        final long backupWindow = glfwGetCurrentContext();
        ImGui.updatePlatformWindows();
        ImGui.renderPlatformWindowsDefault();
        glfwMakeContextCurrent(backupWindow);

    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void setupDockspace(long window) {
        ImGuiViewport mainViewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(mainViewport.getWorkPosX(), mainViewport.getWorkPosY());
        ImGui.setNextWindowSize(mainViewport.getWorkSizeX(), mainViewport.getWorkSizeY());
        ImGui.setNextWindowViewport(mainViewport.getID());
        int[] xPos = new int[1];
        int[] yPos = new int[1];
        glfwGetWindowPos(window, xPos, yPos);
        ImGui.setNextWindowPos(xPos[0], yPos[0]);
        ImGui.setNextWindowSize(windowWidth, windowHeight);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 1);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1);
        int flags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking |
                ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize |  ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Dockspace", new ImBoolean(true), flags);
        ImGui.popStyleVar(2);

        ImGui.dockSpace(ImGui.getID("Dockspace"));
        ImGui.end();
    }
}
