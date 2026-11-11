/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_5321
 *  net.minecraft.class_6880
 *  net.minecraft.class_9304
 *  net.minecraft.class_9334
 */
package hook.utils;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Set;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import net.minecraft.class_9334;

public class EnchantmentUtil {
    public static boolean hasEnchantment(class_1799 itemStack, class_5321<?> registryKey) {
        if (itemStack.method_7960()) {
            return false;
        }
        Object2IntArrayMap enchantmentMap = new Object2IntArrayMap();
        EnchantmentUtil.populateEnchantmentMap(itemStack, (Object2IntMap)enchantmentMap);
        return EnchantmentUtil.containsEnchantment(enchantmentMap, registryKey);
    }

    private static boolean containsEnchantment(Object2IntMap<?> enchantmentMap, class_5321<?> registryKey) {
        for (Object enchantment : enchantmentMap.keySet()) {
            if (!((class_6880)enchantment).method_40225(registryKey)) continue;
            return true;
        }
        return false;
    }

    public static void populateEnchantmentMap(class_1799 itemStack, Object2IntMap enchantmentMap) {
        enchantmentMap.clear();
        if (!itemStack.method_7960()) {
            class_9304 stored;
            Set enchantments = itemStack.method_7909() == class_1802.field_8598 ? ((stored = (class_9304)itemStack.method_58694(class_9334.field_49643)) == null ? Set.of() : stored.method_57539()) : itemStack.method_58657().method_57539();
            for (Object enchantmentEntry : enchantments) {
                enchantmentMap.put(((Object2IntMap.Entry)enchantmentEntry).getKey(), ((Object2IntMap.Entry)enchantmentEntry).getIntValue());
            }
        }
    }
}

