package com.github.toast;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;


public class ToastUtils {
    private static Activity context;
    private WindowManager.LayoutParams layoutParams;
    private static boolean needRemovePreView;
    private static Application.ActivityLifecycleCallbacks callback = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    private static void setTopActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        if (context != null && context != activity) {
            /*如果showtoast后马上跳转其他activity，则需要把之前的视图移除*/
            needRemovePreView = true;
        }
        context = activity;
        if (needFinishShow) {
            /*如果toast后activity马上finish，则需要在后面的activity显示toast*/
            if (toastView != null) {
                toastView.show();
            }
            needFinishShow = false;
        }
    }

    public static void init(Application application) {
        if (application == null) {
            return;
        }
        application.unregisterActivityLifecycleCallbacks(callback);
        application.registerActivityLifecycleCallbacks(callback);
    }

    private static Activity getContext() {
        return context;
    }

    private static ToastUtils toastView;

    private static Handler handler = new Handler(Looper.getMainLooper());
    private static final int SHORT_DURATION_MS = 2000;//1500;
    private static final int LONG_DURATION_MS = 4000;//2750;

    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;


    private View contentView;
    private int duration = LENGTH_SHORT;
    private long timeMillis;
    private ValueAnimator animator;
    private static WindowManager manger;

    public ToastUtils(View content) {
        this.contentView = content;
    }

    /******************************************************************************/
    public static void showToast(String msg) {
        make(msg, LENGTH_SHORT).show();
    }

    public static void showToastL(String msg) {
        make(msg, LENGTH_LONG).show();
    }

    public static void showToast(int msg) {
        make(getContext().getString(msg), LENGTH_SHORT).show();
    }

    public static void showToastL(int msg) {
        make(getContext().getString(msg), LENGTH_LONG).show();
    }

    /******************************************************************************/
    private static boolean needFinishShow;

    public static void showToastFinish(CharSequence msg) {
        make(msg, LENGTH_SHORT);
        needFinishShow = true;
    }

    public static void showToastLFinish(CharSequence msg) {
        make(msg, LENGTH_LONG);
        needFinishShow = true;
    }

    public static void showToastFinish(int msg) {
        make(getContext().getString(msg), LENGTH_SHORT);
        needFinishShow = true;
    }

    public static void showToastLFinish(int msg) {
        make(getContext().getString(msg), LENGTH_LONG);
        needFinishShow = true;
    }

    /******************************************************************************/
    private static ToastUtils make(int text, int duration) {
        return make(getContext().getString(text), duration);
    }

    private WindowManager getManger() {
        if (manger == null || needRemovePreView) {
            manger = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        }
        return manger;
    }

    private WindowManager.LayoutParams getWindowManagerLayoutParams() {
        if (layoutParams == null || needRemovePreView) {
            layoutParams = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
            );
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            /*layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;*/
        }
        return layoutParams;
    }

    private static ToastUtils make(CharSequence text, int duration) {
        if (toastView != null) {
            toastView.setText(text);
            toastView.setDuration(duration);
            return toastView;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View content = inflater.inflate(R.layout.layout_zr_toast, null);
        FrameLayout flToastGroup = content.findViewById(R.id.flToastGroup);
        /*需要设置内边距，所以需要viewgroup*/
        flToastGroup.setPadding(0, 0, 0, (int) (Resources.getSystem().getDisplayMetrics().density * 60));
        toastView = new ToastUtils(content);
        toastView.setText(text);
        toastView.setDuration(duration);
        return toastView;
    }


    private static final long animTime = 320;

    private static boolean isActivityAlive(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    public void show() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showOnUi();
            return;
        }
        if (!isActivityAlive(getContext())) {
            return;
        }
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showOnUi();
            }
        });
    }

    private void showOnUi() {
        if (!isActivityAlive(getContext())) {
            return;
        }
        if (contentView == null) {
            return;
        }
        try {
            if (needRemovePreView) {
                /*如果跳转了act，上一个activity还没移除的view,需要移除view再添加*/
                handler.removeCallbacks(runnable);
                removeContentViewRightNow();
            }
            if (contentView.getParent() == null) {
                contentView.setAlpha(0);
                getManger().addView(contentView, getWindowManagerLayoutParams());
                startAlphaAnimator(true, 0);
                needRemovePreView = false;
            } else {
                /*移除之前的toast视图*/
                if (animator != null) {
                    animator.cancel();
                }
                float alpha = contentView.getAlpha();
                handler.removeCallbacks(runnable);
                startAlphaAnimator(true, alpha);
            }
            long dismissTime;
            if (timeMillis > 0) {
                dismissTime = timeMillis;
            } else {
                dismissTime = duration == LENGTH_LONG ? LONG_DURATION_MS : SHORT_DURATION_MS;
            }
            handler.postDelayed(runnable, dismissTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            dismissOnUi();
            return;
        }
        if (!isActivityAlive(getContext())) {
            return;
        }
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissOnUi();
            }
        });
    }

    private void dismissOnUi() {
        removeContentViewRightNow();
    }

    private ValueAnimator startAlphaAnimator(final boolean show, float startAlpha) {
        if (startAlpha < 0 || startAlpha > 1) {
            startAlpha = show ? 0 : 1;
        }
        animator = new ValueAnimator();
        if (show) {
            animator.setFloatValues(startAlpha, 1f);
        } else {
            animator.setFloatValues(startAlpha, 0);
        }
        if (startAlpha != 0 && startAlpha != 1) {
            animator.setDuration((long) (animTime * (1f - startAlpha)));
        } else {
            animator.setDuration(animTime);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (show) {
                    float value = (float) animation.getAnimatedValue();
                    contentView.setAlpha(value);
                } else {
                    if (contentView != null) {
                        float value = (float) animation.getAnimatedValue();
                        if (value <= 0) {
                            /*因为调用animator.cancel();方法时，会走end监听，此时不需要移除view，所以在update回调里面通过value值判断是否移除view*/
                            removeContentViewRightNow();
                        } else {
                            contentView.setAlpha(value);
                        }
                    }
                }
            }
        });
        animator.start();
        return animator;
    }

    private void removeContentViewRightNow() {
        if (!canRemoveContentView()) {
            return;
        }
        if (!isActivityAlive(getContext())) {
            return;
        }
        getManger().removeView(contentView);
    }

    private boolean canRemoveContentView() {
        if (contentView == null) {
            return false;
        }
        ViewParent parent = contentView.getParent();
        if (parent == null) {
            return false;
        }
        return true;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!canRemoveContentView()) {
                return;
            }
            startAlphaAnimator(false, 1);
        }
    };


    /*****************************************************************************************************************/

    public ToastUtils setDuration(int duration) {
        if (duration != LENGTH_SHORT && duration != LENGTH_LONG) {
            duration = LENGTH_SHORT;
        }
        this.duration = duration;
        return this;
    }

    public ToastUtils setText(CharSequence text) {
        if (text == null) {
            text = "";
        }
        if (contentView != null) {
            TextView tvToastText = contentView.findViewById(R.id.tvToastText);
            if (tvToastText != null) {
                tvToastText.setText(text);
            }
        }
        return this;
    }

    public View getContentView() {
        return contentView;
    }

    public ToastUtils setContentView(View contentView) {
        this.contentView = contentView;
        return this;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public ToastUtils setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
        return this;
    }

    public ToastUtils setGravity(int gravity) {
        getWindowManagerLayoutParams().gravity = gravity;
        return this;
    }
}
