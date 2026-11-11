package hook.feature;

import net.minecraft.class_1703;
import net.minecraft.class_310;
import net.minecraft.class_465;

/** Loops /xpshop -> red candle -> confirm (lime glass) until stopped. (Ported from SilentClient.) */
public final class XpShopModule extends ContainerMacroModule {
    private enum State { SEND_COMMAND, WAIT_SHOP, DELAY_CANDLE, CLICK_CANDLE, WAIT_CONFIRM, DELAY_CONFIRM, CLICK_CONFIRM, WAIT_CYCLE }

    private State state = State.SEND_COMMAND;

    public XpShopModule() {
        super("XP Shop", "Buys XP from /xpshop on repeat until you stop it");
    }

    @Override
    protected String tag() {
        return "XP Shop";
    }

    @Override
    protected void restart() {
        this.next(State.SEND_COMMAND);
    }

    private void next(State state) {
        this.state = state;
        this.timer = 0;
    }

    @Override
    protected void step(class_310 client) {
        switch (this.state) {
            case SEND_COMMAND -> {
                if (client.field_1755 != null) closeScreen(client);
                client.field_1724.field_3944.method_45730("xpshop");
                this.next(State.WAIT_SHOP);
            }
            case WAIT_SHOP -> {
                String title = title(client);
                if (client.field_1755 instanceof class_465<?> && title.contains("xp") && title.contains("shop")) {
                    this.next(State.DELAY_CANDLE);
                } else {
                    this.retryAfterTimeout();
                }
            }
            case DELAY_CANDLE -> {
                if (this.timer >= this.actionDelay.getIntValue()) this.next(State.CLICK_CANDLE);
            }
            case CLICK_CANDLE -> {
                class_1703 menu = menu(client);
                if (menu == null) {
                    this.next(State.SEND_COMMAND);
                    return;
                }
                int slot = findSlot(menu, stack -> itemPath(stack).contains("red_candle") || itemName(stack).contains("red candle") || itemName(stack).contains("rote kerze"));
                if (slot < 0 && menu.field_7761.size() > 25) slot = menu.field_7761.get(25).field_7874; // known layout fallback
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
                boolean confirm = menu != null && (title.contains("confirm") || title.contains("bestätigen") || title.contains("bestaetigen")
                        || findSlot(menu, ContainerMacroModule::isConfirmItem) >= 0);
                if (confirm) this.next(State.DELAY_CONFIRM); else this.retryAfterTimeout();
            }
            case DELAY_CONFIRM -> {
                if (this.timer >= this.actionDelay.getIntValue()) this.next(State.CLICK_CONFIRM);
            }
            case CLICK_CONFIRM -> {
                class_1703 menu = menu(client);
                if (menu == null) {
                    this.next(State.SEND_COMMAND);
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
                    this.next(State.SEND_COMMAND);
                }
            }
        }
    }
}
