package editor;

// The state the editor is in
public enum EditorState {
    EDIT, // Scene is paused while being edited
    PLAY, // Scene is in play
    DEBUG // Scene will only move forward when right arrow is tapped
}
