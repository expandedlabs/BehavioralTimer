package com.expandedlabs.behavioraltimerbase;

import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.expandedlabs.behavioraltimerbase.BehaviorCountDownTimer.RandomStyleEnum;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ApplicationTest
{
    private String TAG = "BCDT Test";


    private void timerTestHelper(final long timerValue,
                                 final long intervalValue,
                                 boolean randomFlag,
                                 RandomStyleEnum style,
                                 long minRandom, long maxRandom,
                                 int numberOfIterations,
                                 boolean limitedHoldFlag, long limitedHold,
                                 //Expected
                                 final long expectedTimerValue,
                                 final long expectedIntervalValue,
                                 final int expectedNumberOfIterations) throws InterruptedException
    {


        //Create timer
        final BehaviorCountDownTimer timer = new BehaviorCountDownTimer(timerValue, intervalValue,
                randomFlag, style, minRandom, maxRandom, numberOfIterations,
                limitedHoldFlag, limitedHold)
        {
            @Override
            public void onTick()
            {
                Log.v(TAG, "Interval: " + mCurrentTimerValue);
            }

            @Override
            public void onFinish()
            {
                Log.v(TAG, "Timer finished.");
            }
        };

        //Create runnable since it has to sync up with the main thread
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                timer.start();
            }

        };

        //Start timer and wait for the timer value + 1 second
        getInstrumentation().waitForIdle(runnable);
        Thread.sleep(timerValue + 1000);

        //Verify end values are correct
        assertEquals(expectedTimerValue, timer.mCurrentTimerValue);
        assertEquals(expectedIntervalValue, timer.mCurrentIntervalValue);
        assertEquals(expectedNumberOfIterations, timer.mCurrentIterationValue);
    }

    /**
     * Basic behavioral timer
     *  - Checks proper behavior with valid input
     *  - No randomization
     * @throws InterruptedException
     */
    @Test
    public void timer_isCorrect() throws InterruptedException
    {
        //Initialize parameters
        final long timerValue = 5 * 1000;
        long intervalValue = 1 * 1000;
        boolean randomFlag = false;
        RandomStyleEnum style = RandomStyleEnum.REGULAR;
        long minRandom = 5 * 1000;
        long maxRandom = 10 * 1000;
        final int numberOfIterations = 3;
        boolean limitedHoldFlag = false;
        long limitedHold = 10 * 1000;

        //Initialize expected
        long expectedTimerValue = 0;
        long expectedIntervalValue = 0;
        int expectedNumberOfIterations = 4;

        timerTestHelper(timerValue, intervalValue,
                randomFlag, style, minRandom, maxRandom, numberOfIterations,
                limitedHoldFlag, limitedHold,
                //Expected
                expectedTimerValue, expectedIntervalValue,
                expectedNumberOfIterations);

        Log.v(TAG, "timer_isCorrect finished.");


    }
}