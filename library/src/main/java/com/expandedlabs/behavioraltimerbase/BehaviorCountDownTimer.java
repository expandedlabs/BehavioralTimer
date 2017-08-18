package com.expandedlabs.behavioraltimerbase;

import android.util.Log;

import com.expandedlabs.behavioraltimerbase.exceptions.IntervalTimerException;
import com.expandedlabs.behavioraltimerbase.exceptions.IterationException;
import com.expandedlabs.behavioraltimerbase.exceptions.LimitedHoldException;
import com.expandedlabs.behavioraltimerbase.exceptions.MinMaxException;
import com.expandedlabs.behavioraltimerbase.exceptions.TotalTimerException;

import java.util.Random;


public abstract class BehaviorCountDownTimer
{
//region MEMBER VARIABLES
    private static final int TICK_INTERVAL = 100;
    static final String TAG = "BehaviorCountDownTimer";

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
     * NO_ADJUSTMENT - No changes to the timer were done
     * INTERVAL_ADJUSTMENT - Adjusted main timer because the interval does not divide
     *                      nicely against the main timer
     * ITERATION_ADJUSTMENT - Random iterations was set but the total number of iterations
     *                      specified was bogus e.g. 0 iterations, 1 iteration or iterations
     *                      that create less than 1 second interval values
     */
    public enum IllFitEnum
    {
        NO_ADJUSTMENT,
        INTERVAL_ADJUSTMENT,
        ITERATION_ADJUSTMENT
    }

    private  IllFitEnum mTimerFitting = IllFitEnum.NO_ADJUSTMENT;

    /**
     * Flag that specifies a reset should be done
     */
    private boolean mReset = false;
    /**
     * Specifies if the timer is running
     */
    private boolean mTimerRunning = false;

    /**
     * Countdown timer instance
     */
    private CustomizedCountdown mCustomizedCountdown;

    /**
     * Flag that signifies this instance has randomized interval values
     */
    private boolean mDefinedRandomFlag = false;
    private RandomStyleEnum mDefinedStyle = RandomStyleEnum.REGULAR;

    /**
     * Holds highest/lowest value a random interval can be
     */
    private long mDefinedMaxRandomValue = 9999;
    private long mDefinedMinRandomValue = 0;

    /**
     * Hold how many iterations for the random intervals
     */
    private int mDefinedRandIterationValue = 1;
    /**
     * Flag that signifies we have a limited hold type interval
     */
    private boolean mDefinedLimitedHold = false;

    /**
     * Holds the definition for the limited hold interval
     */
    private long mDefinedLimitedHoldValue = 0;

    /**
     * Holds the definition for the basic timer elements
     */
    private long mDefinedIntervalValue = 0;
    private long mDefinedTimerValue = 0;

    /**
     * Holds the current value for the timer elements
     */
    private long mCurrentTimerValue = 0;
    private long mCurrentIntervalValue = 0;
    private boolean mCurrentLimitedHold = false;
    private int mCurrentIterationValue = 0;

    /**
     *  This will hold the up coming interval value which will depend if things are randomized
     *  and how the randomized is broken down.
     */
    private long mNextIntervalValue = 0;
    private long mNextValueForAnInterval = 0;

    /**
     * Random generator
     */
    private final Random mRandomGen = new Random();
//endregion

//region CONSTRUCTOR
    public BehaviorCountDownTimer(long timerValue,
                                  long intervalValue,
                                  boolean randomFlag,
                                  RandomStyleEnum style,
                                  long minRandom, long maxRandom,
                                  int numberOfIterations,
                                  boolean limitedHoldFlag, long limitedHold)
            throws IntervalTimerException,
            IterationException,
            LimitedHoldException,
            MinMaxException,
            TotalTimerException
    {
        setTimerValue(timerValue);
        setIntervalValue(intervalValue);
        mNextIntervalValue = intervalValue;

        setTimerRandom(randomFlag, style, minRandom, maxRandom, numberOfIterations);

        setLimitedHold(limitedHoldFlag, limitedHold);

        invalidate();
        reset();
    }
//endregion

//region TIMER ACTIONS
    /**
     * Pause the timer
     */
    public void pause()
    {
        if(mCustomizedCountdown!= null)
        {
            mCustomizedCountdown.cancel();
        }
        mTimerRunning = false;
    }

    /**
     * Resume the timer
     */
    public void start()
    {
        if(mCurrentTimerValue <= 0 || mReset)
        {
            //Check if our timer is over if so, reset it first
            reset();
        }

        createTimer(); //recreate it in this sense
        mCustomizedCountdown.start();
        mTimerRunning = true;
    }

    /**
     * Update current values for defined values and verify the timer members have proper
     * values
     */
    public void reset()
    {
        //Verify times are accurate
        checkTimerFitting();

        mNextIntervalValue = mDefinedIntervalValue;
        mCurrentTimerValue = mDefinedTimerValue;

        calculateNewIntervalValue();
        mNextValueForAnInterval = mDefinedTimerValue - mNextIntervalValue;

        mCurrentLimitedHold = false;
        mTimerRunning = false;
        mCurrentIterationValue = 0;

        mReset = false;
    }
    //endregion

//region SETTERS

    /**
     * Set the timer limited hold features on and off with a given
     * hold value
     * @param holdFlag - Toggle timer capability to do a limited hold
     * @param holdValue - Value in milliseconds to do a limited hold after a regular interval
     */
    public void setLimitedHold(boolean holdFlag, long holdValue) throws LimitedHoldException
    {
        mDefinedLimitedHold = holdFlag;
        if(holdFlag)
        {
            if(holdValue <= 0)
            {
                throw new LimitedHoldException("Limited hold value is set to an invalid number.");
            }
            mDefinedLimitedHoldValue = holdValue;
        }
        else
        {
            mDefinedLimitedHoldValue = 0;
        }

        //The timer values have changed, invalidate
        invalidate();
    }

    /**
     * Sets the timer to do random interval values based on style
     * @param randomFlag True to create random intervals
     * @param style Set a style of randomized intervals
     * @param minRandom Lowest value the random interval will use for manipulation
     * @param maxRandom Highest value the random interval will use for manipulation
     * @param numberOfIterations Used when style is ITERATION and the total iterations the timer
     *                           would run for with random length intervals
     */
    public void setTimerRandom(boolean randomFlag,
                               RandomStyleEnum style,
                               long minRandom, long maxRandom,
                               int numberOfIterations) throws MinMaxException, IterationException
    {

        mDefinedRandomFlag = randomFlag;
        mDefinedStyle = style;

        if(mDefinedRandomFlag && (maxRandom <= 0 ||minRandom <= 0)
                && (style == RandomStyleEnum.REGULAR || style == RandomStyleEnum.DEVIATION))
        {
            throw new MinMaxException("Min/Max random values are invalid.");
        }

        if(mDefinedRandomFlag && numberOfIterations <= 1 && style == RandomStyleEnum.ITERATION)
        {
            throw new IterationException("Iteration value is invalid.");

        }

        if(minRandom > maxRandom)
        {
            //For some reason the incoming values are flipped where the max number wanted
            //is actually smaller than the minimum specified
            mDefinedMinRandomValue = maxRandom;
            mDefinedMaxRandomValue = minRandom;
        }
        else
        {
            mDefinedMinRandomValue = minRandom;
            mDefinedMaxRandomValue = maxRandom;
        }



        mDefinedRandIterationValue = numberOfIterations;

        invalidate();
    }

    /**
     * Set the main timer's duration
     * @param timerValue Milliseconds for the main timer's duration
     */
    public void setTimerValue(long timerValue) throws TotalTimerException
    {
        if(timerValue <= 0)
        {
            throw new TotalTimerException("Invalid timer value specified.");
        }
        mDefinedTimerValue = timerValue;
        invalidate();
    }

    /**
     * Set the timer's interval duration
     * @param intervalValue Milliseconds for timer intervals
     */
    public void setIntervalValue(long intervalValue) throws IntervalTimerException
    {
        if(intervalValue <= 0)
        {
            throw new IntervalTimerException("Interval value specified is invalid.");
        }
        mDefinedIntervalValue = intervalValue;
        invalidate();
    }
//endregion

//region GETTERS
    /**
     * Getter for limited hold flag
     * @return Returns true if the timer is running a limited hold after each interval
     */
    public boolean getLimitedHoldFlag()
    {
        return mDefinedLimitedHold;
    }

    /**
     * Getter for limited hold value
     * @return Returns the limited hold value in milliseconds, zero if limited hold flag is false
     */
    public long getLimitedHoldValue()
    {
        return mDefinedLimitedHoldValue;
    }

    /**
     * Get the defined timer value, this value can be different if it has been form fitted given
     * by mTimerFitting
     * @return Returns the main timer's defined value in milliseconds
     */
    public long getDefinedTimerValue()
    {
        return mDefinedTimerValue;
    }

    /**
     * Get the current timer's value
     * @return Returns the main timer's current value in milliseconds
     */
    public long getCurrentTimerValue()
    {
        return mCurrentTimerValue;
    }

    /**
     * Returns the current interval's value
     * @return Returns the current interval's value in milliseconds
     */
    public long getCurrentIntervalValue()
    {
        return mCurrentIntervalValue;
    }

    /**
     * Returns current iteration value
     * @return Returns the current iteration value
     */
    public int getCurrentIterationValue()
    {
        return mCurrentIterationValue;
    }

    /**
     * Returns the length in milliseconds the next iteration is
     * @return milliseconds length of the next iteration
     */
    public long getNextIntervalValue() { return mNextIntervalValue; }

    /**
     * Returns the type of fitting that was done to the timer
     * NO_ADJUSTMENT, INTERVAL_ADJUSTMENT or ITERATION ADJUSTMENT
     * @return The type of adjustment done to the timer
     */
    public IllFitEnum getTimerFitting() { return mTimerFitting; }

    /**
     * Returns true if the current interval we are running is a limited hold
     * @return True if the current interval session is a limited hold
     */
    public boolean getCurrentLimitedHoldFlag() { return mCurrentLimitedHold; }

    /**
     * Returns true if the timer has random intervals
     * @return True if the timer is doing random intervals
     */
    public boolean getRandomFlag() { return mDefinedRandomFlag; }

//endregion

//region CALLBACKS
    public abstract void onTick();
    public abstract void onFinish();
    public abstract void onIntervalReached();
//endregion

//region PROTECTED
    @SuppressWarnings("WeakerAccess")
    protected void innerTick(long millisUntilFinished)
    {
        mCurrentTimerValue = millisUntilFinished;

        mCurrentIntervalValue = mCurrentTimerValue - mNextValueForAnInterval;
        checkForIntervalChanges();

        onTick();

        if(mReset)
        {
            //The timer was running but the reset flag seemed to have been queued.
            //The user must have changed the timer while it was running.
            mCustomizedCountdown.cancel();
            reset();
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void innerFinish()
    {
        Log.d(TAG, "finished.");
        mTimerRunning = false;

        // Final increment iteration when we aren't doing a limited hold
        if(!mCurrentLimitedHold)
            ++mCurrentIterationValue;

        //Zero values since the timer has finished
        mCurrentTimerValue = 0;
        mCurrentIntervalValue = 0;
        mCurrentLimitedHold = false;

        onFinish();
    }
//endregion

//region PRIVATE HELPER METHODS
    private void checkTimerFitting()
    {
        mTimerFitting = IllFitEnum.NO_ADJUSTMENT;

        //Check to see if we are doing random intervals, if we are timer fitting is set to
        //no adjustments
        if(mDefinedRandomFlag) return;

        //Check to make sure we didn't get an invalid number e.g. less than 0 and/or iteration
        //is not less than 1 second. Maybe this check should be done prior to this timer...?
        if(mDefinedRandIterationValue <= 0 &&
                (mDefinedTimerValue / 1000) / mDefinedRandIterationValue <= 1)
        {
            mDefinedRandIterationValue = 1;
            mTimerFitting = IllFitEnum.ITERATION_ADJUSTMENT;
        }

        long modResult = mDefinedTimerValue % (mDefinedIntervalValue + mDefinedLimitedHoldValue);
        if(modResult != 0)
        {
            //The interval time does not fit perfectly in our timer,
            //this will adjust the main timer to fit an equal set of intervals
            mDefinedTimerValue += mDefinedIntervalValue + mDefinedLimitedHoldValue - modResult;

            mTimerFitting = IllFitEnum.INTERVAL_ADJUSTMENT;
        }
    }

    private void calculateNewIntervalValue()
    {
        //Check if we are doing a limited hold if we are then make sure it's not the very first
        //iteration (e.g. before the timer is even running since a limited hold ALWAYS happens
        //after 1 regular/random iteration.
        if(mDefinedLimitedHold && mTimerRunning )
        {
            if(!mCurrentLimitedHold)
            {
                //We were in a regular interval, do a limited hold next
                mNextIntervalValue = mDefinedLimitedHoldValue;
                mCurrentIntervalValue = mDefinedLimitedHoldValue;
                mCurrentLimitedHold = true;
                Log.d(TAG, "Limited Hold started.");
                return;
            }

            //Since we got here, that means we just did a limited hold and the next
            //interval should be a regular interval
            mCurrentLimitedHold = false;
            mNextIntervalValue = mDefinedIntervalValue;
        }

        //Check if we are doing randomized intervals
        if(mDefinedRandomFlag)
        {
            switch(mDefinedStyle)
            {
                case ITERATION:
                    Log.d(TAG, "Iteration Random");
                    getIterationInterval();
                    break;
                case DEVIATION:
                    Log.d(TAG, "Deviation Random");
                    getDeviationInterval();
                    break;
                case REGULAR:
                    Log.d(TAG, "Regular Random");
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
            Log.d(TAG, "Interval completed.");

            //Increment iteration when we are not doing a limited hold
            if(!mCurrentLimitedHold)
            {
                ++mCurrentIterationValue;
                Log.d(TAG, "Iteration count: " + mCurrentIterationValue);
            }

            //We have iterated, calculate an interval value
            calculateNewIntervalValue();

            //The additional 1000 is to mitigate the fact the interval will NEVER exactly tick
            // at the TICK_RATE. We go back to the last second of that interval and realign
            // the milliseconds so our interval time and our timer time align
            mNextValueForAnInterval = mCurrentTimerValue +
                    (1000 - (mCurrentTimerValue % 1000)) - mNextIntervalValue;

            Log.d(TAG, "Next Interval: " + mNextIntervalValue);

            onIntervalReached();
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
        if(interval >= mCurrentTimerValue)
        {
            interval = mCurrentTimerValue;
        }

        mNextIntervalValue = interval;
    }

    private void getIterationInterval()
    {
        int intervalsLeft = mDefinedRandIterationValue - mCurrentIterationValue;

        if(intervalsLeft == 1)
        {
            //Last interval, simply set it to what we have left in our main timer
            mNextIntervalValue = mCurrentTimerValue;
            return;
        }

        long maxIntervalValue = mCurrentTimerValue / intervalsLeft;
        long minIntervalValue = (mCurrentTimerValue / mDefinedRandIterationValue) / 2;

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

    private void invalidate()
    {
        mReset = true;
    }
    //endregion
}
