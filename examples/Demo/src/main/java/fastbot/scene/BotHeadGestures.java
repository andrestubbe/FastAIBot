package fastbot.scene;

import fastanimation.Animation;
import fastanimation.FastAnimation;
import fastsoftware3d.scene.ModelNode;
import fasttween.FastTween;
import fasttween.Ease;

import java.util.function.Consumer;

public class BotHeadGestures {
    // Magic Numbers & Constants

    private static final long BLINK_BASE_INTERVAL = 3000;
    private static final long BLINK_RANDOM_ADD = 4000;
    private static final long BLINK_DOUBLE_CHANCE = 25; // 25% chance of double blink

    private static final long WINK_DURATION = 280;
    private static final long BLINK_DURATION_MIN = 90;
    private static final long BLINK_DURATION_RANDOM = 50;
    private static final long BLINK_DOUBLE_GAP = 80;

    private static final long GESTURE_NOD_DURATION = 800;
    private static final long GESTURE_SHAKE_DURATION = 1000;
    private static final long GESTURE_LOOKUP_DURATION = 1400;

    private static final long USER_TYPING_FOCUS_DURATION = 260;
    private static final long IDLE_FOCUS_RELEASE_DURATION = 600;

    private static final float GAZE_DOWN_PITCH = 0.13f;
    private static final float GAZE_LEFT_YAW = -0.12f;
    private static final float GAZE_RIGHT_YAW = 0.12f;

    private final ModelNode headNode;
    private final BotHeadTextureDrawer textureDrawer;

    // Active animations — stored so they can be stopped on interrupt
    private Animation currentGestureAnim;
    private Animation currentGazeAnim;

    private volatile boolean gestureInProgress = false;
    private long gestureEndTime = 0;

    private long lastBlinkTime = System.currentTimeMillis();
    private long blinkInterval = BLINK_BASE_INTERVAL + (long) (Math.random() * BLINK_RANDOM_ADD);
    private long eyeCloseDuration = 0;
    private int blinkPhase = 0;
    private boolean winkLeftOnly = false;
    private boolean winkRightOnly = false;
    private String lastGestureName = "";

    private float eyeOffsetX = 0f;
    private float eyeOffsetY = 0f;
    private float mouthOffsetX = 0f;
    private float mouthOffsetY = 0f;
    private float eyeScale = 1.0f; // 1.0f is default

    private boolean isSpeaking = false;

    private boolean userIsTyping = false;
    private boolean wasUserTyping = false;
    private long lastMoveTime = System.currentTimeMillis();

    public BotHeadGestures(ModelNode headNode, BotHeadTextureDrawer textureDrawer) {
        this.headNode = headNode;
        this.textureDrawer = textureDrawer;
    }

    public boolean isPerformingGesture() {
        return gestureInProgress || System.currentTimeMillis() < gestureEndTime;
    }

    // ─── Stop any running gesture animation ───────────────────────────────────
    private void stopCurrentGesture() {
        if (currentGestureAnim != null) {
            currentGestureAnim.stop();
            currentGestureAnim = null;
        }
    }

    // ─── Helper: build a single-axis sequence (go to target, hold, return to base) ────
    private Animation axisSequence(float from, float target, float base, long out, long hold, long in,
                                   Consumer<Float> setter) {
        if (target == base && from == base) return null;
        if (hold > 0) {
            return FastAnimation.sequence(
                    FastTween
                            .to(from, target, out)
                            .ease(Ease.QUAD_OUT)
                            .onUpdate(setter),
                    FastTween
                            .to(target, target, hold),
                    FastTween
                            .to(target, base, in)
                            .ease(Ease.QUAD_IN_OUT)
                            .onUpdate(setter)
            );
        } else {
            return FastAnimation.sequence(
                    FastTween
                            .to(from, target, out)
                            .ease(Ease.QUAD_OUT)
                            .onUpdate(setter),
                    FastTween
                            .to(target, base, in)
                            .ease(Ease.QUAD_IN_OUT)
                            .onUpdate(setter)
            );
        }
    }

    private Animation axisSequence(float from, float target, long out, long hold, long in, Consumer<Float> setter) {
        return axisSequence(from, target, 0.0f, out, hold, in, setter);
    }


    // ─── Trigger Methods ──────────────────────────────────────────────────────
    public synchronized void triggerNod() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + GESTURE_NOD_DURATION;

        final long duration1 = (long) (Math.random() * 150 + 150f);
        final long duration2 = (long) (Math.random() * 150 + 150f);
        final long duration3 = Math.max(duration1, duration2);

        currentGestureAnim = FastAnimation.parallel(
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().roll, 0.15f, duration1)
                                .ease(Ease.QUAD_OUT)
                                .onUpdate(v -> headNode.getTransform().roll = v),
                        FastTween
                                .to(0.15f, 0f, duration2)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().roll = v)
                ),
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().yaw, 0.0f, duration3)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().yaw = v)
                ),
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().pitch, 0.0f, duration3)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().pitch = v)
                )
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerShake() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + GESTURE_SHAKE_DURATION;

        final long duration1 = (long) (Math.random() * 125 + 125);
        final long duration2 = (long) (Math.random() * 150 + 125f);
        final long duration3 = (long) (Math.random() * 125 + 125f);
        final long duration4 = (long) (Math.random() * 125 + 125f);
        final long duration5 = Math.max(Math.max(Math.max(duration1, duration2), duration3), duration4);

        currentGestureAnim = FastAnimation.parallel(
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().yaw, 0.25f, duration1)
                                .ease(Ease.QUAD_OUT)
                                .onUpdate(v -> headNode.getTransform().yaw = v),
                        FastTween
                                .to(0.25f, -0.25f, duration2)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().yaw = v),
                        FastTween
                                .to(-0.25f, 0.12f, duration3)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().yaw = v),
                        FastTween
                                .to(0.12f, 0.0f, duration4)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().yaw = v)
                ),
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().roll, 0.0f, duration5)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().roll = v)
                ),
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().pitch, 0.0f, duration5)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().pitch = v)
                )
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerLookUp() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + GESTURE_LOOKUP_DURATION;

        final long duration1 = (long) (Math.random() * 200 + 150f);
        final long duration2 = (long) (Math.random() * 400 + 200f);
        final long duration3 = (long) (Math.random() * 200 + 150f);

        currentGestureAnim = FastAnimation.parallel(
                FastAnimation.sequence(
                        FastTween
                                .to(headNode.getTransform().roll, -0.22f, duration1)
                                .ease(Ease.QUAD_OUT)
                                .onUpdate(v -> headNode.getTransform().roll = v),
                        FastTween
                                .to(-0.22f, -0.22f, duration2),
                        FastTween
                                .to(-0.22f, 0.0f, duration3)
                                .ease(Ease.QUAD_IN_OUT)
                                .onUpdate(v -> headNode.getTransform().roll = v)
                )
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerLaugh() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 900;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().roll, -0.10f, 150)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v),
                FastTween
                        .to(-0.10f, 0.05f, 150)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v),
                FastTween
                        .to(0.05f, -0.08f, 150)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v),
                FastTween
                        .to(-0.08f, 0.0f, 300)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v)
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerSleep() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 2000;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().roll, 0.25f, 1000)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v),
                FastTween
                        .to(0.25f, 0.0f, 800)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v)
        );
        currentGestureAnim.start();

        // Force eyes closed for 1500ms
        winkLeftOnly = false;
        winkRightOnly = false;
        textureDrawer.setEyesClosed(true, true, true);
        blinkPhase = 1;
        eyeCloseDuration = 1500;
        lastBlinkTime = System.currentTimeMillis();
    }

    public synchronized void triggerWink() {
        winkLeftOnly = true;
        winkRightOnly = false;
        textureDrawer.setEyesClosed(true, true, false);
        blinkPhase = 1;
        eyeCloseDuration = WINK_DURATION;
        lastBlinkTime = System.currentTimeMillis();
    }

    public synchronized void triggerLookDown() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 1200;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().roll, 0.20f, 400)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v),
                FastTween
                        .to(0.20f, 0.0f, 600)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v)
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerLookLeft() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 1000;

        currentGestureAnim = FastAnimation.sequence(
                FastTween.to(headNode.getTransform().yaw, -0.30f, 300)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().yaw = v),
                FastTween.to(-0.30f, 0.0f, 500)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().yaw = v)
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerLookRight() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 1000;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().yaw, 0.30f, 300)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().yaw = v),
                FastTween
                        .to(0.30f, 0.0f, 500)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().yaw = v)
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerTiltLeft() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 1200;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().pitch, -0.20f, 400)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().pitch = v),
                FastTween
                        .to(-0.20f, 0.0f, 600)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().pitch = v)
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerTiltRight() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 1200;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().pitch, 0.20f, 400)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().pitch = v),
                FastTween
                        .to(0.20f, 0.0f, 600)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().pitch = v)
        );
        currentGestureAnim.start();
    }

    public synchronized void triggerSurprise() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + 800;

        currentGestureAnim = FastAnimation.sequence(
                FastTween
                        .to(headNode.getTransform().roll, -0.15f, 150)
                        .ease(Ease.QUAD_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v),
                FastTween
                        .to(-0.15f, 0.0f, 400)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> headNode.getTransform().roll = v)
        );
        currentGestureAnim.start();
    }

    public void setSpeaking(boolean speaking) {
        this.isSpeaking = speaking;
    }

    private void executeGesture(float pPitch, float pYaw, float pRoll, long out, long hold, long in) {
        executeGestureFull(pPitch, pYaw, pRoll, 0f, 0f, 0f, 0f, 1.0f, out, hold, in, 200);
    }

    private void executeGestureFull(float pPitch, float pYaw, float pRoll, float eX, float eY, float mX, float mY, long out, long hold, long in, long pause) {
        executeGestureFull(pPitch, pYaw, pRoll, eX, eY, mX, mY, 1.0f, out, hold, in, pause);
    }

    private void executeGestureFull(float pPitch, float pYaw, float pRoll, float eX, float eY, float mX, float mY, float eScale, long out, long hold, long in, long pause) {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        gestureEndTime = System.currentTimeMillis() + out + hold + in;

        // Build individual axis animations, skip null ones
        float rollFrom = headNode.getTransform().roll;
        float yawFrom = headNode.getTransform().yaw;
        float pitchFrom = headNode.getTransform().pitch;

        // Collect non-null animations for parallel
        java.util.List<Animation> parts = new java.util.ArrayList<>();

        if (pPitch != 0.0f) {
            parts.add(axisSequence(rollFrom, pPitch, out, hold, in, v -> headNode.getTransform().roll = v));
        }
        if (pYaw != 0.0f) {
            parts.add(axisSequence(yawFrom, pYaw, out, hold, in, v -> headNode.getTransform().yaw = v));
        }
        if (pRoll != 0.0f) {
            parts.add(axisSequence(pitchFrom, pRoll, out, hold, in, v -> headNode.getTransform().pitch = v));
        }
        if (eX != 0.0f) {
            parts.add(axisSequence(eyeOffsetX, eX, out, hold, in, v -> {
                eyeOffsetX = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }));
        }
        if (eY != 0.0f) {
            parts.add(axisSequence(eyeOffsetY, eY, out, hold, in, v -> {
                eyeOffsetY = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }));
        }
        if (mX != 0.0f) {
            parts.add(axisSequence(mouthOffsetX, mX, out, hold, in, v -> {
                mouthOffsetX = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }));
        }
        if (mY != 0.0f) {
            parts.add(axisSequence(mouthOffsetY, mY, out, hold, in, v -> {
                mouthOffsetY = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }));
        }
        if (eScale != 1.0f || eyeScale != 1.0f) {
            parts.add(axisSequence(eyeScale, eScale, 1.0f, out, hold, in, v -> {
                eyeScale = v;
                textureDrawer.setEyeScale(eyeScale);
            }));
        }

        if (parts.isEmpty()) return;

        if (parts.size() == 1) {
            currentGestureAnim = parts.get(0);
        } else {
            currentGestureAnim = FastAnimation.parallel(parts.toArray(new Animation[0]));
        }
        currentGestureAnim.start();
    }

    public synchronized void triggerTestDisplacement() {
        if (headNode == null) return;
        stopCurrentGesture();
        gestureInProgress = true;
        // Total duration of all parts
        gestureEndTime = System.currentTimeMillis() + 8000;

        currentGestureAnim = FastAnimation.sequence(
            // --- Part 1: Eye X Offset (left to right) ---
            FastTween.to(eyeOffsetX, -5f, 600).ease(Ease.QUAD_OUT).onUpdate(v -> {
                eyeOffsetX = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }),
            FastTween.to(-5f, 5f, 1000).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                eyeOffsetX = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }),
            FastTween.to(5f, 0f, 600).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                eyeOffsetX = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }),

            // --- Part 2: Eye Y Offset (up to down) ---
            FastTween.to(eyeOffsetY, -3f, 600).ease(Ease.QUAD_OUT).onUpdate(v -> {
                eyeOffsetY = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }),
            FastTween.to(-3f, 3f, 1000).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                eyeOffsetY = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }),
            FastTween.to(3f, 0f, 600).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                eyeOffsetY = v;
                textureDrawer.setEyeOffset((int) eyeOffsetX, (int) eyeOffsetY);
            }),

            // --- Part 3: Eye Scale (larger to smaller) ---
            FastTween.to(eyeScale, 1.6f, 800).ease(Ease.QUAD_OUT).onUpdate(v -> {
                eyeScale = v;
                textureDrawer.setEyeScale(eyeScale);
            }),
            FastTween.to(1.6f, 0.4f, 1000).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                eyeScale = v;
                textureDrawer.setEyeScale(eyeScale);
            }),
            FastTween.to(0.4f, 1.0f, 800).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                eyeScale = v;
                textureDrawer.setEyeScale(eyeScale);
            }),

            // --- Part 4: Mouth X Offset (left to right) ---
            FastTween.to(mouthOffsetX, -6f, 600).ease(Ease.QUAD_OUT).onUpdate(v -> {
                mouthOffsetX = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }),
            FastTween.to(-6f, 6f, 1000).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                mouthOffsetX = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }),
            FastTween.to(6f, 0f, 600).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                mouthOffsetX = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }),

            // --- Part 5: Mouth Y Offset (up to down) ---
            FastTween.to(mouthOffsetY, -4f, 600).ease(Ease.QUAD_OUT).onUpdate(v -> {
                mouthOffsetY = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }),
            FastTween.to(-4f, 4f, 1000).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                mouthOffsetY = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            }),
            FastTween.to(4f, 0f, 600).ease(Ease.QUAD_IN_OUT).onUpdate(v -> {
                mouthOffsetY = v;
                textureDrawer.setMouthOffset((int) mouthOffsetX, (int) mouthOffsetY);
            })
        );
        currentGestureAnim.start();
    }

    public String getLastGestureName() {
        return lastGestureName;
    }

    public synchronized void performGesture(String name) {
        this.lastGestureName = name.toUpperCase();
        switch (name.toLowerCase()) {
            case "test_displacement":
                triggerTestDisplacement();
                break;
            case "nod":
                triggerNod();
                break;
            case "shake":
                triggerShake();
                break;
            case "look_up":
                triggerLookUp();
                break;
            case "wink":
                triggerWink();
                break;
            case "look_down":
                triggerLookDown();
                break;
            case "look_left":
                triggerLookLeft();
                break;
            case "look_right":
                triggerLookRight();
                break;
            case "tilt_left":
                triggerTiltLeft();
                break;
            case "tilt_right":
                triggerTiltRight();
                break;
            case "surprise":
                triggerSurprise();
                break;
            case "laugh":
                triggerLaugh();
                break;
            case "sleep":
                triggerSleep();
                break;
            case "key_a":
                lastGestureName = "A : SUBTLE_NOD";
                executeGestureFull(0.1f, 0, 0, 0, 0, 0, 0, 150, 0, 150, 200);
                break;
            case "key_b":
                lastGestureName = "B : DEEP_NOD";
                executeGestureFull(0.3f, 0, 0, 0, 0, 0, 0, 300, 100, 400, 200);
                break;
            case "key_c":
                lastGestureName = "C : QUICK_SHAKE_LEFT";
                executeGestureFull(0, -0.1f, 0, 0, 0, 0, 0, 200, 0, 200, 200);
                break;
            case "key_d":
                lastGestureName = "D : QUICK_SHAKE_RIGHT";
                executeGestureFull(0, 0.1f, 0, 0, 0, 0, 0, 200, 0, 200, 200);
                break;
            case "key_e":
                lastGestureName = "E : LOOK_AWAY_LEFT";
                executeGestureFull(0, -0.4f, 0, -4, 0, -2, 0, 400, 1000, 500, 200); // Eye & Mouth shift left!
                break;
            case "key_f":
                lastGestureName = "F : LOOK_AWAY_RIGHT";
                executeGestureFull(0, 0.4f, 0, 4, 0, 2, 0, 400, 1000, 500, 200); // Eye & Mouth shift right!
                break;
            case "key_g":
                lastGestureName = "G : THINKING_LEFT";
                executeGestureFull(-0.1f, -0.2f, 0, -3, -3, 0, -2, 400, 600, 400, 200); // Eyes up-left, mouth slight up
                break;
            case "key_h":
                lastGestureName = "H : THINKING_RIGHT";
                executeGestureFull(-0.1f, 0.2f, 0, 3, -3, 0, -2, 400, 600, 400, 200);
                break;
            case "key_i":
                lastGestureName = "I : SAD_DOWN_LEFT";
                executeGestureFull(0.2f, -0.2f, 0, -2, 3, 0, 2, 400, 600, 400, 200);
                break;
            case "key_j":
                lastGestureName = "J : SAD_DOWN_RIGHT";
                executeGestureFull(0.2f, 0.2f, 0, 2, 3, 0, 2, 400, 600, 400, 200);
                break;
            case "key_k":
                lastGestureName = "K : DEEP_TILT_LEFT";
                executeGestureFull(0, 0, -0.3f, 0, 0, 0, 0, 500, 800, 500, 200);
                break;
            case "key_l":
                lastGestureName = "L : DEEP_TILT_RIGHT";
                executeGestureFull(0, 0, 0.3f, 0, 0, 0, 0, 500, 800, 500, 200);
                break;
            case "key_m":
                lastGestureName = "M : STARE_CEILING";
                executeGestureFull(-0.2f, 0, 0, 0, -5, 0, 0, 800, 2000, 1000, 200);
                break;
            case "key_n":
                lastGestureName = "N : STARE_FLOOR";
                executeGestureFull(0.2f, 0, 0, 0, 5, 0, 0, 800, 2000, 1000, 200);
                break;
            case "key_o":
                lastGestureName = "O : DISGUST_LEFT";
                executeGestureFull(-0.1f, -0.4f, -0.1f, -4, 0, -4, 2, 200, 500, 300, 200);
                break;
            case "key_p":
                lastGestureName = "P : DISGUST_RIGHT";
                executeGestureFull(-0.1f, 0.4f, 0.1f, 4, 0, 4, 2, 200, 500, 300, 200);
                break;
            case "key_q":
                lastGestureName = "Q : FLINCH";
                executeGestureFull(-0.25f, 0, 0, 0, 0, 0, -2, 100, 100, 200, 200);
                break;
            case "key_r":
                lastGestureName = "R : SLOW_POWER_DOWN";
                executeGestureFull(0.3f, 0, 0, 0, 5, 0, 3, 2000, 2000, 2000, 200);
                break;
            case "key_s":
                lastGestureName = "S : PROUD_LOOK";
                executeGestureFull(-0.2f, 0, 0, 0, -2, 0, -2, 2000, 2000, 2000, 200);
                break;
            case "key_t":
                lastGestureName = "T : HUH_TILT_LEFT";
                executeGestureFull(0, 0, -0.15f, 0, 0, 0, 0, 200, 100, 200, 200);
                break;
            case "key_u":
                lastGestureName = "U : HUH_TILT_RIGHT";
                executeGestureFull(0, 0, 0.15f, 0, 0, 0, 0, 200, 100, 200, 200);
                break;
            case "key_v":
                lastGestureName = "V : SWAY_DOWN_LEFT";
                executeGestureFull(0.1f, 0, -0.1f, 0, 0, 0, 0, 600, 400, 600, 200);
                break;
            case "key_w":
                lastGestureName = "W : SWAY_DOWN_RIGHT";
                executeGestureFull(0.1f, 0, 0.1f, 0, 0, 0, 0, 600, 400, 600, 200);
                break;
            case "key_x":
                lastGestureName = "X : SWAY_UP_LEFT";
                executeGestureFull(-0.1f, 0, -0.1f, 0, 0, 0, 0, 600, 400, 600, 200);
                break;
            case "key_y":
                lastGestureName = "Y : SWAY_UP_RIGHT";
                executeGestureFull(-0.1f, 0, 0.1f, 0, 0, 0, 0, 600, 400, 600, 200);
                break;
            case "key_z":
                lastGestureName = "Z : COLLAPSE";
                executeGestureFull(0.3f, 0.3f, 0.3f, 2, 5, 0, 5, 1000, 1000, 1000, 200);
                break;

            case "key_0":
                lastGestureName = "0 : WINK";
                triggerWink();
                break;
            case "key_1":
                lastGestureName = "1 : NOD";
                triggerNod();
                break;
            case "key_2":
                lastGestureName = "2 : SHAKE";
                triggerShake();
                break;
            case "key_3":
                lastGestureName = "3 : LOOK_UP";
                triggerLookUp();
                break;
            case "key_4":
                lastGestureName = "4 : LAUGH";
                triggerLaugh();
                break;
            case "key_5":
                lastGestureName = "5 : SLEEP";
                triggerSleep();
                break;
            case "key_6":
                lastGestureName = "6 : EXTREME_LEFT";
                executeGesture(0, -0.6f, 0, 200, 50, 400);
                break;
            case "key_7":
                lastGestureName = "7 : EXTREME_RIGHT";
                executeGesture(0, 0.6f, 0, 200, 50, 400);
                break;
            case "key_8":
                lastGestureName = "8 : EYES_MOUTH_LEFT";
                executeGestureFull(0, 0, 0, -4, 0, -4, 0, 200, 100, 200, 100);
                break;
            case "key_9":
                lastGestureName = "9 : EYES_MOUTH_SAD";
                executeGestureFull(0.1f, 0, 0, 0, 2, 0, 2, 300, 200, 300, 100);
                break;
        }
    }

    public synchronized void setUserIsTyping(boolean typing) {
        this.userIsTyping = typing;
    }

    public synchronized void update(long deltaMs) {
        if (headNode == null) return;

        long now = System.currentTimeMillis();

        updateBlinking(now);
        updateGestures(now);
        updateGaze(now);

        // NOTE: No manual tween.update() calls needed — FastAnimation drives everything
    }

    private void updateBlinking(long now) {
        if (true) return; // Temporarily disabled for testing
        if (blinkPhase == 0) {
            if (now - lastBlinkTime > blinkInterval) {
                double rand = Math.random();
                if (rand < 0.10) {
                    winkLeftOnly = true;
                } else if (rand < 0.20) {
                    winkRightOnly = true;
                } else {
                    winkLeftOnly = false;
                    winkRightOnly = false;
                }

                textureDrawer.setEyesClosed(true, winkLeftOnly || (!winkRightOnly && !winkLeftOnly), winkRightOnly || (!winkRightOnly && !winkLeftOnly));
                blinkPhase = 1;
                eyeCloseDuration = BLINK_DURATION_MIN + (long) (Math.random() * BLINK_DURATION_RANDOM);
                lastBlinkTime = now;
            }
        } else if (blinkPhase == 1) {
            if (now - lastBlinkTime > eyeCloseDuration) {
                textureDrawer.setEyesClosed(false, true, true);
                double rand = Math.random();
                if (rand < (BLINK_DOUBLE_CHANCE / 100.0) && !winkLeftOnly && !winkRightOnly) {
                    blinkPhase = 3;
                    lastBlinkTime = now;
                } else {
                    blinkPhase = 0;
                    winkLeftOnly = false;
                    winkRightOnly = false;
                    blinkInterval = BLINK_BASE_INTERVAL + (long) (Math.random() * (BLINK_RANDOM_ADD + 1000));
                    lastBlinkTime = now;
                }
            }
        } else if (blinkPhase == 3) {
            if (now - lastBlinkTime > BLINK_DOUBLE_GAP) {
                textureDrawer.setEyesClosed(true, true, true);
                blinkPhase = 4;
                eyeCloseDuration = BLINK_DOUBLE_GAP + (long) (Math.random() * 40);
                lastBlinkTime = now;
            }
        } else if (blinkPhase == 4) {
            if (now - lastBlinkTime > eyeCloseDuration) {
                textureDrawer.setEyesClosed(false, true, true);
                blinkPhase = 0;
                blinkInterval = (BLINK_BASE_INTERVAL + 1000) + (long) (Math.random() * (BLINK_RANDOM_ADD + 1000));
                lastBlinkTime = now;
            }
        }
    }

    private void updateGestures(long now) {
        if (gestureInProgress) {
            if (now > gestureEndTime) {
                gestureInProgress = false;
            }
        }
    }

    private void updateGaze(long now) {
        if (true) return; // Temporarily disabled for testing
        if (!gestureInProgress) {
            if (userIsTyping != wasUserTyping) {
                wasUserTyping = userIsTyping;
                if (userIsTyping) {
                    float targetRotX = GAZE_DOWN_PITCH + (float) (Math.random() * 0.04f);

                    double directionRand = Math.random();
                    float targetRotY;
                    if (directionRand < 0.33) {
                        targetRotY = GAZE_LEFT_YAW - (float) (Math.random() * 0.06f);
                    } else if (directionRand < 0.66) {
                        targetRotY = GAZE_RIGHT_YAW + (float) (Math.random() * 0.06f);
                    } else {
                        targetRotY = (float) ((Math.random() - 0.5f) * 0.04f);
                    }

                    float targetRotZ = (float) ((Math.random() - 0.5f) * 0.04f);

                    if (currentGazeAnim != null) currentGazeAnim.stop();
                    currentGazeAnim = FastAnimation.parallel(
                            FastAnimation.sequence(
                                    FastTween
                                            .to(headNode.getTransform().roll, targetRotX, USER_TYPING_FOCUS_DURATION)
                                            .ease(Ease.QUAD_OUT)
                                            .onUpdate(v -> headNode.getTransform().roll = v)),
                            FastAnimation.sequence(
                                    FastTween
                                            .to(headNode.getTransform().yaw, targetRotY, USER_TYPING_FOCUS_DURATION)
                                            .ease(Ease.QUAD_OUT)
                                            .onUpdate(v -> headNode.getTransform().yaw = v)),
                            FastAnimation.sequence(
                                    FastTween
                                            .to(headNode.getTransform().pitch, targetRotZ, USER_TYPING_FOCUS_DURATION)
                                            .ease(Ease.QUAD_OUT)
                                            .onUpdate(v -> headNode.getTransform().pitch = v))
                    );
                    currentGazeAnim.start();
                } else {
                    if (currentGazeAnim != null) currentGazeAnim.stop();
                    currentGazeAnim = FastAnimation.parallel(
                            FastAnimation.sequence(
                                    FastTween
                                            .to(headNode.getTransform().roll, 0.0f, IDLE_FOCUS_RELEASE_DURATION)
                                            .ease(Ease.QUAD_IN_OUT)
                                            .onUpdate(v -> headNode.getTransform().roll = v)),
                            FastAnimation.sequence(
                                    FastTween
                                            .to(headNode.getTransform().yaw, 0.0f, IDLE_FOCUS_RELEASE_DURATION)
                                            .ease(Ease.QUAD_IN_OUT)
                                            .onUpdate(v -> headNode.getTransform().yaw = v)),
                            FastAnimation.sequence(
                                    FastTween
                                            .to(headNode.getTransform().pitch, 0.0f, IDLE_FOCUS_RELEASE_DURATION)
                                            .ease(Ease.QUAD_IN_OUT)
                                            .onUpdate(v -> headNode.getTransform().pitch = v))
                    );
                    currentGazeAnim.start();
                    lastMoveTime = now;
                }
            }
        }

        if (!userIsTyping && !gestureInProgress) {
            if (now - lastMoveTime > 3000 + Math.random() * 8000) {
                float targetRotX = (float) ((Math.random() - 0.5) * (0.06f / 3.0f));
                float targetRotY = (float) ((Math.random() - 0.5) * 0.06f);
                float targetRotZ = (float) ((Math.random() - 0.5) * 0.04f);
                long duration = 1800 + (long) (Math.random() * 1000);

                if (currentGazeAnim != null) currentGazeAnim.stop();
                currentGazeAnim = FastAnimation.parallel(
                        FastAnimation.sequence(
                                FastTween
                                        .to(headNode.getTransform().roll, targetRotX, duration)
                                        .ease(Ease.QUAD_IN_OUT)
                                        .onUpdate(v -> headNode.getTransform().roll = v)),
                        FastAnimation.sequence
                                (FastTween
                                        .to(headNode.getTransform().yaw, targetRotY, duration)
                                        .ease(Ease.QUAD_IN_OUT)
                                        .onUpdate(v -> headNode.getTransform().yaw = v)),
                        FastAnimation.sequence(
                                FastTween
                                        .to(headNode.getTransform().pitch, targetRotZ, duration)
                                        .ease(Ease.QUAD_IN_OUT)
                                        .onUpdate(v -> headNode.getTransform().pitch = v))
                );
                currentGazeAnim.start();
                lastMoveTime = now;
            }
        }
    }
}
