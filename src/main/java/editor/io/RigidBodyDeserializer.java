package editor.io;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import physics.body.BoxBody;
import physics.body.CircleBody;
import physics.body.RigidBody;

import java.lang.reflect.Type;

public class RigidBodyDeserializer implements JsonDeserializer<RigidBody> {

    @Override
    public RigidBody deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String shape = json.getAsJsonObject().get("shape").getAsString();
        if (shape.equals("BOX")) {
            return context.deserialize(json, BoxBody.class);
        } else if (shape.equals("CIRCLE")) {
            return context.deserialize(json, CircleBody.class);
        }
        return null;
    }
}
