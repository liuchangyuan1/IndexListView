package com.chenxiaov.indexlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * IndexView
 * Created by chenxv on 16/4/8.
 */
@SuppressWarnings("unused")
public class IndexListView extends ListView
        implements AbsListView.OnScrollListener, IndexView.onIndexChangeListener {

    private int indexPaneWidth;

    private IndexSelection mSelection;
    private int currentIndex;
    private IndexView indexView;
    private boolean isSupportIndex;
    private boolean indexViewIsLayout;

    public IndexListView(Context context) {
        this(context, null);
    }

    public IndexListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void indexSupport() {
        try {
            mSelection = (IndexSelection) getAdapter();
            isSupportIndex = true;
        } catch (ClassCastException e) {
            throw new RuntimeException("ListView adapter must extends IndexViewListAdapter.");
        }
        indexPaneWidth = (int) DensityUtil.dpToPx(35, getContext());
        indexView = new IndexView(mSelection.title(), mSelection.indexValues(), this, getContext());
        indexView.setSelectionChangeListener(this);
        setOnScrollListener(this);
        setFocusableInTouchMode(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (isSupportIndex && !indexViewIsLayout) {
            r = getAbsRight(l, r);
            b = getAbsBottom(t, b);
            indexView.setPosition(r - indexPaneWidth, t, r, b);
            indexViewIsLayout = true;
        }
    }

    private int getAbsBottom(int t, int b) {
        b -= t;
        return b;
    }

    private int getAbsRight(int l, int r) {
        r -= l;
        return r;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isSupportIndex) {
            indexView.draw(canvas);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(l);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (isSupportIndex) {
            boolean isTouch = indexView.onTouchEvent(ev);
            if (isTouch) {
                return true;
            }
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int index = mSelection.getIndexByItem(firstVisibleItem);
        if (index != currentIndex) {
            currentIndex = index;
            setIndexViewSelection(index);
        }
    }

    private void setIndexViewSelection(int index) {
        indexView.setCurrentIndex(index);
    }

    @Override
    public void onIndexChange(int index) {
        setSelection(mSelection.getSelectionByIndex(index));
    }

    @SuppressWarnings("unused")
    public void isSmoothScrollForSelectionChange(boolean isSmooth) {
        if (indexView != null) {
            indexView.isSmoothScroll(isSmooth);
        }
    }

    @SuppressWarnings("unused")
    public void isSmoothScrollForClick(boolean isSmooth) {
        if (indexView != null) {
            indexView.isSmoothScrollForClick(isSmooth);
        }
    }
}
