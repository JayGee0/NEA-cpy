package editor.util;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL20.glDeleteProgram;

public class ResourceManager {
    private static final HashMap<String, Shader> shaders = new HashMap<>();
    private static final HashMap<String, Texture> textures = new HashMap<>();

    public static Shader loadShader(String vertex, String fragment, String name) {
        shaders.put(name, new Shader(vertex, fragment));
        return shaders.get(name);
    }

    public static Shader getShader(String name) {
        return shaders.get(name);
    }

    public static Texture loadTexture(String filepath, String name) {
        textures.put(name, new Texture(filepath));
        return textures.get(name);
    }

    public static Texture getTexture(String name) {
        return textures.get(name);
    }

    public static void clear() {
        for(Shader s : shaders.values()) {
            glDeleteProgram(s.shaderProgram);
        }

        for(Texture t : textures.values()) {
            glDeleteTextures(t.getTexID());
        }
    }


}
