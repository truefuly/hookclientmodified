/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiTextureData;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

final class UiImageCache {
    private static final Map<String, BufferedImage> SOURCES = new HashMap<String, BufferedImage>();
    private static final Map<String, UiTextureData> CACHE = new HashMap<String, UiTextureData>();

    private UiImageCache() {
    }

    static UiTextureData get(String var0, int var1) {
        int var2 = Math.max(4, Math.min(512, var1));
        return CACHE.computeIfAbsent(var0 + "@" + var2, var2x -> {
            BufferedImage var3 = UiImageCache.source(var0);
            if (var3 == null) {
                return null;
            }
            BufferedImage var4 = var3;
            while (var4.getWidth() / 2 >= var2) {
                var4 = UiImageCache.resize(var4, var4.getWidth() / 2, var4.getHeight() / 2);
            }
            BufferedImage var5 = UiImageCache.resize(var4, var2, var2);
            int[] var6 = new int[var2 * var2];
            var5.getRGB(0, 0, var2, var2, var6, 0, var2);
            if (!var0.startsWith("sky_")) {
                for (int var7 = 0; var7 < var6.length; ++var7) {
                    var6[var7] = var6[var7] & 0xFF000000 | 0xFFFFFF;
                }
            }
            return new UiTextureData("img_" + var2x, var2, var2, var6);
        });
    }

    private static final Map<String, UiTextureData> PREVIEWS = new HashMap<String, UiTextureData>();

    /** Full-colour preview image of a lobby theme (assets/hook/textures/gui/menu/<folder>/preview.png). */
    static UiTextureData lobbyPreview(String folder) {
        return PREVIEWS.computeIfAbsent(folder, key -> {
            try (InputStream in = UiImageCache.class.getResourceAsStream("/assets/hook/textures/gui/menu/" + key + "/preview.png")) {
                if (in == null) return null;
                BufferedImage image = ImageIO.read(in);
                int w = image.getWidth(), h = image.getHeight();
                int[] pixels = new int[w * h];
                image.getRGB(0, 0, w, h, pixels, 0, w);
                return new UiTextureData("lobby_" + key, w, h, pixels);
            } catch (Throwable t) {
                return null;
            }
        });
    }

    private static BufferedImage source(String var0) {
        return SOURCES.computeIfAbsent(var0, var0x -> {
            String var1 = switch (var0x) {
                case "logo" -> "/assets/hook/textures/gui/hook_mark.png";
                case "hk_mark" -> "/assets/hook/textures/gui/hk_mark.png";
                case "sky_borealis" -> "/assets/hook/textures/sky/previews/borealis.png";
                case "sky_nebula" -> "/assets/hook/textures/sky/previews/nebula.png";
                case "sky_hook" -> "/assets/hook/textures/sky/previews/hook.png";
                case "sky_frost" -> "/assets/hook/textures/sky/previews/frost.png";
                case "sky_crimson" -> "/assets/hook/textures/sky/previews/crimson.png";
                default -> null;
            };
            if (var1 == null) {
                return null;
            }
            try {
                BufferedImage var8;
                try (InputStream var2 = UiImageCache.class.getResourceAsStream(var1);){
                    var8 = var2 == null ? null : ImageIO.read(var2);
                }
                return var8;
            }
            catch (Throwable var7) {
                return null;
            }
        });
    }

    private static BufferedImage resize(BufferedImage var0, int var1, int var2) {
        BufferedImage var3 = new BufferedImage(var1, var2, 2);
        Graphics2D var4 = var3.createGraphics();
        var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        var4.drawImage(var0, 0, 0, var1, var2, null);
        var4.dispose();
        return var3;
    }
}

