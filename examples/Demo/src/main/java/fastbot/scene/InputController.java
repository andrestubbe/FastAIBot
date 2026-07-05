package fastbot.scene;

import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;
import fastterminal.FastTerminal;

public class InputController {

    public interface InputListener {
        void onEscape();
        void onGestureKey(String gestureName);
        void onPrintableChar(char c);
        void onBackspace();
        void onEnter();
        void onAnyKeyPress(int vKey, String keyChar);
    }

    private final FastKeyboard keyboard;

    public InputController() {
        this.keyboard = new FastKeyboardImpl();
    }

    public void startListening(InputListener listener) {
        keyboard.startListening((handle, vKey, makeCode, isPressed, isE0, ts, keyChar) -> {
            if (!FastTerminal.isTerminalFocused()) return;

            if (isPressed) {
                listener.onAnyKeyPress(vKey, keyChar);
            }

            if (vKey == 0x1B && isPressed) {
                listener.onEscape();
                return;
            }

            if (isPressed && vKey >= 0x70 && vKey <= 0x7B) {
                if (vKey == 0x70) listener.onGestureKey("nod"); // F1
                else if (vKey == 0x71) listener.onGestureKey("shake"); // F2
                else if (vKey == 0x72) listener.onGestureKey("look_up"); // F3
                else if (vKey == 0x73) listener.onGestureKey("wink"); // F4
                else if (vKey == 0x74) listener.onGestureKey("look_down"); // F5
                else if (vKey == 0x75) listener.onGestureKey("look_left"); // F6
                else if (vKey == 0x76) listener.onGestureKey("look_right"); // F7
                else if (vKey == 0x77) listener.onGestureKey("tilt_left"); // F8
                else if (vKey == 0x78) listener.onGestureKey("tilt_right"); // F9
                else if (vKey == 0x79) listener.onGestureKey("surprise"); // F10
                else if (vKey == 0x7A) listener.onGestureKey("laugh"); // F11
                else if (vKey == 0x7B) listener.onGestureKey("sleep"); // F12
                return;
            }

            if (isPressed) {
                // TEMPORARILY DISABLED CHAT INPUT FOR GESTURE TESTING
                /*
                if (vKey == 0x0D) {
                    listener.onEnter();
                } else if (vKey == 0x08) {
                    listener.onBackspace();
                } else if (keyChar != null && keyChar.length() == 1) {
                    char c = keyChar.charAt(0);
                    if (c >= 32 && c != 127) {
                        listener.onPrintableChar(c);
                    }
                }
                */

                // MAP A-Z and 0-9 using keyChar
                if (keyChar != null && keyChar.length() > 0) {
                    char c = Character.toLowerCase(keyChar.charAt(0));
                    if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                        listener.onGestureKey("key_" + c);
                    }
                }
            }
        });
    }

    public void stopListening() {
        try {
            keyboard.stopListening();
        } catch (Throwable ignored) {
        }
    }
}
