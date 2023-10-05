package editor.gui;

import editor.EditorState;
import editor.Scene;
import editor.util.Framebuffer;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

// Main panel view
public class SceneView implements RenderableGui {
    private final Scene scene;
    private final Framebuffer fb;

    private final ImString filename;

    public SceneView(Scene scene, Framebuffer fb) {
        this.scene = scene;
        this.fb = fb;

        filename = new ImString();
    }

    @Override
    public void render() {
        ImGui.begin("RigidBody simulation", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.MenuBar);

        ImGui.beginMenuBar();

        boolean displaySavePopup = false;
        boolean displayLoadPopup = false;
        boolean displayErrorPopup = false;
        if(ImGui.beginMenu("File")) {
            if(ImGui.menuItem("Save As")) {
                displaySavePopup = true;
            }

            if(ImGui.menuItem("Load")) {
                displayLoadPopup = true;
            }

            if(ImGui.menuItem("Restart")) {
                this.scene.refreshEnvironment();
            }

            if(ImGui.menuItem("Quit")) {
                System.exit(0);
            }
            ImGui.endMenu();
        }

        if(displaySavePopup) ImGui.openPopup("Save File", ImGuiWindowFlags.NoResize);
        if (ImGui.beginPopupModal("Save File", ImGuiWindowFlags.NoResize)) {
            ImGui.text("Enter the desired filename");
            ImGui.text("saves/");
            ImGui.sameLine();
            ImGui.inputText(" ", filename, ImGuiInputTextFlags.CharsNoBlank);

            if(!filename.get().equals("")) {
                if(ImGui.button("Save"))  {
                    if(this.scene.save("saves/" + filename.get())) {
                        System.out.println("saved");
                        ImGui.closeCurrentPopup();
                    } else {
                        displayErrorPopup = true;
                    }
                }
            }

            if(ImGui.button("Close")) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if(displayLoadPopup) ImGui.openPopup("Load File", ImGuiWindowFlags.NoResize);
        if (ImGui.beginPopupModal("Load File", ImGuiWindowFlags.NoResize)) {
            ImGui.text("Enter the desired filename");
            ImGui.text("saves/");
            ImGui.sameLine();
            ImGui.inputText(" ", filename, ImGuiInputTextFlags.CharsNoBlank);

            if(!filename.get().equals("")) {
                if(ImGui.button("Load"))  {
                    if(this.scene.load("saves/" + filename.get())) {
                        System.out.println("Loaded");
                        ImGui.closeCurrentPopup();
                    } else {
                        displayErrorPopup = true;
                    }
                }
            }

            if(ImGui.button("Close")) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if(displayErrorPopup) ImGui.openPopup("Error", ImGuiWindowFlags.NoResize);
        if (ImGui.beginPopupModal("Error", ImGuiWindowFlags.NoResize)) {
            ImGui.text("An error had occured when handling files");
            if(ImGui.button("Close")) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }

        if(ImGui.menuItem("Debug", "", scene.getState().equals(EditorState.DEBUG) , !scene.getState().equals(EditorState.DEBUG))) {
            scene.setState(EditorState.DEBUG);
        }
        if(ImGui.menuItem("Play", "", scene.getState().equals(EditorState.PLAY), !scene.getState().equals(EditorState.PLAY))) {
            if(scene.getState() == EditorState.EDIT) this.scene.save("saves/tmp.txt");
            scene.setState(EditorState.PLAY);
        }
        if(ImGui.menuItem("Stop", "", scene.getState().equals(EditorState.EDIT), !scene.getState().equals(EditorState.EDIT))) {
            scene.setState(EditorState.EDIT);
            if(!scene.load("saves/tmp.txt")) {
                scene.refreshEnvironment();
            }
        }

        ImGui.endMenuBar();

        ImGui.setCursorPos(ImGui.getCursorPosX(), ImGui.getCursorPosY());

        ImVec2 windowSize = getLargestViewportSize();
        ImGui.image(fb.getTexture(), windowSize.x, windowSize.y, 0, 1, 1, 0);
        ImGui.end();

        scene.render();
    }

    /**
     * Largest size of the screen that can fit with the preffered aspect ratio
     */
    private ImVec2 getLargestViewportSize() {
        ImVec2 windowSize = new ImVec2();
        ImGui.getContentRegionAvail(windowSize);

        float aspectWidth = windowSize.x;
        float aspectHeight = aspectWidth / (16f/9);
        if (aspectHeight > windowSize.y) {
            aspectHeight = windowSize.y;
            aspectWidth = aspectHeight * (16f/9);
        }

        return new ImVec2(aspectWidth, aspectHeight);
    }
}
