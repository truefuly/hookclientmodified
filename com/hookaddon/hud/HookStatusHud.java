package com.hookaddon.hud;

import com.hookaddon.HookAddon;
import com.hookaddon.modules.AutoAreaMiner;
import com.hookaddon.modules.AutoSchematicBuilder;
import com.hookaddon.modules.AutoSpawner;
import hook.compat.meteorclient.systems.hud.HudElement;
import hook.compat.meteorclient.systems.hud.HudElementInfo;
import hook.compat.meteorclient.systems.hud.HudRenderer;
import hook.compat.meteorclient.systems.modules.Module;
import hook.compat.meteorclient.systems.modules.Modules;
import hook.compat.meteorclient.utils.render.color.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HookStatusHud extends HudElement {
   public static final HudElementInfo<HookStatusHud> INFO = new HudElementInfo<>(
      HookAddon.HUD_GROUP, "hook-status", "Shows the status of the Hook Addon modules.", HookStatusHud::new
   );
   private static final Color TITLE_COLOR = new Color(157, 89, 248);
   private static final Color INACTIVE_COLOR = new Color(120, 120, 130);

   public HookStatusHud() {
      super(INFO);
   }

   @Override
   public void render(HudRenderer renderer) {
      List<String[]> lines = this.collectLines();
      double width = 0.0;
      double lineHeight = renderer.textHeight(true);

      for (String[] line : lines) {
         width = Math.max(width, renderer.textWidth(line[0] + " " + line[1], true));
      }

      this.setSize(width, lines.size() * lineHeight);
      double rowY = this.y;

      for (String[] line : lines) {
         renderer.text(line[0], this.x, rowY, TITLE_COLOR, true);
         renderer.text(line[1], this.x + renderer.textWidth(line[0] + " ", true), rowY, INACTIVE_COLOR, true);
         rowY += lineHeight;
      }
   }

   private List<String[]> collectLines() {
      List<String[]> lines = new ArrayList<>();
      addLine(
         lines,
         Modules.get().getOptional(AutoSchematicBuilder.class).filter(Module::isActive).map(module -> new String[]{"Schematic:", module.getInfoString()})
      );
      addLine(lines, Modules.get().getOptional(AutoSpawner.class).filter(Module::isActive).map(module -> new String[]{"Spawner:", module.getInfoString()}));
      addLine(lines, Modules.get().getOptional(AutoAreaMiner.class).filter(Module::isActive).map(module -> new String[]{"Area:", module.getInfoString()}));
      return lines;
   }

   private static void addLine(List<String[]> lines, Optional<String[]> line) {
      line.ifPresent(lines::add);
   }
}
