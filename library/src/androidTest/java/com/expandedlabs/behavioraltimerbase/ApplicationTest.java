package com.expandedlabs.behavioraltimerbase;

import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.Before;
import org.junit.BeforeClass;
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
    private static String TAG = "BCDT Test";

    private static BehaviorCountDownTimer sTimer;


    @BeforeClass
    public static void createTimer()
    {
        long sTimerValue = 15 * 1000;
        long sIntervalValue = 5 * 1000;
        boolean sRandomFlag = false;
        RandomStyleEnum sStyle = RandomStyleEnum.REGULAR;
        long sMinRandom = 2 * 1000;
        long sMaxRandom = 4 * 1000;
        int sNumberOfIterations = 3;
        boolean sLimitedHoldFlag = false;
        long sLimitedHold = 5 * 1000;

        //Create timer
        sTimer = new BehaviorCountDownTimer(sTimerValue, sIntervalValue,
                sRandomFlag, sStyle, sMinRandom, sMaxRandom, sNumberOfIterations,
                sLimitedHoldFlag, sLimitedHold)
        {
            @Override
            public void onTick()
            {
                Log.v(TAG, "Interval: " + getCurrentTimerValue());
            }

            @Override
            public void onFinish()
            {
                Log.v(TAG, "Timer finished.");
            }
        };
    }

    private void runTimer() throws InterruptedException
    {
        //Create runnable since it has to sync up with the main thread
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                sTimer.reset();
                sTimer.start();
            }

        };

        //Start timer and wait for the timer value + 1 second
        getInstrumentation().waitForIdle(runnable);
        Thread.sleep(sTimer.getDefinedTimerValue() + 1000);
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
        Log.v(TAG, "timer_isCorrect begin.");

        //Initialize parameters
        sTimer.setTimerValue(5 * 1000);
        sTimer.setIntervalValue(1 * 1000);
        sTimer.setTimerRandom(false,
                RandomStyleEnum.REGULAR,
                5 * 1000,
                10 * 1000,
                3);
        sTimer.setLimitedHold(false, 10*1000);

        //Initialize expected
        long expectedTimerValue = 0;
        long expectedIntervalValue = 0;
        int expectedNumberOfIterations = 5;

        runTimer();

        //Verify end values are correct
        assertEquals(expectedTimerValue, sTimer.getCurrentTimerValue());
        assertEquals(expectedIntervalValue, sTimer.getCurrentIntervalValue());
        assertEquals(expectedNumberOfIterations, sTimer.getCurrentIterationValue());

        Log.v(TAG, "timer_isCorrect finished.");
    }

    @Test
    public void limitedHold_isCorrect() throws InterruptedException
    {
        Log.v(TAG, "limitedHold_isCorrect begin.");

        //Initialize parameters
        sTimer.setTimerValue(30 * 1000);
        sTimer.setIntervalValue(5 * 1000);
        sTimer.setTimerRandom(false,
                RandomStyleEnum.REGULAR,
                5 * 1000,
                10 * 1000,
                3);
        sTimer.setLimitedHold(true, 10*1000);


        //Initialize expected
        int expectedNumberOfIterations = 2;

        runTimer();

        assertEquals(expectedNumberOfIterations, sTimer.getCurrentIterationValue());

        Log.v(TAG, "limitedHold_isCorrect finished.");

    }
}