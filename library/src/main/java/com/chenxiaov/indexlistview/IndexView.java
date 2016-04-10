package com.chenxiaov.indexlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

/**
 * Index View
 * Created by chenxv on 16/4/8.
 */
public class IndexView {

    private static final PorterDuffXfermode MODE = new PorterDuffXfermode(PorterDuff.Mode.XOR);
    private int left;

    private int right;
    private int top;
    private int width;
    private int bottom;
    private int height;
    private int indexSize;
    private int titleHeight;
    private int indexViewTop;
    private int wrapperSpeed;
    private int currentIndex;
    private int targetWrapperY;
    private int currentWrapperY;

    private boolean smooth;
    private boolean isTouchNow;
    private boolean smoothScroll;
    private boolean smoothScrollForClick;

    private Context ctx;
    private Paint mPaint;
    private View parentView;
    private String titleValue;
    private List<String> values;
    private IndexWrapper wrapper;
    private TitleView titleViewView;
    private List<IndexText> indexTextList;
    private onIndexChangeListener selectionChangeListener;

    public IndexView(String titleValue, List<String> values, View parentView, Context ctx) {
        init();
        this.titleValue = titleValue;
        this.values = values;
        this.parentView = parentView;
        this.ctx = ctx;
    }

    private void init() {
        this.wrapperSpeed = (int) DensityUtil.dpToPx(5, ctx);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.smoothScroll = true;
        this.smoothScrollForClick = true;
        this.smooth = true;
        this.titleHeight = (int) DensityUtil.dpToPx(50, ctx);
    }

    public void setSelectionChangeListener(onIndexChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setPosition(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.height = bottom - top;
        this.width = right - left;
        this.indexSize = getIndexSize();
        this.indexViewTop = getIndexViewTop();
        this.titleViewView = new TitleView(
                left, this.indexViewTop, titleValue, width, titleHeight, mPaint, ctx
        );
        this.wrapper = new IndexWrapper(left, width, indexSize);
        this.currentWrapperY = indexViewTop;
        this.targetWrapperY = currentWrapperY;
        initText();
    }

    private void initText() {
        indexTextList = new LinkedList<>();
        for (int i = 0; i < values.size(); i++) {
            indexTextList.add(new IndexText(
                    i, indexSize, indexViewTop, left, width, values.get(i), mPaint, ctx)
            );
        }
    }

    private int getIndexSize() {
        return Math.min(width, (height - titleHeight) / values.size());
    }

    private int getTargetWrapperY() {
        return indexViewTop + indexSize * currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        if (isTouchNow) {
            return;
        }
        this.currentIndex = currentIndex;
        this.targetWrapperY = getTargetWrapperY();
        invalidate();
    }

    public void isSmoothScrollForClick(boolean smooth) {
        this.smoothScrollForClick = smooth;
    }

    public void isSmoothScroll(boolean smooth) {
        this.smoothScroll = smooth;
    }

    private int getIndexViewTop() {
        int topTmp = (height >> 1) - (indexSize * values.size() >> 1) + top;
        return topTmp <= titleHeight ? titleHeight : topTmp;
    }

    public boolean isTouch(float x, float y) {
        return x > left && x < right && y < bottom && y > top;
    }

    public boolean onTouchEvent(MotionEvent event) {

        if (isGiveUpHandleTouchEvent(event)) {
            return false;
        }

        int touchIndex = getIndexByY(event.getY());
        if (isValidIndex(touchIndex) && event.getY() > indexViewTop) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    actionDownHandle(event);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    actionMoveHandle(event);
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    actionUpHandle(touchIndex);
                    break;
                }
            }
            if (selectionChangeListener != null) {
                selectionChangeListener.onIndexChange(touchIndex);
            }
        }
        return true;
    }

    private boolean isGiveUpHandleTouchEvent(MotionEvent event) {
        return !isTouch(event.getX(), event.getY()) && !isTouchNow;
    }

    private void actionUpHandle(int touchIndex) {
        smooth = smoothScrollForClick;
        isTouchNow = false;
        setCurrentIndex(touchIndex);
    }

    private void actionMoveHandle(MotionEvent event) {
        targetWrapperY = (int) event.getY() - (indexSize >> 1);
        targetWrapperY = targetWrapperY < indexViewTop ? indexViewTop : targetWrapperY;
        smooth = true;
    }

    private void actionDownHandle(MotionEvent event) {
        isTouchNow = true;
        targetWrapperY = (int) event.getY() - (indexSize >> 1);
        smooth = smoothScrollForClick;
        invalidate();
    }

    private int getIndexByY(float y) {
        return getIndexByXY(left, y);
    }

    private int getIndexByXY(float x, float y) {
        int index = -1;
        int height = indexViewTop;
        for (IndexText textView : indexTextList) {
            if (!isValidIndex(index)) {
                index = textView.touchIndex(x, y);
            } else {
                return index;
            }
        }
        if (!isValidIndex(index)) {
            if (y > height) {
                return indexTextList.size() - 1;
            }
        }
        return index;
    }

    private boolean isValidIndex(int index) {
        return index != -1;
    }

    public void draw(Canvas canvas) {
        if (canvas == null || parentView == null) {
            return;
        }
        mPaint.reset();
        int src = saveLayer(canvas);
        drawTitleText(canvas);
        drawIndexText(canvas);
        drawWrapperShape(canvas);
        restoreLayer(canvas, src);
        WrapperAnimateControl();

    }

    private void WrapperAnimateControl() {
        if (currentWrapperY != targetWrapperY) {
            if (!smooth) {
                currentWrapperY = targetWrapperY;
                smooth = smoothScroll;
                invalidate();
                return;
            }
            int remainder = targetWrapperY - currentWrapperY;
            if (currentWrapperY < targetWrapperY) {
                currentWrapperY += wrapperSpeed > remainder ? remainder : wrapperSpeed;
            } else if (currentWrapperY > targetWrapperY) {
                currentWrapperY -= wrapperSpeed > -remainder ? -remainder : wrapperSpeed;
            }
            invalidate();
        }
    }

    private int saveLayer(Canvas canvas) {
        return canvas.saveLayer(left, top, right, bottom, null, Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
    }

    private void restoreLayer(Canvas canvas, int src) {
        if (src == 1) {
            canvas.restoreToCount(src);
        }
    }

    private void drawWrapperShape(Canvas canvas) {
        wrapper.drawWrapper(currentWrapperY, canvas, mPaint);
    }

    private void drawTitleText(Canvas canvas) {
        titleViewView.drawTitle(canvas);
    }

    private void drawIndexText(Canvas canvas) {
        for (IndexText indexText : indexTextList) {
            indexText.draw(canvas);
        }
    }

    private void invalidate() {
        if (parentView != null) {
            parentView.invalidate();
        }
    }

    static class TitleView {

        private int fontSize;
        private int x;
        private int y;
        private int width;
        private int titleViewHeight;
        private Rect mRect;
        private Paint mPaint;
        private String value;

        public TitleView(int x, int y, String value, int width, int titleViewHeight, Paint mPaint, Context ctx) {
            this.fontSize = (int) DensityUtil.spToPx(14, ctx);
            this.mRect = new Rect();
            this.mPaint.setTextSize(fontSize);
            this.mPaint.getTextBounds(value, 0, value.length(), mRect);
            this.value = value;
            this.width = width;
            this.titleViewHeight = titleViewHeight;
            this.mPaint = mPaint;
            this.x = getCenterX(x);
            this.y = getTitleY(y);
        }

        private int getTitleY(int y) {
            return y - (titleViewHeight >> 1) + (mRect.height() >> 1);
        }

        private int getCenterX(int x) {
            return x + (width >> 1) - (mRect.width() >> 1);
        }

        public void drawTitle(Canvas canvas) {
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(fontSize);
            canvas.drawText(value, x, y, mPaint);
        }

    }

    static class IndexWrapper {

        private int left;
        private int width;
        private int size;
        private int radius;
        private int x;
        private float y;

        public IndexWrapper(int left, int width, int size) {
            this.left = left;
            this.width = width;
            this.size = size;
            this.radius = size >> 1;
            this.x = getX();
        }

        private int getX() {
            int leftX = left + (width >> 1) - radius;
            return (leftX + leftX + size) >> 1;
        }

        public void drawWrapper(float y, Canvas canvas, Paint paint) {
            this.y = y + radius;
            paint.setXfermode(MODE);
            paint.setColor(Color.RED);
            canvas.drawCircle(x, this.y, radius, paint);
        }
    }

    static class IndexText {

        private int index;
        private int top;
        private int left;
        private int bottom;
        private int right;
        private String value;
        private Paint mPaint;
        private int x;
        private int y;
        private Rect textRect;
        private int fontSize;

        public IndexText(int index, int size, int top, int left, int width, String value, Paint mPaint, Context ctx) {
            this.fontSize = (int) DensityUtil.spToPx(14, ctx);
            this.index = index;
            this.textRect = new Rect();
            this.mPaint = mPaint;
            this.mPaint.setTextSize(fontSize);
            this.mPaint.getTextBounds(value, 0, value.length(), textRect);
            this.top = getRealTop(top, size, index);
            this.bottom = getRealBottom(this.top, size);
            this.left = left;
            this.right = left + size;
            this.value = value;
            this.y = getRealY(top, index, size);
            this.x = getRealX(left, width);
        }

        private int getRealTop(int top, int size, int index) {
            return top + size * index;
        }

        private int getRealBottom(int realTop, int size) {
            return realTop + size;
        }

        private int getRealX(int left, int width) {
            return left + (width >> 1) - (textRect.width() >> 1);
        }

        private int getRealY(int top, int index, int size) {
            return top + index * size + (size >> 1) + (textRect.height() >> 1);
        }

        public void draw(Canvas canvas) {
            canvas.drawText(value, x, y, mPaint);
        }

        public int touchIndex(float x, float y) {

            if (x >= left && x <= right && y <= bottom && y >= top) {
                return index;
            } else {
                return -1;
            }
        }

    }

    public interface onIndexChangeListener {
        void onIndexChange(int index);
    }

}
