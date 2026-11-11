/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package hook.halloween;

import hook.feature.HalloweenWelcomeAnimation;
import hook.feature.MinecraftUiRenderer;
import hook.feature.UiAudio;
import hook.halloween.Halloween;
import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class WelcomeScreen
extends class_437 {
    private static final float EXIT_TIME = 0.6f;
    private final HalloweenWelcomeAnimation title = new HalloweenWelcomeAnimation();
    private float t;
    private float exit = -1.0f;
    private long last;
    private boolean done;

    public WelcomeScreen() {
        super((class_2561)class_2561.method_43470((String)"Welcome To HookVisuals"));
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }

    public void method_25419() {
        this.proceed();
    }

    public void method_25420(class_332 var1, int var2, int var3, float var4) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 var1, int var2, int var3, float var4) {
        if (this.field_22787 != null) {
            long var5 = System.nanoTime();
            float var7 = this.last == 0L ? 0.016f : Math.min(0.1f, (float)(var5 - this.last) / 1.0E9f);
            this.last = var5;
            this.t += var7;
            if (this.exit >= 0.0f) {
                this.exit = Math.min(1.0f, this.exit + var7 / 0.6f);
            }
            class_1041 var8 = this.field_22787.method_22683();
            int var9 = var8.method_4489();
            int var10 = var8.method_4506();
            float var11 = var8.method_4495();
            double var12 = this.field_22787.field_1729.method_1603() * (double)var9 / (double)Math.max(1, var8.method_4480());
            double var14 = this.field_22787.field_1729.method_1604() * (double)var10 / (double)Math.max(1, var8.method_4507());
            var1.method_51448().pushMatrix();
            var1.method_51448().scale(1.0f / var11, 1.0f / var11);
            try {
                this.title.render(new MinecraftUiRenderer(var1, var11), var9, var10, this.t, var7, Math.max(0.0f, this.exit), var12, var14);
            }
            finally {
                var1.method_51448().popMatrix();
            }
            if (this.exit >= 1.0f && !this.done) {
                this.done = true;
                Halloween.titleFinished(this);
            }
        }
    }

    private double px(double var1) {
        return var1 * (this.field_22787 == null ? 1.0 : (double)this.field_22787.method_22683().method_4495());
    }

    public boolean method_25402(class_11909 var1, boolean var2) {
        if (this.title.overContinue(this.px(var1.comp_4798()), this.px(var1.comp_4799()))) {
            this.proceed();
        }
        return true;
    }

    public boolean method_25404(class_11908 var1) {
        int var2 = var1.comp_4795();
        if (var2 == 257 || var2 == 335 || var2 == 32 || var2 == 256) {
            this.proceed();
        }
        return true;
    }

    public void proceed() {
        if (this.exit < 0.0f && this.title.continueVisible()) {
            this.exit = 0.0f;
            this.title.pressed();
            UiAudio.play(UiAudio.Cue.CONFIRM);
        }
    }

    public boolean exiting() {
        return this.exit >= 0.0f;
    }
}

