package io.climintv.home.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * TV-optimized ScrollView that preserves D-pad focus state.
 * Standard Android ScrollView unconditionally runs FocusFinder from (0,0) down
 * during layout passes (via restoreDefaultFocus), stealing focus away from child
 * carousels back to the top of the screen.
 *
 * TvScrollView guards against focus-stealing by ensuring existing descendant focus
 * is strictly preserved across all layout and scroll traversals.
 */
public class TvScrollView extends ScrollView {

    public TvScrollView(Context context) {
        super(context);
        init();
    }

    public TvScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TvScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(false);
        setFocusableInTouchMode(false);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // If this hierarchy already has a focused descendant, NEVER re-assign focus
        View current = findFocus();
        if (current != null && current != this) {
            return true;
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    @Override
    public boolean restoreDefaultFocus() {
        // If a descendant is currently focused, keep focus intact
        View current = findFocus();
        if (current != null && current != this) {
            return true;
        }
        return super.restoreDefaultFocus();
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (focused != null) {
            Rect rect = new Rect();
            focused.getDrawingRect(rect);
            offsetDescendantRectToMyCoords(focused, rect);
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(rect);
            if (scrollDelta != 0) {
                smoothScrollBy(0, scrollDelta);
            }
        }
    }
}
