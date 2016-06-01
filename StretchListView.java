import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ListView;
import org.ddrc.ksbmobilebank.R;


public class StretchListView extends ListView implements View.OnTouchListener {

    private static final int MAX_Y_OVERSCROLL_DISTANCE = 500;
    private int touchOnItem = -1;
    private int itemsOnScreen = -1;
    private int itemHeight = -1;
    private int dividerDefaultHeight = -1;
    private int topOffset;
    private boolean overScrollEnabled = true;
    private int itemAmount = 0;


    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public boolean isOverScrollEnabled() {
        return overScrollEnabled;
    }

    public void setOverScrollEnabled(boolean overScrollEnabled) {
        this.overScrollEnabled = overScrollEnabled;
    }

    public StretchListView(Context context) {
        super(context);
    }

    public StretchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStretchListView();
    }

    public int getTopOffset() {
        return topOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public StretchListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStretchListView();
    }

    private void initStretchListView() {
        dividerDefaultHeight = getResources().getDimensionPixelSize(R.dimen.drawer_list_divider_height);
        setOnTouchListener(this);
    }

    int lastDeltaY = 0;

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (beingDragged && overScrollEnabled && itemAmount > 1) {
            int first = getFirstVisiblePosition();
            int last = getLastVisiblePosition();
            int myDelta = deltaY + lastDeltaY;

            if (myDelta > MAX_Y_OVERSCROLL_DISTANCE) {
                myDelta = MAX_Y_OVERSCROLL_DISTANCE;
            } else if (myDelta < -MAX_Y_OVERSCROLL_DISTANCE) {
                myDelta = -MAX_Y_OVERSCROLL_DISTANCE;
            }

            if (myDelta > 0) {
                // overscroll bottom
                for (int i = 1; i <= itemsOnScreen - touchOnItem; i++) {
                    View item = getChildAt((last - first) - i - 1);
                    if (item != null) {
                        int someVal = myDelta / (i + 5);
                        changeDividerHeight(item.findViewById(R.id.progress_footer_header_divider), someVal);
                        setSelection(last);
                    }
                }
            } else {
                // overscroll top.
                for (int i = 1; i <= touchOnItem; i++) {
                    View item = getChildAt(i);
                    if (item != null)
                        changeDividerHeight(item.findViewById(R.id.progress_footer_header_divider), myDelta / (i + 5));
                }
            }

            lastDeltaY = myDelta;
        }
        return false;
    }


    public void changeDividerHeight(View view, int height) {
        if (Math.abs(height) > dividerDefaultHeight) {
            if (view != null) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = Math.abs(height);
                view.requestLayout();
                requestLayout();
            }
        }
    }

    boolean beingDragged = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int first = getFirstVisiblePosition();
        int last = getLastVisiblePosition();
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
            for (int i = 0; i <= last - first; i++) {
                View view = getChildAt(i);
                if (view != null) {
                    view = view.findViewById(R.id.progress_footer_header_divider);
                    if (view != null)
                        view.startAnimation(new ResizeAnimation(view, view.getHeight(), dividerDefaultHeight));
                }
            }
            beingDragged = false;
            lastDeltaY = 0;
            touchOnItem = -1;
        } else {
            if (itemsOnScreen == -1) {
                itemsOnScreen = last - first;
            }
            if (itemHeight == -1) {
                View childView = getChildAt(1);
                if (childView != null)
                    itemHeight = childView.getHeight();
            }

            if (touchOnItem == -1) {
                float currentPosition = event.getY() - topOffset;
                if (currentPosition > 0)
                    if (currentPosition > itemHeight) {
                        touchOnItem = (int) (currentPosition / itemHeight);
                    } else {
                        touchOnItem = 0;
                    }
            }
            beingDragged = true;
        }

        return false;
    }


    public class ResizeAnimation extends Animation {
        private View mView;
        private float mToHeight;
        private float mFromHeight;

        public ResizeAnimation(View v, float fromHeight, float toHeight) {
            mToHeight = toHeight;
            mFromHeight = fromHeight;
            mView = v;
            setDuration(300);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            ViewGroup.LayoutParams p = mView.getLayoutParams();
            p.height = (int) height;
            mView.requestLayout();
        }
    }
}