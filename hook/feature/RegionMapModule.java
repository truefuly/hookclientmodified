/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render2DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import hook.utils.TextRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;

public final class RegionMapModule
extends Module {
    private static final int MAP_SIZE = 9;
    private static final double CELL_WORLD_SIZE = 50000.0;
    private static final double MAP_OFFSET = 225000.0;
    private static final int[] REGION_IDS = new int[]{82, 100, 101, 102, 103, 104, 105, 106, 91, 83, 44, 75, 42, 41, 40, 39, 38, 92, 84, 45, 14, 13, 12, 11, 10, 37, 93, 85, 46, 74, 3, 2, 1, 25, 36, 94, 86, 47, 72, 71, 5, 4, 24, 35, 95, 87, 51, 17, 9, 8, 7, 23, 34, 96, 88, 54, 18, 61, 62, 21, 22, 33, 97, 89, 26, 27, 28, 29, 30, 59, 32, 98, 90, 107, 108, 109, 110, 111, 112, 113, 99};
    private static final int[] REGION_TYPES = new int[]{5, 3, 3, 3, 2, 2, 2, 2, 2, 5, 3, 3, 3, 2, 2, 2, 2, 2, 5, 3, 3, 3, 2, 2, 2, 2, 2, 5, 5, 5, 3, 2, 2, 2, 2, 2, 4, 4, 4, 4, 2, 2, 2, 2, 2, 4, 1, 1, 0, 0, 0, 0, 0, 2, 4, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0};
    private static final Color[] REGION_COLORS = new Color[]{new Color(159, 206, 99), new Color(0, 166, 99), new Color(79, 173, 234), new Color(47, 110, 186), new Color(245, 194, 66), new Color(252, 136, 3)};
    private static final String[] REGION_NAMES = new String[]{"EU Central", "EU West", "NA East", "NA West", "Asia", "Oceania"};
    private final NumberSetting mapX = new NumberSetting(EncryptedString.of("Position X"), 0.0, 4000.0, 15.0, 1.0);
    private final NumberSetting mapY = new NumberSetting(EncryptedString.of("Position Y"), 0.0, 4000.0, 70.0, 1.0);
    private final NumberSetting cellSize = new NumberSetting(EncryptedString.of("Cell Size"), 14.0, 56.0, 26.0, 1.0);
    private final NumberSetting transparency = new NumberSetting(EncryptedString.of("Transparency"), 0.1, 1.0, 0.78, 0.05);
    private final BooleanSetting purple = new BooleanSetting(EncryptedString.of("Purple"), false).setDescription(EncryptedString.of("Uses the supplied RTP-map spot-zone style"));
    private final BooleanSetting coordinates = new BooleanSetting(EncryptedString.of("Coordinates"), true);
    private final BooleanSetting legend = new BooleanSetting(EncryptedString.of("Legend"), true);
    private final BooleanSetting grid = new BooleanSetting(EncryptedString.of("Grid"), true);
    private final BooleanSetting playerIndicator = new BooleanSetting(EncryptedString.of("Player Indicator"), true);
    private final List<MapCell> spots = RegionMapModule.createSpots();

    public RegionMapModule() {
        super(EncryptedString.of("Region Map"), EncryptedString.of("Shows the DonutSMP 50k region grid and your position"), -1, Category.DONUT);
        this.addSettings(this.mapX, this.mapY, this.cellSize, this.transparency, this.purple, this.coordinates, this.legend, this.grid, this.playerIndicator);
    }

    @EventListener
    public void onRender2D(Render2DEvent event) {
        int row;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_332 context = event.context;
        RenderUtils.unscaledProjection();
        int x = this.mapX.getIntValue();
        int y = this.mapY.getIntValue();
        int size = this.cellSize.getIntValue();
        int width = size * 9;
        int alpha = (int)(this.transparency.getValue() * 255.0);
        context.method_25294(x - 2, y - 2, x + width + 2, y + width + 2, new Color(8, 10, 16, Math.max(30, alpha)).getRGB());
        boolean purpleStyle = this.purple.getValue();
        for (row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = row * 9 + col;
                Color color = purpleStyle ? new Color(18, 19, 28, Math.max(45, alpha)) : RegionMapModule.withAlpha(REGION_COLORS[REGION_TYPES[index]], alpha);
                context.method_25294(x + col * size + 1, y + row * size + 1, x + (col + 1) * size, y + (row + 1) * size, color.getRGB());
            }
        }
        if (purpleStyle) {
            for (MapCell spot : this.spots) {
                Color color = switch (spot.type) {
                    case 2 -> new Color(64, 140, 255, 215);
                    case 3 -> new Color(186, 110, 255, 205);
                    default -> new Color(176, 188, 208, 195);
                };
                context.method_25294(x + spot.col * size + 2, y + spot.row * size + 2, x + (spot.col + 1) * size - 1, y + (spot.row + 1) * size - 1, color.getRGB());
            }
        }
        if (this.grid.getValue()) {
            int gridColor = new Color(25, 28, 38, 230).getRGB();
            for (int index = 0; index <= 9; ++index) {
                context.method_25294(x + index * size, y, x + index * size + 1, y + width, gridColor);
                context.method_25294(x, y + index * size, x + width, y + index * size + 1, gridColor);
            }
        }
        for (row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                String regionId = Integer.toString(REGION_IDS[row * 9 + col]);
                int textWidth = TextRenderer.getWidth(regionId);
                int textX = x + col * size + Math.max(2, (size - textWidth) / 2);
                int textY = y + row * size + Math.max(2, (size - 10) / 2);
                TextRenderer.drawString(regionId, context, textX, textY, new Color(238, 241, 250, 235).getRGB());
            }
        }
        if (this.playerIndicator.getValue()) {
            double mapCol = (this.mc.field_1724.method_23317() + 225000.0) / 50000.0;
            double mapRow = (this.mc.field_1724.method_23321() + 225000.0) / 50000.0;
            if (mapCol >= 0.0 && mapCol < 9.0 && mapRow >= 0.0 && mapRow < 9.0) {
                int playerX = x + (int)Math.round(mapCol * (double)size);
                int playerY = y + (int)Math.round(mapRow * (double)size);
                context.method_25294(playerX - 3, playerY - 3, playerX + 4, playerY + 4, new Color(255, 55, 70, 255).getRGB());
                context.method_25294(playerX - 1, playerY - 6, playerX + 2, playerY - 3, new Color(255, 210, 215, 255).getRGB());
            }
        }
        int textY = y + width + 7;
        if (this.coordinates.getValue()) {
            String position = "Position: X: " + (int)this.mc.field_1724.method_23317() + ", Z: " + (int)this.mc.field_1724.method_23321();
            TextRenderer.drawString(position, context, x, textY, Color.WHITE.getRGB());
            textY += 16;
        }
        if (this.legend.getValue()) {
            if (purpleStyle) {
                textY = this.legendLine(context, x, textY, new Color(186, 110, 255), "Glitched / no RTP spots");
                textY = this.legendLine(context, x, textY, new Color(64, 140, 255), "Media / baltop");
                this.legendLine(context, x, textY, new Color(176, 188, 208), "Other base spots");
            } else {
                for (int index = 0; index < REGION_NAMES.length; ++index) {
                    textY = this.legendLine(context, x, textY, REGION_COLORS[index], REGION_NAMES[index]);
                }
            }
        }
        RenderUtils.scaledProjection();
    }

    private int legendLine(class_332 context, int x, int y, Color color, String label) {
        int var19 = x + 12;
        int var23 = y + 12;
        int var27 = color.getRGB();
        context.method_25294(x, y, var19, var23, var27);
        int var18 = x + 17;
        int var25 = Color.WHITE.getRGB();
        TextRenderer.drawString(label, context, var18, y, var25);
        return y + 16;
    }

    private static Color withAlpha(Color color, int alpha) {
        int var9 = color.getRed();
        int var10 = color.getGreen();
        int var11 = color.getBlue();
        int var13 = Math.min(255, alpha);
        int var12 = Math.max(0, var13);
        return new Color(var9, var10, var11, var12);
    }

    private static List<MapCell> createSpots() {
        ArrayList<MapCell> spots = new ArrayList<MapCell>();
        RegionMapModule.addSpot(spots, 3, -200000.0, 200000.0);
        RegionMapModule.addSpot(spots, 3, -200000.0, 150000.0);
        RegionMapModule.addSpot(spots, 3, -100000.0, -200000.0);
        RegionMapModule.addSpot(spots, 3, 0.0, -200000.0);
        RegionMapModule.addSpot(spots, 3, 50000.0, -200000.0);
        RegionMapModule.addSpot(spots, 3, 200000.0, 100000.0);
        RegionMapModule.addSpot(spots, 2, 0.0, 0.0);
        RegionMapModule.addSpot(spots, 2, 50000.0, 0.0);
        RegionMapModule.addSpot(spots, 2, 0.0, 50000.0);
        RegionMapModule.addSpot(spots, 2, 50000.0, 150000.0);
        int[][] var1 = new int[][]{{-200000, 0}, {-200000, 100000}, {0, -50000}, {50000, 50000}, {150000, 50000}, {-50000, 150000}, {0, 150000}, {100000, 150000}, {150000, 150000}, {200000, 150000}, {-50000, 200000}, {100000, 200000}, {150000, 200000}, {200000, 200000}};
        for (int[] point : var1) {
            RegionMapModule.addSpot(spots, 1, point[0], point[1]);
        }
        return spots;
    }

    private static void addSpot(List<MapCell> spots, int type, double worldX, double worldZ) {
        int col = (int)Math.floor((worldX + 225000.0) / 50000.0);
        int row = (int)Math.floor((worldZ + 225000.0) / 50000.0);
        if (col >= 0 && col < 9 && row >= 0 && row < 9) {
            spots.removeIf(existing -> existing.col == col && existing.row == row && existing.type <= type);
            if (!spots.stream().anyMatch(existing -> existing.col == col && existing.row == row && existing.type > type)) {
                spots.add(new MapCell(col, row, type));
            }
        }
    }

    private record MapCell(int col, int row, int type) {
    }
}

