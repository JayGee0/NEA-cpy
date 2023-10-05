package editor;

import editor.gui.ImGuiLayer;
import editor.gui.SceneView;
import editor.io.KeyboardInput;
import editor.io.MouseInput;
import editor.util.DebugDraw;
import editor.util.Framebuffer;
import editor.util.ResourceManager;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

// Window of the application
public class Window {

    private long windowID; // OpenGL id of the window

    // 16:9 window
    private final int width = 1600;
    private final int height = 900;

    private ImGuiLayer guiLayer;
    private Scene editorScene;
    private SceneView editorSceneView;
    private Framebuffer sceneFramebuffer;

    /**
     * Start the window, initializing all necessary OpenGL context
     * and run the window
     */
    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(windowID);
        glfwDestroyWindow(windowID);
        guiLayer.destroy();

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Initialize the OpenGL window
     */
    private void init()  {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);

        // Create the window
        windowID = glfwCreateWindow(width, height, "Physics Simulation", NULL, NULL);
        if ( windowID == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowID);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Configure I/O functions
        glfwSetKeyCallback(windowID, KeyboardInput::key_callback);
        glfwSetCursorPosCallback(windowID, MouseInput::mousePos_callback);
        glfwSetMouseButtonCallback(windowID, MouseInput::mouseButton_callback);
        glfwSetFramebufferSizeCallback(windowID, this::framebuffer_size_callback);

        glViewport(0,0, width, height);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Initialize the necessary Scenes and Framebuffer to render the environment in
        sceneFramebuffer = new Framebuffer(width, height);
        editorScene = new Scene(width * 0.01f, height * 0.01f); // Scene will be 16 units wide and 9 units tall
        editorSceneView = new SceneView(editorScene, sceneFramebuffer);
        guiLayer = new ImGuiLayer(windowID, width, height);
        DebugDraw.init();

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(windowID);
    }

    /**
     * Main loop of the window
     */
    private void loop() {
        float deltaTime = 0.0f;
        float lastFrame = 0.0f;
        glClearColor(1.0f,1.0f,1.0f,1.0f);

        while (!glfwWindowShouldClose(windowID) ) {
            KeyboardInput.resetPressed();
            MouseInput.resetPressed();
            glfwPollEvents(); // Call necessary callbacks

            glClearColor(1.0f,1.0f,1.0f,1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glViewport(0,0,width,height);

            guiLayer.startFrame();
            guiLayer.setupDockspace(windowID);

            // The following is done within the Framebuffer object
            sceneFramebuffer.bind();

            glClearColor(0.1f,0.1f,0.1f,0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            editorScene.update(deltaTime);
            editorSceneView.render();

            DebugDraw.beginFrame();
            DebugDraw.draw();

            sceneFramebuffer.unbind();
            // END of framebuffer object rendering

            guiLayer.endFrame();

            glfwSwapBuffers(windowID);

            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;
            // Keep an FPS counter in the title
            glfwSetWindowTitle(windowID, "Physics Simulation - FPS: " + (int)(1/deltaTime));
        }

        // Clear the ResourceManager of loaded shaders and textures
        ResourceManager.clear();
    }

    public void framebuffer_size_callback(long window, int width, int height) {
        glViewport(0,0,width,height);
    }

    public static void main(String[] args) {
        new Window().run();
    }

}