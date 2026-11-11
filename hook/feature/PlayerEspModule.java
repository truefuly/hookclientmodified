/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import hook.utils.RenderVisibility;
import hook.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

public final class PlayerEspModule
extends Module {
    private final NumberSetting alpha;
    private final NumberSetting lineWidth;
    private final BooleanSetting tracers;
    private final List<RenderUtils.ColoredBox> renderBoxes = new ArrayList<RenderUtils.ColoredBox>();
    private final List<RenderUtils.ColoredLine> renderLines = new ArrayList<RenderUtils.ColoredLine>();

    public PlayerEspModule() {
        super(EncryptedString.of("Player ESP"), EncryptedString.of("Renders players through walls"), -1, Category.RENDER);
        this.alpha = new NumberSetting(EncryptedString.of("Alpha"), 0.0, 255.0, 100.0, 1.0);
        this.lineWidth = new NumberSetting(EncryptedString.of("Line width"), 1.0, 10.0, 1.0, 1.0);
        this.tracers = new BooleanSetting(EncryptedString.of("Tracers"), false).setDescription(EncryptedString.of("Draws a line from the camera to each player"));
        this.addSettings(this.alpha, this.lineWidth, this.tracers);
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) {
            return;
        }
        class_243 cameraPosition = camera.method_71156();
        float tickProgress = camera.method_55437();
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        this.renderBoxes.clear();
        this.renderLines.clear();
        class_243 tracerStart = this.getTracerStart(camera, cameraPosition);
        int playerIndex = 0;
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player == this.mc.field_1724 || player.method_31481() || !player.method_5805()) continue;
            class_243 position = player.method_30950(tickProgress);
            float halfWidth = player.method_17681() * 0.5f;
            double minX = position.field_1352 - (double)halfWidth;
            double minY = position.field_1351;
            double minZ = position.field_1350 - (double)halfWidth;
            double maxX = position.field_1352 + (double)halfWidth;
            double maxY = position.field_1351 + (double)player.method_17682();
            double maxZ = position.field_1350 + (double)halfWidth;
            if (!this.tracers.getValue() && !RenderVisibility.box(minX, minY, minZ, maxX, maxY, maxZ)) {
                ++playerIndex;
                continue;
            }
            Color color = Utils.getMainColor(this.alpha.getIntValue(), playerIndex++).brighter();
            this.renderBoxes.add(new RenderUtils.ColoredBox(minX, minY, minZ, maxX, maxY, maxZ, color));
            if (!this.tracers.getValue()) continue;
            class_243 target = position.method_1031(0.0, (double)player.method_17682() * 0.5, 0.0);
            Color tracerColor = Utils.getMainColor(255, playerIndex);
            this.renderLines.add(new RenderUtils.ColoredLine(tracerStart, target, tracerColor, this.lineWidth.getIntValue()));
        }
        RenderUtils.renderFilledBoxes(matrices, this.renderBoxes);
        if (this.tracers.getValue()) {
            RenderUtils.renderLines(matrices, this.renderLines);
        }
        matrices.method_22909();
    }

    private class_243 getTracerStart(class_4184 camera, class_243 cameraPosition) {
        return RenderUtils.getTracerStart(camera);
    }
}

