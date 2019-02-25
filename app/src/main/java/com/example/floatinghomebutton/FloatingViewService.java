package com.example.floatinghomebutton;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.concurrent.TimeUnit;


public class FloatingViewService extends Service {

    private WindowManager mWindowManager;
    private View floatingView;
    boolean isViewExpanded = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Inflate the floating view layout we created
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_opened_items, null);

        //Use different flag for older Android versions
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(floatingView, params);

        foldLayout();

        LinearLayout linearLayout1 = floatingView.findViewById(R.id.linearLayout1);
        linearLayout1.setOnClickListener(
                v -> {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
        );

        LinearLayout linearLayout2 = floatingView.findViewById(R.id.linearLayout2);
        linearLayout2.setOnClickListener(
                v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("sms:"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
        );

        LinearLayout linearLayout4 = floatingView.findViewById(R.id.linearLayout4);
        linearLayout4.setOnClickListener(
                v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
        );

        LinearLayout linearLayout5 = floatingView.findViewById(R.id.linearLayout5);
        linearLayout5.setOnClickListener(
                v -> {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
        );

        //Move view around screen
        floatingView.findViewById(R.id.imageViewExit).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            double deltaTime, startTime;
            boolean moved = false;
            boolean animating = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moved = false;
                        //Remember the initial position.
                        initialX = params.x;
                        initialY = params.y;
                        startTime = System.currentTimeMillis();

                        //Get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        int XDiff = (int) (event.getRawX() - initialTouchX);
                        int YDiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        if (XDiff < 10 && YDiff < 10) {
                            if (isViewExpanded) {
                                foldLayout();
                            } else {
                                expandLayout();
                            }
                            isViewExpanded = !isViewExpanded;
                        }
                    case MotionEvent.ACTION_MOVE:
                        System.out.println("move called");

                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(floatingView, params);

                        XDiff = (int) (event.getRawX() - initialTouchX);
                        YDiff = (int) (event.getRawY() - initialTouchY);
                        if (XDiff > 10 || YDiff > 10) {
                            moved = true;
                        }
                }

                deltaTime = (System.currentTimeMillis() - startTime);
                if (deltaTime > 500 && !moved) {
                    System.out.println("Called animation");
                    if (!animating) {
                        animating = true;
                        floatingView.findViewById(R.id.imageViewExit).performClick();
                        LinearLayout linearLayoutMain = floatingView.findViewById(R.id.linearLayoutMain);
                        linearLayoutMain.animate().scaleX(0.0f);
                        linearLayoutMain.animate().scaleY(0.0f);
                    }

                    new Thread(() -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopSelf();
                    }).start();

                } else {
                    System.out.println("Not moving, deltaTime: " + deltaTime + " moved: " + moved);
                }
                return false;
            }
        });

        /*btnClose.setOnLongClickListener(
                v -> {
                    stopSelf();
                    return false;
                }
        );*/

    }

    private void foldLayout() {
        LinearLayout linearLayoutMain = floatingView.findViewById(R.id.linearLayoutMain);
        linearLayoutMain.animate().scaleX(0.4f);
        linearLayoutMain.animate().scaleY(0.4f);

        LinearLayout linearLayout1 = floatingView.findViewById(R.id.linearLayout1);
        linearLayout1.animate().alpha(0.0f);
        //linearLayout1.setVisibility(View.GONE);
        LinearLayout linearLayout2 = floatingView.findViewById(R.id.linearLayout2);
        linearLayout2.animate().alpha(0.0f);
        //linearLayout2.setVisibility(View.GONE);
        LinearLayout linearLayoutExit = floatingView.findViewById(R.id.linearLayoutExit);
        linearLayoutExit.animate().scaleX(2.5f);
        linearLayoutExit.animate().scaleY(2.5f);
        //linearLayoutExit.setVisibility(View.GONE);
        LinearLayout linearLayout4 = floatingView.findViewById(R.id.linearLayout4);
        linearLayout4.animate().alpha(0.0f);
        //linearLayout4.setVisibility(View.GONE);
        LinearLayout linearLayout5 = floatingView.findViewById(R.id.linearLayout5);
        linearLayout5.animate().alpha(0.0f);
        //linearLayout5.setVisibility(View.GONE);
    }

    private void expandLayout() {
        LinearLayout linearLayoutMain = floatingView.findViewById(R.id.linearLayoutMain);
        linearLayoutMain.animate().scaleX(1.0f);
        linearLayoutMain.animate().scaleY(1.0f);

        LinearLayout linearLayout1 = floatingView.findViewById(R.id.linearLayout1);
        linearLayout1.animate().alpha(1.0f);
        //linearLayout1.setVisibility(View.VISIBLE);
        LinearLayout linearLayout2 = floatingView.findViewById(R.id.linearLayout2);
        linearLayout2.animate().alpha(1.0f);
        //linearLayout2.setVisibility(View.VISIBLE);
        LinearLayout linearLayoutExit = floatingView.findViewById(R.id.linearLayoutExit);
        linearLayoutExit.animate().scaleX(1.0f);
        linearLayoutExit.animate().scaleY(1.0f);
        //linearLayoutExit.setVisibility(View.VISIBLE);
        LinearLayout linearLayout4 = floatingView.findViewById(R.id.linearLayout4);
        linearLayout4.animate().alpha(1.0f);
        //linearLayout4.setVisibility(View.VISIBLE);
        LinearLayout linearLayout5 = floatingView.findViewById(R.id.linearLayout5);
        linearLayout5.animate().alpha(1.0f);
        //linearLayout5.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) mWindowManager.removeView(floatingView);
    }
}