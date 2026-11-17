package hook.feature;

/**
 * One scrollable area with a scrollbar you can grab and drag (or click the track to jump), plus smooth mouse-wheel
 * scrolling. Every HookVisuals screen that scrolls uses this, so the bar always works the same way everywhere.
 *
 * Usage per frame: {@link #update} with the visible and the content height, draw the content shifted by {@link #pos},
 * then {@link #draw}. Forward mouse press / drag / release to {@link #press}, {@link #drag} and {@link #release}.
 * {@code inverted} is for lists that stick to the bottom (chat): pos 0 = newest at the bottom.
 */
final class UiScrollbar {
    float pos, target, max;
    private final boolean inverted;
    private boolean dragging, drawn;
    private float grab, trackY, trackH, thumbH, barX, hitW, hover;

    UiScrollbar() { this(false); }

    UiScrollbar(boolean inverted) { this.inverted = inverted; }

    void update(float view, float content, float dt) {
        this.max = Math.max(0, content - view);
        this.target = UiEasing.clamp(this.target, 0, this.max);
        if (this.dragging) this.pos = this.target;
        else this.pos += (this.target - this.pos) * (1 - (float) Math.exp(-18 * dt));
        if (Math.abs(this.target - this.pos) < 0.2f) this.pos = this.target;
        this.pos = UiEasing.clamp(this.pos, 0, this.max);
    }

    void reset() { this.pos = this.target = 0; this.dragging = false; }

    void wheel(float delta) { this.target = UiEasing.clamp(this.target + delta, 0, this.max); }

    boolean dragging() { return this.dragging; }

    /** Draws the bar at x (thin line at the right edge of the list) over the track y..y+h. */
    void draw(UiRenderer ui, float x, float y, float h, float u, double mx, double my, float dt) {
        this.drawn = this.max > 0.5f;
        if (!this.drawn) { this.dragging = false; return; }
        this.trackY = y;
        this.trackH = h;
        this.thumbH = Math.max(30 * u, h * h / (h + this.max));
        this.hitW = 16 * u;
        this.barX = x;
        boolean hot = this.dragging || (mx >= x - 6 * u && mx < x + 10 * u && my >= y && my < y + h);
        this.hover += ((hot ? 1f : 0f) - this.hover) * (1 - (float) Math.exp(-16 * dt));
        float w = (3 + 2 * this.hover) * u;
        ui.round(x - (w - 3 * u) / 2, y, w, h, w / 2, UiRenderer.white(0.04f * this.hover));
        ui.round(x - (w - 3 * u) / 2, this.thumbY(), w, this.thumbH, w / 2, UiRenderer.white(this.dragging ? 0.45f : 0.16f + 0.16f * this.hover));
    }

    private float thumbY() {
        float t = this.max <= 0 ? 0 : this.pos / this.max;
        if (this.inverted) t = 1 - t;
        return this.trackY + (this.trackH - this.thumbH) * t;
    }

    /** Mouse down: true when it grabbed the bar (then the click must not go to anything else). */
    boolean press(double mx, double my) {
        if (!this.drawn || mx < this.barX - 6 * (this.hitW / 16) || mx >= this.barX - 6 * (this.hitW / 16) + this.hitW || my < this.trackY || my >= this.trackY + this.trackH) return false;
        float ty = this.thumbY();
        this.grab = my >= ty && my < ty + this.thumbH ? (float) my - ty : this.thumbH / 2;
        this.dragging = true;
        this.drag(my);
        return true;
    }

    void drag(double my) {
        if (!this.dragging) return;
        float free = Math.max(1, this.trackH - this.thumbH);
        float t = UiEasing.clamp(((float) my - this.grab - this.trackY) / free, 0, 1);
        if (this.inverted) t = 1 - t;
        this.target = this.pos = t * this.max;
    }

    void release() { this.dragging = false; }

    // self-test access
    float testBarX() { return this.barX; }

    float testTrackY() { return this.trackY; }

    float testTrackH() { return this.trackH; }
}
