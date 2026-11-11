/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.Hook;
import hook.feature.ModuleRecency;
import hook.module.Category;
import hook.module.Module;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public final class ModuleCatalog {
    private static final Map<Object, CatalogSnapshot> CACHES = new WeakHashMap<Object, CatalogSnapshot>();

    private ModuleCatalog() {
    }

    private static List<Module> all() {
        try {
            return Hook.INSTANCE.getModuleManager().c();
        }
        catch (RuntimeException var1) {
            return List.of();
        }
    }

    public static List<Module> category(Object var0, Category var1) {
        List<Module> var2 = ModuleCatalog.all();
        CatalogSnapshot var3 = CACHES.computeIfAbsent(var0, var0x -> new CatalogSnapshot());
        var3.update(var2);
        return var3.categories.computeIfAbsent(var1, var1x -> {
            ArrayList<Module> var2x = new ArrayList<Module>();
            for (Object var4Raw : var2) {
                Module var4 = (Module)var4Raw;
                if (var4.getCategory() != var1x || PanicButtonModule.shouldHide(var4)) continue;
                var2x.add(var4);
            }
            return ModuleRecency.newFirst(var2x);
        });
    }

    public static List<Module> search(Object var0, String var1) {
        CatalogSnapshot var2 = CACHES.computeIfAbsent(var0, var0x -> new CatalogSnapshot());
        var2.update(ModuleCatalog.all());
        String var3 = var1.toLowerCase(Locale.ROOT).trim();
        if (var3.equals(var2.query)) {
            return var2.results;
        }
        ArrayList<Module> var4 = new ArrayList<Module>();
        ArrayList<Module> var5 = new ArrayList<Module>();
        for (int var6 = 0; var6 < var2.modules.length; ++var6) {
            String var7 = var2.names[var6];
            if (PanicButtonModule.shouldHide(var2.modules[var6])) continue;
            if (var7.contains(var3)) {
                (var7.startsWith(var3) ? var4 : var5).add(var2.modules[var6]);
                continue;
            }
            if (!var2.descriptions[var6].contains(var3)) continue;
            var5.add(var2.modules[var6]);
        }
        var4.addAll(var5);
        var2.query = var3;
        var2.results = ModuleRecency.newFirst(var4);
        return var2.results;
    }

    public static List<String> enabled(Module var0) {
        CatalogSnapshot var1 = CACHES.computeIfAbsent(var0, var0x -> new CatalogSnapshot());
        boolean var2 = var1.update(ModuleCatalog.all());
        for (int var3 = 0; var3 < var1.modules.length; ++var3) {
            boolean var4 = var1.modules[var3].isEnabled();
            if (var4 == var1.enabled[var3]) continue;
            var1.enabled[var3] = var4;
            var2 = true;
        }
        if (var2) {
            ArrayList<String> var6 = new ArrayList<String>();
            for (int var7 = 0; var7 < var1.modules.length; ++var7) {
                Module var5 = var1.modules[var7];
                if (!var1.enabled[var7] || var5 == var0 || var5 instanceof hook.module.modules.client.Hook || PanicButtonModule.shouldHide(var5)) continue;
                var6.add(var1.displayNames[var7]);
            }
            var1.activeNames = List.copyOf(var6);
        }
        return var1.activeNames;
    }

    public static void invalidate() {
        for (CatalogSnapshot snapshot : CACHES.values()) {
            snapshot.categories.clear();
            snapshot.query = null;
            snapshot.results = List.of();
        }
    }

    private static final class CatalogSnapshot {
        Module[] modules = new Module[0];
        Object[] nameSources = new Object[0];
        Object[] descriptionSources = new Object[0];
        Category[] categorySources = new Category[0];
        String[] names = new String[0];
        String[] displayNames = new String[0];
        String[] descriptions = new String[0];
        boolean[] enabled = new boolean[0];
        final EnumMap<Category, List<Module>> categories = new EnumMap(Category.class);
        String query;
        List<Module> results = List.of();
        List<String> activeNames = List.of();

        private CatalogSnapshot() {
        }

        boolean update(List<Module> var1) {
            boolean var2;
            boolean bl = var2 = var1.size() != this.modules.length;
            if (!var2) {
                for (int var3 = 0; var3 < var1.size(); ++var3) {
                    Module var4 = var1.get(var3);
                    if (var4 == this.modules[var3] && var4.getName() == this.nameSources[var3] && var4.getDescription() == this.descriptionSources[var3] && var4.getCategory() == this.categorySources[var3]) continue;
                    var2 = true;
                    break;
                }
            }
            if (!var2) {
                return false;
            }
            int var6 = var1.size();
            this.modules = (Module[])var1.toArray(Module[]::new);
            this.nameSources = new Object[var6];
            this.descriptionSources = new Object[var6];
            this.categorySources = new Category[var6];
            this.names = new String[var6];
            this.displayNames = new String[var6];
            this.descriptions = new String[var6];
            this.enabled = new boolean[var6];
            for (int var7 = 0; var7 < var6; ++var7) {
                Module var5 = this.modules[var7];
                this.nameSources[var7] = var5.getName();
                this.descriptionSources[var7] = var5.getDescription();
                this.categorySources[var7] = var5.getCategory();
                this.displayNames[var7] = this.nameSources[var7].toString();
                this.names[var7] = this.displayNames[var7].toLowerCase(Locale.ROOT);
                this.descriptions[var7] = this.descriptionSources[var7] == null ? "" : this.descriptionSources[var7].toString().toLowerCase(Locale.ROOT);
            }
            this.categories.clear();
            this.query = null;
            return true;
        }
    }
}

