package hook.feature;

import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3965;

/** Loops: right-click the crate you look at -> nether star -> confirm (lime glass) until stopped. (Ported from SilentClient.) */
public final class CrateOpenerModule extends ContainerMacroModule {
    private enum State { USE_CRATE, WAIT_SELECT, DELAY_STAR, CLICK_STAR, WAIT_CONFIRM, DELAY_CONFIRM, CLICK_CONFIRM, WAIT_CYCLE }

    private State state = State.USE_CRATE;
    private class_3965 crate;

    public CrateOpenerModule() {
        super("Crate Opener", "Opens the crate you look at on repeat until you stop it");
    }

    @Override
    protected String tag() {
        return "Crate Opener";
    }

    @Override
    protected void restart() {
        this.next(State.USE_CRATE);
    }

    @Override
    public void onEnable() {
        this.crate = this.mc.field_1765 instanceof class_3965 hit && hit.method_17783() == class_239.class_240.field_1332 ? hit : null;
        super.onEnable();
    }

    private void next(State state) {
        this.state = state;
        this.timer = 0;
    }

    @Override
    protected void step(class_310 client) {
        if (client.field_1755 == null && client.field_1765 instanceof class_3965 hit && hit.method_17783() == class_239.class_240.field_1332) {
            this.crate = hit;
        }
        switch (this.state) {
            case USE_CRATE -> {
                if (client.field_1755 != null) closeScreen(client);
                if (this.crate == null) {
                    if (this.timer % 20 == 0) this.actionBar("Look at the crate", class_124.field_1054);
                    return;
                }
                if (client.field_1761 != null) {
                    client.field_1761.method_2896(client.field_1724, class_1268.field_5808, this.crate);
                    client.field_1724.method_6104(class_1268.field_5808);
                }
                this.next(State.WAIT_SELECT);
            }
            case WAIT_SELECT -> {
                class_1703 menu = menu(client);
                String title = title(client);
                boolean select = menu != null && (title.contains("item") || title.contains("select") || title.contains("auswählen") || title.contains("auswaehlen")
                        || findSlot(menu, stack -> itemPath(stack).contains("nether_star")) >= 0);
                if (select) this.next(State.DELAY_STAR); else this.retryAfterTimeout();
            }
            case DELAY_STAR -> {
                if (this.timer >= this.actionDelay.getIntValue()) this.next(State.CLICK_STAR);
            }
            case CLICK_STAR -> {
                class_1703 menu = menu(client);
                if (menu == null) {
                    this.next(State.USE_CRATE);
                    return;
                }
                int slot = findSlot(menu, stack -> itemPath(stack).contains("nether_star")
                        || (itemName(stack).contains("nether") && itemName(stack).contains("star")) || itemName(stack).contains("netherstern"));
                if (slot < 0 && menu.field_7761.size() > 19) slot = menu.field_7761.get(19).field_7874;
                if (slot >= 0) {
                    click(client, menu, slot);
                    this.next(State.WAIT_CONFIRM);
                } else {
                    this.retryAfterTimeout();
                }
            }
            case WAIT_CONFIRM -> {
                class_1703 menu = menu(client);
                String title = title(client);
                boolean confirm = menu != null && (title.contains("confirm") || title.contains("crate") || title.contains("bestätigen") || title.contains("bestaetigen")
                        || findSlot(menu, ContainerMacroModule::isConfirmItem) >= 0);
                if (confirm) this.next(State.DELAY_CONFIRM); else this.retryAfterTimeout();
            }
            case DELAY_CONFIRM -> {
                if (this.timer >= this.actionDelay.getIntValue()) this.next(State.CLICK_CONFIRM);
            }
            case CLICK_CONFIRM -> {
                class_1703 menu = menu(client);
                if (menu == null) {
                    this.next(State.USE_CRATE);
                    return;
                }
                int slot = findSlot(menu, ContainerMacroModule::isConfirmItem);
                if (slot < 0 && menu.field_7761.size() > 15) slot = menu.field_7761.get(15).field_7874;
                if (slot >= 0) {
                    click(client, menu, slot);
                    this.next(State.WAIT_CYCLE);
                } else {
                    this.retryAfterTimeout();
                }
            }
            case WAIT_CYCLE -> {
                if (this.timer >= this.cycleDelay.getIntValue()) {
                    if (this.autoClose.getValue() && client.field_1755 != null) closeScreen(client);
                    this.next(State.USE_CRATE);
                }
            }
        }
    }
}
