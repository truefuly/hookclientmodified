/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package hook.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hook.Hook;
import hook.feature.HudModule;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class ConfigLayoutSupport {
    ConfigLayoutSupport() {
    }

    private static Object field(Object var0, String var1) throws ReflectiveOperationException {
        Field var2 = var0.getClass().getDeclaredField(var1);
        var2.setAccessible(true);
        return var2.get(var0);
    }

    private static void field(Object var0, String var1, Object var2) throws ReflectiveOperationException {
        Field var3 = var0.getClass().getDeclaredField(var1);
        var3.setAccessible(true);
        var3.set(var0, var2);
    }

    private static void save(Object var0, String var1) throws ReflectiveOperationException {
        Method var2 = var0.getClass().getDeclaredMethod(var1, new Class[0]);
        var2.setAccessible(true);
        var2.invoke(var0, new Object[0]);
    }

    static JsonObject capture() {
        JsonObject var0 = new JsonObject();
        try {
            Object var1 = ConfigLayoutSupport.field(Hook.INSTANCE.GUI, "ui");
            ConfigLayoutSupport.save(var1, "loadPosition");
            var0.addProperty("x", (Number)((Float)ConfigLayoutSupport.field(var1, "offX")));
            var0.addProperty("y", (Number)((Float)ConfigLayoutSupport.field(var1, "offY")));
            HudModule var2 = (HudModule)Hook.INSTANCE.getModuleManager().getModuleByClass(HudModule.class);
            Object var3 = ConfigLayoutSupport.field(var2, "hud");
            ConfigLayoutSupport.save(var3, "load");
            Map var4 = (Map)ConfigLayoutSupport.field(var3, "places");
            JsonObject var5 = new JsonObject();
            Iterator iterator = var4.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry var7Raw;
                Map.Entry var7 = var7Raw = (Map.Entry)iterator.next();
                Object var8 = var7.getValue();
                JsonObject var9 = new JsonObject();
                var9.addProperty("moved", (Boolean)ConfigLayoutSupport.field(var8, "moved"));
                var9.addProperty("right", (Boolean)ConfigLayoutSupport.field(var8, "right"));
                var9.addProperty("bottom", (Boolean)ConfigLayoutSupport.field(var8, "bottom"));
                var9.addProperty("x", (Number)((Float)ConfigLayoutSupport.field(var8, "ox")));
                var9.addProperty("y", (Number)((Float)ConfigLayoutSupport.field(var8, "oy")));
                var5.add(((Enum)var7.getKey()).name(), (JsonElement)var9);
            }
            var0.add("hud", (JsonElement)var5);
            return var0;
        }
        catch (ReflectiveOperationException var10) {
            throw new IllegalStateException("Couldn't read the HUD layout.", var10);
        }
    }

    static JsonObject defaults() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("x", (Number)0);
        var0.addProperty("y", (Number)0);
        var0.add("hud", (JsonElement)new JsonObject());
        return var0;
    }

    static void validate(JsonObject var0) {
        ConfigLayoutSupport.number(var0.get("x"));
        ConfigLayoutSupport.number(var0.get("y"));
        JsonObject var1 = var0.getAsJsonObject("hud");
        if (var1 != null && var1.size() <= 32) {
            for (Map.Entry var3 : var1.entrySet()) {
                JsonObject var4 = ((JsonElement)var3.getValue()).getAsJsonObject();
                ConfigLayoutSupport.number(var4.get("x"));
                ConfigLayoutSupport.number(var4.get("y"));
                for (String var6 : List.of("moved", "right", "bottom")) {
                    JsonElement var7 = var4.get(var6);
                    if (var7 != null && var7.isJsonPrimitive() && var7.getAsJsonPrimitive().isBoolean()) continue;
                    throw new IllegalArgumentException("Invalid HUD layout.");
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid HUD layout.");
        }
    }

    private static float number(JsonElement var0) {
        if (var0 != null && var0.isJsonPrimitive() && var0.getAsJsonPrimitive().isNumber()) {
            float var1 = var0.getAsFloat();
            if (Float.isFinite(var1) && !(Math.abs(var1) > 10000.0f)) {
                return var1;
            }
            throw new IllegalArgumentException("Invalid layout position.");
        }
        throw new IllegalArgumentException("Invalid layout position.");
    }

    static void apply(JsonObject var0) {
        ConfigLayoutSupport.validate(var0);
        try {
            Object var1 = ConfigLayoutSupport.field(Hook.INSTANCE.GUI, "ui");
            ConfigLayoutSupport.field(var1, "offX", Float.valueOf(ConfigLayoutSupport.number(var0.get("x"))));
            ConfigLayoutSupport.field(var1, "offY", Float.valueOf(ConfigLayoutSupport.number(var0.get("y"))));
            ConfigLayoutSupport.field(var1, "posLoaded", true);
            ConfigLayoutSupport.field(var1, "dragging", false);
            ConfigLayoutSupport.save(var1, "savePosition");
            HudModule var2 = (HudModule)Hook.INSTANCE.getModuleManager().getModuleByClass(HudModule.class);
            Object var3 = ConfigLayoutSupport.field(var2, "hud");
            Map var4 = (Map)ConfigLayoutSupport.field(var3, "places");
            var4.clear();
            ConfigLayoutSupport.field(var3, "dragging", null);
            Class<?> var5 = Class.forName("hook.feature.HudLayoutEditor$HudSection");
            Constructor<?> var6 = Class.forName("hook.feature.HudLayoutEditor$adsdzhecwnzmbs").getDeclaredConstructor(new Class[0]);
            var6.setAccessible(true);
            for (Map.Entry var8 : var0.getAsJsonObject("hud").entrySet()) {
                Object var9;
                try {
                    var9 = Enum.valueOf((Class)var5, (String)var8.getKey());
                }
                catch (IllegalArgumentException var12) {
                    continue;
                }
                JsonObject var10 = ((JsonElement)var8.getValue()).getAsJsonObject();
                Object var11 = var6.newInstance(new Object[0]);
                ConfigLayoutSupport.field(var11, "moved", var10.get("moved").getAsBoolean());
                ConfigLayoutSupport.field(var11, "right", var10.get("right").getAsBoolean());
                ConfigLayoutSupport.field(var11, "bottom", var10.get("bottom").getAsBoolean());
                ConfigLayoutSupport.field(var11, "ox", Float.valueOf(ConfigLayoutSupport.number(var10.get("x"))));
                ConfigLayoutSupport.field(var11, "oy", Float.valueOf(ConfigLayoutSupport.number(var10.get("y"))));
                var4.put(var9, var11);
            }
            ConfigLayoutSupport.field(var3, "loaded", true);
            ConfigLayoutSupport.save(var3, "save");
        }
        catch (ReflectiveOperationException var13) {
            throw new IllegalStateException("Couldn't restore the HUD layout.", var13);
        }
    }
}

