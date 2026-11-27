package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ItemSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_3965;

/**
 * Places the chosen block from your offhand where you look and mines it again with the tool in your main hand,
 * over and over. Keeps the offhand stocked from your inventory and pauses while Anti Break protects your tool.
 */
public final class AutoMinerModule extends Module {
    /** Self-test: picks the block. */
    void testBlock(class_1792 item) {
        this.block.setItem(item);
    }

    private final ItemSetting block = new ItemSetting(EncryptedString.of("Block"), class_1802.field_20412).onlyBlocks();
    private final NumberSetting cps = new NumberSetting(EncryptedString.of("Speed (CPS)"), 1.0, 25.0, 25.0, 1.0).getValue(EncryptedString.of("Place + break cycles per second, at most 25"));
    private final BooleanSetting restock = new BooleanSetting(EncryptedString.of("Auto Restock"), true).setDescription(EncryptedString.of("Moves the block from your inventory into the offhand when it runs out"));
    private final BooleanSetting place = new BooleanSetting(EncryptedString.of("Auto Place"), true).setDescription(EncryptedString.of("Places the block from your offhand where you look"));
    private final BooleanSetting mine = new BooleanSetting(EncryptedString.of("Auto Break"), true).setDescription(EncryptedString.of("Breaks the block with the tool in your main hand"));

    /** Hard limit: never more than 25 place + break cycles per second. */
    private static final double MAX_CPS = 25.0;

    private double budget;
    private long lastNanos;
    private class_3965 support;
    private boolean warnedEmpty;
    private static AutoMinerModule instance;
    private static long breakingTick = Long.MIN_VALUE;
    private long tickCounter;
    /** Self-test: cycles done since enabled. */
    int cycles;

    /**
     * True while Auto Miner is mining a block this tick. Vanilla cancels block breaking every tick the attack key is
     * not held, which would reset the progress, so that check is skipped while this is set.
     */
    public static boolean ownsBreaking() {
        AutoMinerModule m = instance;
        return m != null && m.isEnabled() && breakingTick == m.tickCounter;
    }

    public AutoMinerModule() {
        super(EncryptedString.of("Auto Miner"), EncryptedString.of("Places your chosen block from the offhand and breaks it again, up to 25 times a second"), -1, Category.MISC);
        this.addSettings(this.block, this.cps, this.restock, this.place, this.mine);
        instance = this;
    }

    @Override
    public void onEnable() {
        this.budget = 0.0;
        this.lastNanos = 0L;
        this.support = null;
        this.cycles = 0;
        this.warnedEmpty = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.mc.field_1761 != null) this.mc.field_1761.method_2925();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        // keeps working with a GUI or the pause menu open and while tabbed out (only a singleplayer pause stops ticks)
        this.tickCounter++;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1761 == null) return;
        class_1792 wanted = this.block.getItem();
        if (!(wanted instanceof class_1747 blockItem)) return;

        if (this.restock.getValue()) this.restockOffhand(wanted);
        // Anti Break compatibility: never place or mine while the tool is protected
        if (AntiBreakModule.blocksMining()) {
            this.mc.field_1761.method_2925();
            return;
        }
        // budget by real time (the tick event can fire more than once per game tick): never above the set CPS
        long now = System.nanoTime();
        double cpsNow = Math.min(MAX_CPS, this.cps.getValue());
        if (this.lastNanos != 0L) this.budget = Math.min(this.budget + (now - this.lastNanos) / 1.0e9 * cpsNow, 3.0);
        this.lastNanos = now;
        if (!(this.mc.field_1765 instanceof class_3965 hit) || hit.method_17783() != class_239.class_240.field_1332) return;
        net.minecraft.class_2248 target = blockItem.method_7711();

        // P = the spot the block lives in, support = the face it gets placed against
        class_2338 spot;
        net.minecraft.class_2350 face;
        if (this.mc.field_1687.method_8320(hit.method_17777()).method_26204() == target) {
            spot = hit.method_17777();
            face = hit.method_17780();
            if (this.support != null && !this.support.method_17777().method_10093(this.support.method_17780()).equals(spot)) this.support = null;
        } else {
            this.support = hit;
            spot = hit.method_17777().method_10093(hit.method_17780());
            face = hit.method_17780();
        }

        // a few cycles in one go so 25/s also works at 20 ticks per second and at low FPS (the budget keeps the average)
        for (int guard = 0; guard < 6; guard++) {
            net.minecraft.class_2680 state = this.mc.field_1687.method_8320(spot);
            if (state.method_26204() == target) {
                if (!this.mine.getValue()) return;
                float progress = state.method_26165(this.mc.field_1724, this.mc.field_1687, spot);
                if (progress >= 1.0f) {
                    // breaks in one hit: start and finish right now
                    this.mc.field_1761.method_2910(spot, face);
                } else {
                    // needs several ticks: keep mining, the place step follows as soon as it is gone
                    breakingTick = this.tickCounter;
                    this.mc.field_1761.method_2902(spot, face);
                }
                this.mc.field_1724.method_6104(class_1268.field_5808);
                if (this.mc.field_1687.method_8320(spot).method_26204() == target) return;
                if (!this.place.getValue()) {
                    this.budget -= 1.0;
                    this.cycles++;
                }
                continue;
            }
            if (!this.place.getValue() || this.support == null || !state.method_45474()) return;
            if (this.budget < 1.0) return;
            if (this.mc.field_1724.method_6079().method_7909() != wanted) return;
            if (!this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5810, this.support).method_23665()) return;
            this.mc.field_1724.method_6104(class_1268.field_5810);
            this.budget -= 1.0;
            this.cycles++;
            if (this.mc.field_1687.method_8320(spot).method_26204() != target) return;
        }
    }

    private void restockOffhand(class_1792 wanted) {
        class_1799 off = this.mc.field_1724.method_6079();
        if (off.method_7909() == wanted) {
            this.warnedEmpty = false;
            return;
        }
        // only swap when the offhand is free or holds something else and no container is open
        if (this.mc.field_1724.field_7512 != this.mc.field_1724.field_7498) return;
        for (int slot = 0; slot < 36; slot++) {
            if (this.mc.field_1724.method_31548().method_5438(slot).method_7909() != wanted) continue;
            int screenSlot = slot < 9 ? slot + 36 : slot;
            this.mc.field_1761.method_2906(this.mc.field_1724.field_7498.field_7763, screenSlot, 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
            this.warnedEmpty = false;
            return;
        }
        if (!this.warnedEmpty) {
            this.warnedEmpty = true;
            this.mc.field_1724.method_7353(net.minecraft.class_2561.method_43470("§e[Auto Miner] §fNo more " + wanted.method_63680().getString() + " in your inventory"), true);
        }
    }
}
