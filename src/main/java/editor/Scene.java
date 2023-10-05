package editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import editor.gui.PropertiesPanel;
import editor.gui.RenderableGui;
import editor.io.KeyboardInput;
import editor.io.RigidBodyDeserializer;
import editor.util.DebugDraw;
import editor.util.ResourceManager;
import editor.util.SpriteRenderer;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import math.Vector2;
import math.Vector3;
import org.joml.Matrix4f;
import physics.Environment;
import physics.body.BodyShape;
import physics.body.BoxBody;
import physics.body.CircleBody;
import physics.body.RigidBody;
import physics.quadtree.QuadNode;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;

import static org.lwjgl.glfw.GLFW.*;

// The main scene
public class Scene implements RenderableGui {
    private EditorState state;
    private final float width;
    private final float height;

    private SpriteRenderer renderer;
    private Environment env;
    private final Stack<RigidBody> actionStack; //Stack used for undo function

    private float debounce = 0.1f; // 100 ms time delay when holding right arrow for debug mode

    // ImGui configs
    private final ImBoolean drawQuadtree;
    private final ImInt qtNodeCapacity;
    private final ImInt qtMaxDepth;
    private final ImInt iterations;
    private final ImBoolean drawExtents;
    private final ImFloat gravity;
    private final ImFloat debounceTime;
    private final ImInt activeObject;

    /**
     * Constructor for the scene
     * @param width Units of width of the scene
     * @param height Units of height of the scene
     */
    public Scene(float width, float height) {
        this.state = EditorState.EDIT;
        this.width = width;
        this.height = height;
        env = new Environment(new Vector2(0f,-10f), width, height);
        actionStack = new Stack<>();

        drawQuadtree = new ImBoolean(false);
        drawExtents = new ImBoolean(false);
        gravity = new ImFloat(env.getGravity());
        qtNodeCapacity = new ImInt(4);
        qtMaxDepth = new ImInt(4);
        iterations = new ImInt(15);
        debounceTime = new ImFloat(0.1f);
        activeObject = new ImInt(0);

        init();
    }

    /**
     * Initialise the scene, loading shaders and projection vector
     */
    private void init() {
        // Loading both shader-programs into the resource manager, sprite to be used for the environment and debug to be used for DebugDraw
        ResourceManager.loadShader("assets/shaders/sprite/vertex.vs", "assets/shaders/sprite/fragment.fs", "sprite");
        ResourceManager.loadShader("assets/shaders/debug/vertex.vs", "assets/shaders/debug/fragment.fs", "debug");
        // Projection matrix for the shader, defines the bounds of the screen
        Matrix4f projection = new Matrix4f().ortho(0.0f,width,0.0f, height, -1.0f, 1.0f);

        ResourceManager.getShader("sprite").uploadMat4("projection", projection);
        ResourceManager.getShader("debug").uploadMat4("projection", projection);
        renderer = new SpriteRenderer(ResourceManager.getShader("sprite"));

        ResourceManager.loadTexture("assets/textures/circle.png", "circle");
        ResourceManager.loadTexture("assets/textures/box.png", "box");
    }

    /**
     * Update the scene, used every frame
     * @param dt delta time - time between each frame in seconds
     */
    public void update(float dt) {
        // Delete object with delete button
        if(KeyboardInput.getKeyPressed(GLFW_KEY_DELETE) && env.getObjects().size() > 0) {
            RigidBody objectToRemove = env.getObjects().get(activeObject.get());
            removeObjectFromEnvironment(objectToRemove);
        }

        // Duplicate object ctrl+d
        if(KeyboardInput.getKeyDown(GLFW_KEY_LEFT_CONTROL) && KeyboardInput.getKeyPressed(GLFW_KEY_D) && env.getObjects().size() > 0) {
            RigidBody duplicatedObject = env.getObjects().get(activeObject.get()).clone();
            addObjectToEnvironment(duplicatedObject);
        }

        // Undo function crtl+z
        if(KeyboardInput.getKeyDown(GLFW_KEY_LEFT_CONTROL) && KeyboardInput.getKeyPressed(GLFW_KEY_Z) && actionStack.size() > 0) {
            RigidBody objectToRemove = actionStack.pop();
            removeObjectFromEnvironment(objectToRemove);
        }


        updateEnvironment(dt);
    }

    /**
     * Update the environment
     * @param dt Delta Time - time between frames in seconds
     */
    public void updateEnvironment(float dt) {
        // discrete timestep of 10 ms
        float timeStep = 0.01f;

        if(state == EditorState.PLAY) {
            // Fixed Update
            // If time between each timestep is greater than usual... break it up
            while(dt > timeStep) {
                env.update(timeStep);
                dt -= timeStep;
            }
            env.update(timeStep);
        }

        if(state == EditorState.DEBUG) {
            debounce -= dt;
            // When right arrow pressed and debounce time reached, update
            if(KeyboardInput.getKeyDown(GLFW_KEY_RIGHT) && debounce < 0) {
                env.update(timeStep);
                debounce = debounceTime.get();
            }
        }
    }

    /**
     * Create a fresh new environment
     */
    public void refreshEnvironment() {
        this.env = new Environment(new Vector2(0f,-10f), width, height);
        activeObject.set(0);
    }

    @Override
    public void render() {
        drawControls();

        // Get the sprite renderer to draw each object in the scene
        for(RigidBody b : env.getObjects()) {
            if(b.getShape() == BodyShape.BOX) {
                BoxBody box = (BoxBody) b;
                renderer.drawSprite(ResourceManager.getTexture("box"),
                        new Vector2(box.getPosition()).add(new Vector2(box.getHalfSize()).mul(-1f)),
                        new Vector2(box.getHalfSize()).mul(2f),
                        box.getRotation(),
                        b.getColor());
            } else if(b.getShape() == BodyShape.CIRCLE) {
                CircleBody circle = (CircleBody) b;
                renderer.drawSprite(ResourceManager.getTexture("circle"),
                        new Vector2(circle.getPosition()).add(-circle.getRadius(), -circle.getRadius()),
                        new Vector2(circle.getRadius()).mul(2f),
                        circle.getRotation(),
                        b.getColor());
            }
        }

        if(drawQuadtree.get()) {
            for(QuadNode<RigidBody> n : env.getQuadTree().getAllNodes()) {
                DebugDraw.addBox(n.getBounds().getPosition(), n.getBounds().getHalfSize(), new Vector3(1,0,0), 2);
            }
        }

        if(drawExtents.get()) {
            for(RigidBody b : env.getObjects()) {
                DebugDraw.addBox(b.getBounds().getPosition(), b.getBounds().getHalfSize(), new Vector3(0,0,1), 2);
            }
        }

        drawActiveObjectHighlighter();
    }

    /**
     * Draw all GUI elements
     */
    public void drawControls() {
        drawEditorConfig();

        drawEditorBlocks();

        drawPropertiesPanel();
    }

    private void drawEditorConfig() {
        ImGui.begin("Config");

        ImGui.text("Environment options");
        ImGui.text("Amount of objects: " + env.getObjects().size());
        ImGui.sliderFloat("Gravity", gravity.getData(), -10f, 10f);
        ImGui.inputInt("Node Capacity", qtNodeCapacity);
        ImGui.inputInt("Max Depth", qtMaxDepth);
        ImGui.inputInt("Iterations", iterations);
        if(qtNodeCapacity.get() < 1) qtNodeCapacity.set(1);
        if(qtMaxDepth.get() < 1) qtMaxDepth.set(1);
        if(iterations.get() < 1) iterations.set(1);


        env.setGravity(gravity.get());
        env.setNodeCapacity(qtNodeCapacity.get());
        env.setMaxDepth(qtMaxDepth.get());
        env.setIterations(iterations.get());

        ImGui.text("");
        ImGui.text("Editor options");
        ImGui.checkbox("Draw Quadtree", drawQuadtree);
        ImGui.checkbox("Draw Bounding Boxes", drawExtents);
        ImGui.inputFloat("Debug Debounce Time", debounceTime, 0.05f);
        // ImGui.inputInt("Active Object", activeObject);
        ImGui.combo("Active Object", activeObject, objectListDisplay());
        ImGui.end();
    }

    private void drawEditorBlocks() {
        ImGui.begin("Blocks");

        if(ImGui.imageButton(ResourceManager.getTexture("box").getTexID(), 50, 50)) {
            BoxBody box = new BoxBody(new Vector2(width/2f, height/2f), 0, 1f, new Vector2(0.3f,0.3f));
            box.setCOR(0);
            addObjectToEnvironment(box);
        }

        if(ImGui.getContentRegionAvailX() > 120) ImGui.sameLine();

        if(ImGui.imageButton(ResourceManager.getTexture("circle").getTexID(), 50, 50)) {
            CircleBody circle = new CircleBody(new Vector2(width/2f, height/2f), 0, 1f, 0.3f);
            circle.setColor(new Vector3(1,0,0));
            addObjectToEnvironment(circle);
        }

        ImGui.end();
    }

    private void drawPropertiesPanel() {
        if(activeObject.get() >= 0 && activeObject.get() < env.getObjects().size()) {
            RigidBody object = env.getObjects().get(activeObject.get());
            PropertiesPanel properties = new PropertiesPanel(object);
            properties.render();
        } else {
            activeObject.set(0);
        }
    }

    private void drawActiveObjectHighlighter() {
        if(activeObject.get() >= 0 && activeObject.get() < env.getObjects().size()) {
            RigidBody object = env.getObjects().get(activeObject.get());
            DebugDraw.addBox(object.getBounds().getPosition(), object.getBounds().getHalfSize(), new Vector3(0,1,1), 2);
        }
    }

    public EditorState getState() {
        return state;
    }

    public void setState(EditorState state) {
        this.state = state;
    }

    private String[] objectListDisplay() {
        List<RigidBody> list = env.getObjects();
        String[] output = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            output[i] = list.get(i).toString();
        }
        return output;
    }

    /**
     * Add an object to the environment and change object selection
     * @param object object to add
     */
    private void addObjectToEnvironment(RigidBody object) {
        env.addObject(object);
        actionStack.push(object);
        if(object.getShape() == BodyShape.BOX) activeObject.set(0);
        if(object.getShape() == BodyShape.CIRCLE) activeObject.set(env.getObjects().size()-1);

        if(state == EditorState.PLAY) setState(EditorState.DEBUG);
    }

    /**
     * Remove and object from the environment and change object selection
     * @param object object to remove
     */
    private void removeObjectFromEnvironment(RigidBody object) {
        env.removeObject(object);
        actionStack.remove(object);
        if(activeObject.get() < 0) activeObject.set(0);
        if(activeObject.get() > env.getObjects().size()-1) activeObject.set(env.getObjects().size()-1);
        if(state == EditorState.PLAY) setState(EditorState.DEBUG);
    }

    /**
     * Save the environment using gson
     * @param filename File to save the environment to
     * @return Environment saved successfully
     */
    public boolean save(String filename) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(gson.toJson(this.env));
            writer.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Load the environment from a file
     * @param filename File containing environment data
     * @return Environment has been loaded successfully
     */
    public boolean load(String filename) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(RigidBody.class, new RigidBodyDeserializer())
                .create();

        String fileContents;
        try {
            fileContents = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            return false;
        }

        this.env = gson.fromJson(fileContents, Environment.class);

        gravity.set(this.env.getGravity());
        qtNodeCapacity.set(this.env.getNodeCapacity());
        qtMaxDepth.set(this.env.getMaxDepth());
        iterations.set(this.env.getIterations());

        return true;
    }

}
