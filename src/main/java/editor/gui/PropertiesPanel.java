package editor.gui;

import imgui.ImGui;
import math.Vector2;
import math.Vector3;
import physics.body.BodyShape;
import physics.body.BoxBody;
import physics.body.CircleBody;
import physics.body.RigidBody;

public class PropertiesPanel implements RenderableGui {
    private RigidBody activeObject;

    public PropertiesPanel(RigidBody object) {
        activeObject = object;
    }

    @Override
    public void render() {
        ImGui.begin("Properties");
        ImGui.text(activeObject.toString());

        float[] position = {activeObject.getPosition().getX(), activeObject.getPosition().getY()};
        ImGui.dragFloat2("Position", position, 0.01f);
        activeObject.setPosition(new Vector2(position[0], position[1]));

        if(activeObject.getShape() == BodyShape.CIRCLE) {
            float[] radius = {((CircleBody)activeObject).getRadius()};
            ImGui.dragFloat("Radius", radius, 0.01f, 0, 5);
            ((CircleBody) activeObject).setRadius(radius[0]);
        } else if(activeObject.getShape() == BodyShape.BOX) {
            float[] halfSize = {((BoxBody)activeObject).getHalfSize().getX(), ((BoxBody)activeObject).getHalfSize().getY()};
            ImGui.dragFloat2("HalfSize", halfSize, 0.01f, 0, 5);
            ((BoxBody) activeObject).setHalfSize(new Vector2(halfSize[0], halfSize[1]));
        }

        float[] rotation = {activeObject.getRotation()};
        ImGui.dragFloat("Rotation", rotation , 0.5f);
        activeObject.setRotation(rotation[0]);

        float[] mass = {activeObject.getMass()};
        ImGui.dragFloat("Mass", mass, 0.01f, 0, 10);
        activeObject.setMass(mass[0]);

        float[] linearVelocity = {activeObject.getLinearVelocity().getX(), activeObject.getLinearVelocity().getY()};
        ImGui.dragFloat2("Linear Velocity", linearVelocity, 0.01f);
        activeObject.setLinearVelocity(new Vector2(linearVelocity[0], linearVelocity[1]));

        float[] angularVelocity = {activeObject.getAngularVelocity()};
        ImGui.dragFloat("Angular Velocity", angularVelocity, 0.01f);
        activeObject.setAngularVelocity(angularVelocity[0]);

        float[] e = {activeObject.getCOR()};
        ImGui.dragFloat("Coefficient of Restitution", e, 0.01f, 0, 1);
        activeObject.setCOR(e[0]);

        float[] friction = {activeObject.getFriction()};
        ImGui.dragFloat("Coefficient of Friction", friction, 0.01f, 0, 1);
        activeObject.setFriction(friction[0]);

        ImGui.text("Rotational Inertia: " + activeObject.getI());

        float[] color = {activeObject.getColor().getX(), activeObject.getColor().getY(), activeObject.getColor().getZ()};
        ImGui.colorPicker3("Color", color);
        activeObject.setColor(new Vector3(color[0], color[1], color[2]));

        ImGui.end();
    }


    public RigidBody getActiveObject() {
        return activeObject;
    }

    public void setActiveObject(RigidBody activeObject) {
        this.activeObject = activeObject;
    }
}
