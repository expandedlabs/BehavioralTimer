package com.expandedlabs.behavioraltimerbase;

import android.os.CountDownTimer;

import java.util.Random;

/**
 * Created by darkamikaze on 4/12/2016.
 */
public class BehaviorCountDownTimer
{
    static final int TICK_INTERVAL = 100;

    /**
     * REGULAR - regular random pulled from defined min/max random values
     * ITERATION - random intervals that give a max iteration count.
     *             e.g. if the timer is for 30 sec and we want 3 iterations, the timer can be
     *             itervals 3, 16, 11 or 10, 15, 5 etc. as long as the iterations are 3.
     * DEVIATION - The min/max (mDefinedMaxRandomValue) random values become deviations of the
     *             defined interval value (mDefinedIntervalValue)
     */
    public enum RandomStyleEnum
    {
        REGULAR,
        ITERATION,
        DEVIATION
    }

    /**
     * Countdown timer instance
     */
    CustomizedCountdown mCustomizedCountdown;

    /**
     * Specifies if the timer is running
     */
    boolean mTimerRunning = false;

    /**
     * Flag that signifies this instance has randomized interval values
     */
    boolean mDefinedRandomFlag = false;
    RandomStyleEnum mDefinedStyle = RandomStyleEnum.REGULAR;

    /**
     * Holds highest/lowest value a random interval can be
     */
    long mDefinedMaxRandomValue = 9999;
    long mDefinedMinRandomValue = 0;

    /**
     * Hold how many iterations for the random intervals
     */
    int mDefinedRandIterationValue = 0;
    /**
     * Flag that signifies we have a limited hold type interval
     */
    boolean mDefinedLimitedHold = false;

    /**
     * Holds the definition for the limited hold interval
     */
    long mDefinedLimitedHoldValue = 0;

    /**
     * Holds the definition for the basic timer elements
     */
    long mDefinedIntervalValue = 0;
    long mDefinedTimerValue = 0;

    /**
     * Holds the current value for the timer elements
     */
    long mCurrentTimerValue = 0;
    long mCurrentIntervalValue = 0;
    boolean mCurrentLimitedHold = false;

    public BehaviorCountDownTimer(long timerValue,
                                  long intervalValue,
                                  boolean randomFlag,
                                  RandomStyleEnum style,
                                  long minRandom, long maxRandom,
                                  int numberOfIterations,
                                  boolean limitedHoldFlag, long limitedHold)
    {
        mDefinedTimerValue = timerValue;
        mDefinedIntervalValue = intervalValue;

        mDefinedRandomFlag = randomFlag;
        mDefinedStyle = style;

        mDefinedMinRandomValue = minRandom;
        mDefinedMaxRandomValue = maxRandom;

        mDefinedRandIterationValue = numberOfIterations;

        mDefinedLimitedHold = limitedHoldFlag;
        mDefinedLimitedHoldValue = limitedHold;

        reset();
    }

    /**
     * Pause the timer
     */
    public void pause()
    {
        mCustomizedCountdown.cancel();
        mTimerRunning = false;
    }

    /**
     * Resume the timer
     */
    public void resume()
    {
        createTimer(); //recreate it in this sense
        mCustomizedCountdown.start();
        mTimerRunning = true;
    }

    public void reset()
    {
        calculateNewIntervalValue();

        mCurrentIntervalValue = mDefinedIntervalValue;
        mCurrentTimerValue = mDefinedTimerValue;
        mCurrentLimitedHold = false;

        createTimer();
    }


    /********************************************
     * PRIVATE
     ********************************************/
    private void calculateNewIntervalValue()
    {
        //Check if we are doing a limited hold, if we are update the current interval and
        //continue
        if(mDefinedLimitedHold)
        {
            if(!mCurrentLimitedHold)
            {
                //We were in a regular interval, do a limited hold next
                mCurrentIntervalValue = mDefinedLimitedHoldValue;
                return;
            }

            //Toggle the limited hold flag
            mCurrentLimitedHold = !mCurrentLimitedHold;
        }

        //Check if we are doing randomized intervals
        if(mDefinedRandomFlag)
        {
            switch(mDefinedStyle)
            {
                case ITERATION:
                    //TODO
                    break;
                case DEVIATION:
                    //TODO
                    break;

                case REGULAR:
                    getRegularRandomInterval();
                default:
            }
        }

        mCurrentIntervalValue = mDefinedIntervalValue;

    }

    /**
     * Create instance of the android countdown timer
     */
    private void createTimer()
    {
        mCustomizedCountdown = new CustomizedCountdown(mCurrentTimerValue, TICK_INTERVAL);
    }

    /**
     * Check if we have iterated
     */
    private void checkForIntervalChanges()
    {
        if(mCurrentIntervalValue <= 0)
        {
            //We have iterated, calculate an interval value
            calculateNewIntervalValue();
        }
    }

    private void getRegularRandomInterval()
    {
        Random rand = new Random();

        long interval = mDefinedMinRandomValue
                + (long) ((rand.nextDouble()) * ((mDefinedMaxRandomValue - mDefinedMinRandomValue) + 1));

        interval = interval - (interval % 1000); // go to nearest second

        //Check if our interval is larger than our current timer, if it is just set the interval
        //to the current timer
        if(interval <= mCurrentTimerValue)
        {
            interval = mCurrentTimerValue;
        }

        mDefinedIntervalValue = interval;
    }

    /**
     * This is the actual android countdown timer the behavioral timer uses. We didn't extend
     * to this class simply because the basic timer has no pause functionality. The only way
     * to stop it is by calling cancel() and when that's done, there's no way to update the
     * millis in future without having to call new.
     */
    private class CustomizedCountdown extends CountDownTimer
    {
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CustomizedCountdown(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished)
        {
            mCurrentTimerValue = millisUntilFinished;

            mCurrentIntervalValue = mCurrentTimerValue - (mDefinedTimerValue - mDefinedIntervalValue);
            checkForIntervalChanges();

        }

        @Override
        public void onFinish()
        {

        }
    }
}
