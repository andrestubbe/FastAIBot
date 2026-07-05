package fastbot.scene;

import fastsoftware3d.material.Material;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class BotHeadTextureDrawer {

    // Magic Numbers & Constants
    private static final int MOUTH_COLOR = 0xf5bb6c;
    private static final int LASH_COLOR = 0x3d281a;
    
    private static final int MOUTH_START_X = 178;
    private static final int MOUTH_END_X = 205;
    private static final int MOUTH_BASE_START_Y = 71;
    private static final int MOUTH_BASE_END_Y = 73;
    private static final int MOUTH_MAX_START_Y = 69;
    private static final int MOUTH_MAX_END_Y = 85;

    private static final int LEFT_EYE_START_X = 167;
    private static final int RIGHT_EYE_START_X = 209;
    private static final int EYE_START_Y = 55;
    private static final int EYE_WIDTH = 8;
    private static final int EYE_HEIGHT = 7;
    private static final int LASH_LINE_1_Y = 58;
    private static final int LASH_LINE_2_Y = 59;

    private final Material headMat;
    private final int[] cleanTextureTexels;
    private boolean isDirty = false;
    
    // Stored states for full redraw
    private int mouthStartY = MOUTH_BASE_START_Y;
    private int mouthEndY = MOUTH_BASE_END_Y;
    private boolean eyesClosed = false;
    private boolean leftEyeClosed = false;
    private boolean rightEyeClosed = false;
    
    private int eyeOffsetX = 0;
    private int eyeOffsetY = 0;
    private int mouthOffsetX = 0;
    private int mouthOffsetY = 0;
    private float eyeScale = 1.0f; // 1.0f is default

    public BotHeadTextureDrawer(Material headMat) {
        this.headMat = headMat;
        // Keep a clean backup of the original unedited texture pixels
        this.cleanTextureTexels = new int[headMat.texels.length];
        System.arraycopy(headMat.texels, 0, this.cleanTextureTexels, 0, headMat.texels.length);
        
        redrawFace();
    }

    public synchronized void setMouthOpenness(float openness) {
        if (headMat == null) return;

        // Calculate dynamic height based on openness
        int startY = MOUTH_BASE_START_Y - (int)(openness * 2.0f);
        int endY = MOUTH_BASE_END_Y + (int)(openness * 12.0f);
        if (startY < MOUTH_MAX_START_Y) startY = MOUTH_MAX_START_Y;
        if (endY > MOUTH_MAX_END_Y) endY = MOUTH_MAX_END_Y;

        if (startY == this.mouthStartY && endY == this.mouthEndY) {
            return; // No change, avoid mipmap regen
        }

        this.mouthStartY = startY;
        this.mouthEndY = endY;
        redrawFace();
    }

    public void setEyesClosed(boolean closed, boolean left, boolean right) {
        if (headMat == null) return;
        this.eyesClosed = closed;
        this.leftEyeClosed = closed && left;
        this.rightEyeClosed = closed && right;
        redrawFace();
    }
    
    public void setEyeOffset(int x, int y) {
        this.eyeOffsetX = x;
        this.eyeOffsetY = y;
        redrawFace();
    }

    public void setEyeScale(float scale) {
        this.eyeScale = scale;
        redrawFace();
    }
    
    public void setMouthOffset(int x, int y) {
        this.mouthOffsetX = x;
        this.mouthOffsetY = y;
        redrawFace();
    }

    private synchronized void redrawFace() {
        if (headMat == null) return;
        int w = headMat.texWidth;
        
        // 1. Full face reset from pristine backup
        System.arraycopy(this.cleanTextureTexels, 0, headMat.texels, 0, headMat.texels.length);
        
        // 2. Draw Mouth
        for (int texY = mouthStartY; texY <= mouthEndY; texY++) {
            for (int texX = MOUTH_START_X; texX <= MOUTH_END_X; texX++) {
                // Round the corners: skip extreme corner pixels
                if (texY == mouthStartY || texY == mouthEndY) {
                    if (texX == MOUTH_START_X || texX == MOUTH_START_X + 1 || texX == MOUTH_END_X - 1 || texX == MOUTH_END_X) {
                        continue;
                    }
                }
                
                int finalX = texX + mouthOffsetX;
                int finalY = texY + mouthOffsetY;
                
                if (finalX >= 0 && finalX < w && finalY >= 0 && finalY < headMat.texHeight) {
                    headMat.texels[finalY * w + finalX] = MOUTH_COLOR;
                }
            }
        }
        
        // 3. Draw Eyes
        if (eyesClosed) {
            // Draw lash line on top of restored skin
            if (leftEyeClosed) {
                for (int x = 0; x < EYE_WIDTH; x++) {
                    int finalX = LEFT_EYE_START_X + eyeOffsetX + x;
                    int finalY1 = LASH_LINE_1_Y + eyeOffsetY;
                    int finalY2 = LASH_LINE_2_Y + eyeOffsetY;
                    if (finalX >= 0 && finalX < w) {
                        if (finalY1 >= 0 && finalY1 < headMat.texHeight) headMat.texels[finalY1 * w + finalX] = LASH_COLOR;
                        if (finalY2 >= 0 && finalY2 < headMat.texHeight) headMat.texels[finalY2 * w + finalX] = LASH_COLOR;
                    }
                }
            }
            if (rightEyeClosed) {
                for (int x = 0; x < EYE_WIDTH; x++) {
                    int finalX = RIGHT_EYE_START_X + eyeOffsetX + x;
                    int finalY1 = LASH_LINE_1_Y + eyeOffsetY;
                    int finalY2 = LASH_LINE_2_Y + eyeOffsetY;
                    if (finalX >= 0 && finalX < w) {
                        if (finalY1 >= 0 && finalY1 < headMat.texHeight) headMat.texels[finalY1 * w + finalX] = LASH_COLOR;
                        if (finalY2 >= 0 && finalY2 < headMat.texHeight) headMat.texels[finalY2 * w + finalX] = LASH_COLOR;
                    }
                }
            }
        }
        
        // Draw open eyes if they aren't explicitly closed
        if (!leftEyeClosed)  drawOpenEye(LEFT_EYE_START_X + eyeOffsetX, EYE_START_Y + eyeOffsetY);
        if (!rightEyeClosed) drawOpenEye(RIGHT_EYE_START_X + eyeOffsetX, EYE_START_Y + eyeOffsetY);

        isDirty = true;
    }

    /**
     * Draws a single filled anti-aliased oval into the texture at the given coordinates.
     */
    private void drawOpenEye(int startX, int startY) {
        if (headMat == null) return;

        int w = headMat.texWidth;

        // Calculate dynamic dimensions based on scale
        int currentWidth = Math.max(1, Math.round(EYE_WIDTH * eyeScale));
        int currentHeight = Math.max(1, Math.round(EYE_HEIGHT * eyeScale));

        // Offset start positions to scale from the center of the eye
        int offsetX = (EYE_WIDTH - currentWidth) / 2;
        int offsetY = (EYE_HEIGHT - currentHeight) / 2;
        int finalStartX = startX + offsetX;
        int finalStartY = startY + offsetY;

        BufferedImage img = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < currentHeight; y++) {
            for (int x = 0; x < currentWidth; x++) {
                int finalX = finalStartX + x;
                int finalY = finalStartY + y;
                if (finalX >= 0 && finalX < w && finalY >= 0 && finalY < headMat.texHeight) {
                    int rgb = headMat.texels[finalY * w + finalX];
                    img.setRGB(x, y, 0xFF000000 | rgb);
                }
            }
        }

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(new Color(MOUTH_COLOR));
        g.fill(new Ellipse2D.Float(0f, 0f, (float)(currentWidth - 1), (float)(currentHeight - 1)));
        g.dispose();

        for (int y = 0; y < currentHeight; y++) {
            for (int x = 0; x < currentWidth; x++) {
                int finalX = finalStartX + x;
                int finalY = finalStartY + y;
                if (finalX >= 0 && finalX < w && finalY >= 0 && finalY < headMat.texHeight) {
                    int argb = img.getRGB(x, y);
                    int rgb = argb & 0xFFFFFF;
                    headMat.texels[finalY * w + finalX] = rgb;
                }
            }
        }
    }

    public void commit() {
        if (isDirty) {
            regenerateMipmaps();
            isDirty = false;
        }
    }

    private void regenerateMipmaps() {
        // 3. Fast manual base mipmap generation level 0 reload
        System.arraycopy(headMat.texels, 0, headMat.mipmapData, 0, headMat.texels.length);
        
        // Downscale levels for rendering pipeline mipmaps
        int width = headMat.texWidth;
        int height = headMat.texHeight;
        for (int level = 1; level < headMat.mipmapLevels; level++) {
            int srcWidth = Math.max(1, width >> (level - 1));
            int srcHeight = Math.max(1, height >> (level - 1));
            int dstWidth = Math.max(1, width >> level);
            int dstHeight = Math.max(1, height >> level);

            int srcOffset = headMat.mipmapOffsets[level - 1];
            int dstOffset = headMat.mipmapOffsets[level];

            for (int y = 0; y < dstHeight; y++) {
                for (int x = 0; x < dstWidth; x++) {
                    int sx = x * 2;
                    int sy = y * 2;
                    int sx1 = Math.min(sx + 1, srcWidth - 1);
                    int sy1 = Math.min(sy + 1, srcHeight - 1);

                    int c00 = headMat.mipmapData[srcOffset + sy * srcWidth + sx];
                    int c10 = headMat.mipmapData[srcOffset + sy * srcWidth + sx1];
                    int c01 = headMat.mipmapData[srcOffset + sy1 * srcWidth + sx];
                    int c11 = headMat.mipmapData[srcOffset + sy1 * srcWidth + sx1];

                    int r = (((c00 >> 16) & 0xFF) + ((c10 >> 16) & 0xFF) + ((c01 >> 16) & 0xFF) + ((c11 >> 16) & 0xFF)) >> 2;
                    int g = (((c00 >> 8) & 0xFF) + ((c10 >> 8) & 0xFF) + ((c01 >> 8) & 0xFF) + ((c11 >> 8) & 0xFF)) >> 2;
                    int b = ((c00 & 0xFF) + (c10 & 0xFF) + (c01 & 0xFF) + (c11 & 0xFF)) >> 2;

                    headMat.mipmapData[dstOffset + y * dstWidth + x] = (r << 16) | (g << 8) | b;
                }
            }
        }
    }
}
