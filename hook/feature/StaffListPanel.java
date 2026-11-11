/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiIcon;
import hook.feature.UiPalette;
import hook.feature.UiRenderer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class StaffListPanel {
    private final Map<String, AnimatedStaffMember> rows = new LinkedHashMap<String, AnimatedStaffMember>();
    private int count;
    private float visibility;
    private float height;
    private double time;

    public void setMembers(List<StaffMember> var1) {
        for (AnimatedStaffMember var3 : this.rows.values()) {
            var3.active = false;
        }
        int var7 = 0;
        for (StaffMember var4 : var1) {
            String var5 = var4.name().toLowerCase(Locale.ROOT);
            AnimatedStaffMember var6 = this.rows.get(var5);
            if (var6 == null) {
                var6 = new AnimatedStaffMember(var4, var7);
                this.rows.put(var5, var6);
            }
            var6.member = var4;
            var6.index = var7++;
            var6.active = true;
        }
        this.count = var7;
    }

    public void reset() {
        this.rows.clear();
        this.count = 0;
        this.height = 0.0f;
        this.visibility = 0.0f;
    }

    public void render(UiRenderer var1, float var2, float var3, float var4, float var5, float var6, float var7, boolean var8, boolean var9, boolean var10, int var11, DrawCallback var12) {
        var7 = Math.max(0.0f, Math.min(0.1f, var7));
        this.time += (double)var7;
        float var13 = 1.0f - (float)Math.exp(-12.0f * var7);
        float var14 = !var8 || this.count == 0 && !var9 ? 0.0f : 1.0f;
        this.visibility += (var14 - this.visibility) * var13;
        int var15 = Math.max(1, (int)(((float)var11 - var3) / var5 - 54.0f) / 30);
        if (this.count > var15) {
            var15 = Math.max(1, (int)(((float)var11 - var3) / var5 - 73.0f) / 30);
        }
        int var16 = Math.min(Math.max(1, this.count), var15);
        float var17 = (float)(44 + var16 * 30 + 10 + (this.count > var15 ? 19 : 0)) * var5;
        if (this.height == 0.0f) {
            this.height = var17;
        }
        this.height += (var17 - this.height) * var13;
        Iterator<AnimatedStaffMember> var18 = this.rows.values().iterator();
        while (var18.hasNext()) {
            AnimatedStaffMember var19 = var18.next();
            var19.alpha = var19.alpha + ((var19.active ? 1.0f : 0.0f) - var19.alpha) * var13;
            var19.y += ((float)var19.index * 30.0f - var19.y) * var13;
            if (var19.active || !(var19.alpha < 0.005f)) continue;
            var18.remove();
        }
        if (!(this.visibility < 0.005f)) {
            float var26 = 16.0f * var5;
            var1.pushAlpha(this.visibility);
            var1.shadow(var2, var3, var4, this.height, var26, 18.0f * var5, UiRenderer.argb(0.3f, 0, 0, 0));
            var1.round(var2, var3, var4, this.height, var26, UiPalette.glass(var6));
            var1.roundGradient(var2, var3, var4, this.height, var26, UiRenderer.white(0.05f), UiRenderer.white(0.0f));
            float var27 = Math.min(var4, this.height) * 0.38f;
            var1.glow(var2 + var4 * (0.5f + 0.08f * (float)Math.sin(this.time * 0.12)), var3 + this.height * 0.5f, var27, UiPalette.light(0.025f * var6));
            var1.outline(var2, var3, var4, this.height, var26, Math.max(1.0f, var5 * 0.8f), UiRenderer.white(0.085f));
            var1.rect(var2 + var26, var3 + Math.max(1.0f, var5), var4 - var26 * 2.0f, Math.max(1.0f, var5 * 0.7f), UiRenderer.white(0.045f));
            var1.icon(UiIcon.USER, var2 + 13.0f * var5, var3 + 13.0f * var5, 16.0f * var5, UiRenderer.white(0.85f));
            var1.text(var1.ellipsize("Staff List", var4 - 72.0f * var5, 13.0f * var5, 1), var2 + 38.0f * var5, var3 + 13.0f * var5, 13.0f * var5, 1, UiRenderer.white(0.95f));
            var1.textRight(Integer.toString(this.count), var2 + var4 - 14.0f * var5, var3 + 14.0f * var5, 11.0f * var5, 0, UiRenderer.white(0.45f));
            var1.rect(var2 + 12.0f * var5, var3 + 38.0f * var5, var4 - 24.0f * var5, Math.max(1.0f, var5 * 0.8f), UiRenderer.white(0.055f));
            if (this.count == 0 && this.rows.isEmpty()) {
                var1.text("No staff online", var2 + 14.0f * var5, var3 + 54.0f * var5, 11.5f * var5, 0, UiRenderer.white(0.45f));
            }
            var1.pushClip(var2 + 8.0f * var5, var3 + 44.0f * var5, var4 - 16.0f * var5, Math.max(0.0f, this.height - 51.0f * var5));
            for (AnimatedStaffMember var21 : this.rows.values()) {
                float var22 = var3 + 44.0f * var5 + var21.y * var5;
                if (var21.index >= var15 || var22 > var3 + this.height - 10.0f * var5 || var21.alpha < 0.005f) continue;
                var1.pushAlpha(var21.alpha);
                float var23 = (1.0f - var21.alpha) * 6.0f * var5;
                float var24 = var2 + 14.0f * var5 + var23;
                if (var10) {
                    var1.round(var24 - 2.0f * var5, var22 + 3.0f * var5, 23.0f * var5, 23.0f * var5, 7.0f * var5, UiRenderer.white(0.035f));
                    var12.draw(var21.member, var24, var22 + 5.0f * var5, 19.0f * var5, var21.alpha * this.visibility);
                    var24 += 29.0f * var5;
                }
                var1.text(var1.ellipsize(var21.member.name(), var2 + var4 - 14.0f * var5 - var24, 12.0f * var5, 0), var24, var22 + 8.0f * var5, 12.0f * var5, 0, UiRenderer.white(0.9f));
                var1.popAlpha();
            }
            var1.popClip();
            if (this.count > var15) {
                var1.text("+" + (this.count - var15) + " more", var2 + 14.0f * var5, var3 + this.height - 24.0f * var5, 10.0f * var5, 0, UiRenderer.white(0.4f));
            }
            var1.popAlpha();
        }
    }

    private static final class AnimatedStaffMember {
        StaffMember member;
        boolean active;
        float alpha;
        float y;
        int index;

        AnimatedStaffMember(StaffMember var1, int var2) {
            this.member = var1;
            this.index = var2;
            this.y = (float)var2 * 30.0f;
        }
    }

    public record StaffMember(String name, UUID uuid) {
    }

    @FunctionalInterface
    public static interface DrawCallback {
        public void draw(StaffMember var1, float var2, float var3, float var4, float var5);
    }
}

