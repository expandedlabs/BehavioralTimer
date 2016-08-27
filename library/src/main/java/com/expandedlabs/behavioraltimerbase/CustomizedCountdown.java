package com.expandedlabs.behavioraltimerbase;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * This is the actual android countdown timer the behavioral timer uses. We didn't extend
 * to this class simply because the basic timer has no pause functionality. The only way
 * to stop it is by calling cancel() and when that's done, there's no way to update the
 * millis in future without having to call new.
 */
@SuppressWarnings("WeakerAccess")
public class CustomizedCountdown extends CountDownTimer
{
    private static final String TAG = "BCDT.CustomizedCountdown";
    private final BehaviorCountDownTimer mBTimer;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public CustomizedCountdown(long millisInFuture, long countDownInterval, BehaviorCountDownTimer bTimer)
    {
        super(millisInFuture, countDownInterval);
        mBTimer = bTimer;
    }

    @Override
    public void onTick(long millisUntilFinished)
    {
        mBTimer.innerTick(millisUntilFinished);
    }

    @Override
    public void onFinish()
    {
        Log.d(TAG, "finished.");
        mBTimer.innerFinish();
    }
}