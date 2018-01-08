package com.thetestament.cread.listeners;

import android.view.MotionEvent;
import android.view.View;

/**
 * Class to provide dragging functionality.
 */

public class OnDragTouchListener implements View.OnTouchListener {

    /**
     * View to be dragged.
     */
    private View mView;
    /**
     * Parent view of the view to be dragged.
     */
    private View mParent;

    /**
     * Flag to maintain width of the view to be dragged.
     */
    private int width;
    /**
     * Flag to maintain height of the view to be dragged.
     */
    private int height;

    /**
     * Flag to store initial x position of view to be dragged.
     */
    private float xWhenAttached;
    /**
     * Flag to store initial y position of view to be dragged.
     */
    private float yWhenAttached;

    /**
     * Flag to store initial last x position of view to be dragged.
     */
    private float xWhenDeAttached;
    /**
     * Flag to store last y position of view to be dragged.
     */
    private float yWhenDeAttached;
    /**
     * Flag to maintain x position of view to be dragged.
     */
    private float dX;

    /**
     * Flag to maintain y position of view to be dragged.
     */
    private float dY;

    /**
     * Flags to maintain parent view x coordinate.
     */
    private float maxLeft, maxRight;
    /**
     * Flags to maintain parent view y coordinate.
     */
    private float maxTop, maxBottom;

    /**
     * Flags to maintain view dragging status.
     */
    private boolean isDragging;


    private boolean isInitialized = false;


    private OnDragActionListener mOnDragActionListener;


    /**
     * Interface definition for a callback to be invoked when user drag or stop dragging.
     */
    public interface OnDragActionListener {
        /**
         * Called when drag event is started.
         *
         * @param view The view dragged.
         */
        void onDragStart(View view);

        /**
         * Called when drag event is completed.
         *
         * @param view The view dragged.
         */
        void onDragEnd(View view);
    }

    /**
     * Register a callback to be invoked when user drag or stop dragging.
     */
    public void setOnDragActionListener(OnDragActionListener onDragActionListener) {
        mOnDragActionListener = onDragActionListener;
    }

    /**
     * Constructor
     *
     * @param view View to be dragged.
     */
    public OnDragTouchListener(View view) {
        this(view, (View) view.getParent(), null);
    }

    /**
     * Constructor
     *
     * @param view   View to be dragged.
     * @param parent Parent view of view to be dragged.
     */
    public OnDragTouchListener(View view, View parent) {
        this(view, parent, null);
    }

    /**
     * Constructor
     *
     * @param view                 View to be dragged.
     * @param onDragActionListener DragListener reference.
     */
    public OnDragTouchListener(View view, OnDragActionListener onDragActionListener) {
        this(view, (View) view.getParent(), onDragActionListener);
    }

    /**
     * Constructor
     *
     * @param view                 View to be dragged.
     * @param parent               Parent view of view to be dragged.
     * @param onDragActionListener DragListener reference.
     */
    public OnDragTouchListener(View view, View parent, OnDragActionListener onDragActionListener) {
        initListener(view, parent);
        //Set drag listener
        setOnDragActionListener(onDragActionListener);
    }


    /**
     * Initialize member variables.
     *
     * @param view   View to be dragged.
     * @param parent Parent view of view to be dragged.
     */
    void initListener(View view, View parent) {
        mView = view;
        mParent = parent;
        //Initialize variables
        isDragging = false;
        isInitialized = false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //User is dragging the view
        if (isDragging) {
            float[] bounds = new float[4];
            // LEFT
            bounds[0] = event.getRawX() + dX;
            if (bounds[0] < maxLeft) {
                bounds[0] = maxLeft;
            }
            // RIGHT
            bounds[2] = bounds[0] + width;
            if (bounds[2] > maxRight) {
                bounds[2] = maxRight;
                bounds[0] = bounds[2] - width;
            }
            // TOP
            bounds[1] = event.getRawY() + dY;
            if (bounds[1] < maxTop) {
                bounds[1] = maxTop;
            }
            // BOTTOM
            bounds[3] = bounds[1] + height;
            if (bounds[3] > maxBottom) {
                bounds[3] = maxBottom;
                bounds[1] = bounds[3] - height;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    onDragFinish();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //mView.animate().x(bounds[0]).y(bounds[1]).setDuration(0).start();
                    mView.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;
            }
            return true;
        }
        //User is not dragging the view
        else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDragging = true;
                    if (!isInitialized) {
                        updateBounds();
                    }
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    if (mOnDragActionListener != null) {
                        mOnDragActionListener.onDragStart(mView);
                    }
                    return true;
            }
        }
        return true;
    }


    /**
     * Method to update views bounds.
     */
    void updateBounds() {
        updateViewBounds();
        updateParentBounds();
        //Toggle flag
        isInitialized = true;
    }

    /**
     * Method to update bound of the view to be dragged.
     */
    void updateViewBounds() {
        width = mView.getWidth();
        xWhenAttached = mView.getX();
        xWhenDeAttached = xWhenAttached;
        dX = 0;

        height = mView.getHeight();
        yWhenAttached = mView.getY();
        yWhenDeAttached = yWhenAttached;
        dY = 0;
    }

    /**
     * Method to update bound of parent view.
     */
    void updateParentBounds() {
        maxLeft = 0;
        maxRight = maxLeft + mParent.getWidth();

        maxTop = 0;
        maxBottom = maxTop + mParent.getHeight();
    }

    /**
     * Method to reset variables when view dragging finished.
     */
    private void onDragFinish() {
        //Reset variables
        dX = 0;
        dY = 0;
        isDragging = false;


        if (mOnDragActionListener != null) {
            //Drag listener
            if (xWhenDeAttached == mView.getX() && yWhenDeAttached == mView.getY())
                mOnDragActionListener.onDragEnd(mView);
            xWhenDeAttached = mView.getX();
            yWhenDeAttached = mView.getY();
        }
    }


}