package com.expandedlabs.behavioraltimerbase;

import android.util.Log;

import java.util.Random;


public abstract class BehaviorCountDownTimer
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
    int mCurrentIterationValue = 0;

    /**
     *  This will hold the up coming interval value which will depend if things are randomized
     *  and how the randomized is broken down.
     */
    long mNextIntervalValue = 0;
    long mNextValueForAnInterval = 0;

    /**
     * Random generator
     */
    Random mRandomGen = new Random();

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
        mNextIntervalValue = intervalValue;

        mDefinedRandomFlag = randomFlag;
        mDefinedStyle = style;

        mDefinedMinRandomValue = minRandom;
        mDefinedMaxRandomValue = maxRandom;

        mDefinedRandIterationValue = numberOfIterations;

        //Check to make sure we didn't get an invalid number e.g. less than 0 and/or iteration
        //is not less than 1 second. Maybe this check should be done prior to this timer...?
        if(mDefinedRandIterationValue <= 0 &&
            (mDefinedTimerValue / 1000) / mDefinedRandIterationValue <= 1)
        {
            mDefinedRandIterationValue = 1;
        }

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
    public void start()
    {
        if(mCurrentTimerValue <= 0)
        {
            //Check if our timer is over if so, reset it first
            reset();
        }

        createTimer(); //recreate it in this sense
        mCustomizedCountdown.start();
        mTimerRunning = true;
    }

    public void reset()
    {
        mNextIntervalValue = mDefinedIntervalValue;

        calculateNewIntervalValue();
        mNextValueForAnInterval = mDefinedTimerValue - mNextIntervalValue;

        mCurrentTimerValue = mDefinedTimerValue;
        mCurrentLimitedHold = false;
        mCurrentIterationValue = 0;
    }


    /**
     * Callbacks for the timer's onTick and onFinish
     */
    public abstract void onTick();
    public abstract void onFinish();

    /********************************************
     * PROTECTED
     ********************************************/

    protected void innerTick(long millisUntilFinished)
    {
        mCurrentTimerValue = millisUntilFinished;

        mCurrentIntervalValue = mCurrentTimerValue - mNextValueForAnInterval;
        checkForIntervalChanges();

        onTick();
    }

    protected void innerFinish()
    {
        mTimerRunning = false;

        //Zero values since the timer has finished
        mCurrentTimerValue = 0;
        mCurrentIntervalValue = 0;
        mCurrentLimitedHold = false;

        onFinish();
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
                    getIterationInterval();
                    break;
                case DEVIATION:
                    getDeviationInterval();
                    break;
                case REGULAR:
                default:
                    getRegularRandomInterval();

            }
        }

        mCurrentIntervalValue = mNextIntervalValue;
    }

    /**
     * Create instance of the android countdown timer
     */
    private void createTimer()
    {
        mCustomizedCountdown = new CustomizedCountdown(mCurrentTimerValue, TICK_INTERVAL, this);
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

            mNextValueForAnInterval = mCurrentTimerValue - mNextIntervalValue;

            //Increment iteration
            ++mCurrentIterationValue;
        }
    }

    private void getRegularRandomInterval()
    {
       long interval = mDefinedMinRandomValue
                + (long) ((mRandomGen.nextDouble())
               * ((mDefinedMaxRandomValue - mDefinedMinRandomValue) + 1));

        interval = interval - (interval % 1000); // go to nearest second

        //Check if our interval is larger than our current timer, if it is just set the interval
        //to the current timer
        if(interval <= mCurrentTimerValue)
        {
            interval = mCurrentTimerValue;
        }

        mNextIntervalValue = interval;
    }

    private void getIterationInterval()
    {
        int intervalsLeft = mDefinedRandIterationValue - mCurrentIterationValue - 1;

        if(intervalsLeft == 0)
        {
            //Last interval, simply set it to what we have left in our main timer
            mNextIntervalValue = mCurrentTimerValue;
            return;
        }

        long maxIntervalValue = mCurrentTimerValue / intervalsLeft;
        long minIntervalValue = (long)((mCurrentTimerValue / mDefinedRandIterationValue) * 0.5);

        //Create random interval
        long interval = minIntervalValue
                + (long) ((mRandomGen.nextDouble())
                * ((maxIntervalValue - minIntervalValue) + 1));

        interval = interval - (interval % 1000); // go to nearest second

        if(interval <= 0)
        {
            // This is extra precaution in case we generated an interval less than a second
            // might not be needed...
            interval = mCurrentTimerValue;
        }

        mNextIntervalValue = interval;
    }

    private void getDeviationInterval()
    {
        //Get a random interval based on the given min/max intervals
        getRegularRandomInterval();

        //Choose whether to decrement/increment the defined interval value with the min/max
        // intervals
        if(mRandomGen.nextBoolean())
        {
            mNextIntervalValue += mDefinedIntervalValue;
        }
        else
        {
            mNextIntervalValue = Math.abs(mDefinedIntervalValue - mNextIntervalValue);
        }

    }
}
