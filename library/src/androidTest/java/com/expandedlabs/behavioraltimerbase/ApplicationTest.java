package com.expandedlabs.behavioraltimerbase;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import static com.expandedlabs.behavioraltimerbase.BehaviorCountDownTimer.RandomStyleEnum;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ApplicationTest
{
    private static String TAG = "BCDT Test";

    private BehaviorCountDownTimer mTimer;

    @Rule
    public TestName mTestName = new TestName();

    @Before
    public void TestSetUp()
    {
        Log.d(TAG, "-----------------------------------");
        Log.d(TAG, mTestName.getMethodName() + " begin.");
        Log.d(TAG, "-----------------------------------");

    }

    @After
    public void TestTearDown()
    {
        Log.d(TAG, "-----------------------------------");
        Log.d(TAG, mTestName.getMethodName() + " end.");
        Log.d(TAG, "-----------------------------------");

    }

    public void createDefaultTimer()
    {
        long timerValue = 15 * 1000;
        long intervalValue = 5 * 1000;
        boolean randomFlag = false;
        RandomStyleEnum style = RandomStyleEnum.REGULAR;
        long minRandom = 2 * 1000;
        long maxRandom = 4 * 1000;
        int numberOfIterations = 3;
        boolean limitedHoldFlag = false;
        long limitedHold = 5 * 1000;

        //Create timer
        try
        {
            mTimer = new BehaviorCountDownTimer(timerValue, intervalValue,
                    randomFlag, style, minRandom, maxRandom, numberOfIterations,
                    limitedHoldFlag, limitedHold)
            {
                @Override
                public void onTick()
                {
                    Log.d(TAG, "Interval: " + getCurrentTimerValue());
                }

                @Override
                public void onFinish()
                {
                    Log.d(TAG, "Timer finished.");
                }

                @Override
                public void onIntervalReached()
                {
                    Log.d(TAG, "New Interval: " + getCurrentIntervalValue());
                }
            };
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void runTimer() throws InterruptedException
    {
        //Create runnable since it has to sync up with the main thread
        mTimer.reset();
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                mTimer.start();
            }

        };

        Log.d(TAG, "Timer Start: " + mTimer.getDefinedTimerValue() + " Interval: " + mTimer.getCurrentIntervalValue());

        //Start timer and wait for the timer value + 1 second
        getInstrumentation().waitForIdle(runnable);
        Thread.sleep(mTimer.getDefinedTimerValue() + 1000);
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
        createDefaultTimer();
        try
        {
            mTimer.setTimerValue(5 * 1000);
            mTimer.setIntervalValue(1 * 1000);
            mTimer.setTimerRandom(false,
                    RandomStyleEnum.REGULAR,
                    5 * 1000,
                    10 * 1000,
                    3);
            mTimer.setLimitedHold(false, 10*1000);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        //Initialize expected
        long expectedTimerValue = 0;
        long expectedIntervalValue = 0;
        int expectedNumberOfIterations = 5;

        runTimer();

        //Verify end values are correct
        assertEquals(expectedTimerValue, mTimer.getCurrentTimerValue());
        assertEquals(expectedIntervalValue, mTimer.getCurrentIntervalValue());
        assertEquals(expectedNumberOfIterations, mTimer.getCurrentIterationValue());
    }

    @Test
    public void limitedHold_isCorrect() throws InterruptedException
    {
        //Initialize parameters
        createDefaultTimer();
        try
        {
            mTimer.setTimerValue(30 * 1000);
            mTimer.setIntervalValue(5 * 1000);
            mTimer.setTimerRandom(false,
                    RandomStyleEnum.REGULAR,
                    5 * 1000,
                    10 * 1000,
                    3);
            mTimer.setLimitedHold(true, 10*1000);

        } catch (Exception e)
        {
            e.printStackTrace();
        }


        //Initialize expected
        int expectedNumberOfIterations = 2;

        runTimer();

        assertEquals(expectedNumberOfIterations, mTimer.getCurrentIterationValue());
    }

    @Test
    public void intervalImperfectFit_isCorrect() throws InterruptedException
    {
        //Initialize parameters
        createDefaultTimer();
        try
        {
            mTimer.setTimerValue(7 * 1000);
            mTimer.setIntervalValue(3 * 1000);
            mTimer.setTimerRandom(false,
                    RandomStyleEnum.REGULAR,
                    5 * 1000,
                    10 * 1000,
                    3);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            mTimer.setLimitedHold(true, 2*1000);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        mTimer.reset();

        //Initialize expected
        BehaviorCountDownTimer.IllFitEnum expectedFit = BehaviorCountDownTimer.IllFitEnum.INTERVAL_ADJUSTMENT;
        long expectedDefinedTimerValue = 10 * 1000;
        int expectedNumberOfIterations = 2;

        assertThat(expectedDefinedTimerValue, is(mTimer.getDefinedTimerValue()));
        assertThat(expectedFit, is(mTimer.getTimerFitting()));

        runTimer();

        assertThat(expectedNumberOfIterations, is(mTimer.getCurrentIterationValue()));
    }

    @Test
    public void randomRegular_isCorrect() throws InterruptedException
    {
        long timerValue = 10 * 1000;
        long intervalValue = 1 * 1000;
        boolean randomFlag = true;
        RandomStyleEnum style = RandomStyleEnum.REGULAR;
        final long minRandom = 2 * 1000;
        final long maxRandom = 5 * 1000;
        int numberOfIterations = 3;
        boolean limitedHoldFlag = false;
        long limitedHold = 5 * 1000;

        //Create timer
        try
        {
            mTimer = new BehaviorCountDownTimer(timerValue, intervalValue,
                    randomFlag, style, minRandom, maxRandom, numberOfIterations,
                    limitedHoldFlag, limitedHold)
            {
                @Override
                public void onTick()
                {
                    Log.d(TAG, "Interval: " + getCurrentTimerValue());
                }

                @Override
                public void onFinish()
                {
                    Log.d(TAG, "finished.");

                }

                @Override
                public void onIntervalReached()
                {
                  assertThat(maxRandom, is(greaterThanOrEqualTo(getNextIntervalValue())));
                    if(minRandom <= mTimer.getCurrentTimerValue())
                        assertThat(minRandom, is(lessThanOrEqualTo(getNextIntervalValue())));
                    Log.d(TAG, "New Interval:" + getNextIntervalValue() + " Timer Value: " + getCurrentTimerValue());

                }
            };
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        runTimer();

    }

    @Test
    public void randomRegLimited_isCorrect() throws InterruptedException
    {
        long timerValue = 10 * 1000;
        long intervalValue = 1 * 1000;
        boolean randomFlag = true;
        RandomStyleEnum style = RandomStyleEnum.REGULAR;
        final long minRandom = 2 * 1000;
        final long maxRandom = 5 * 1000;
        int numberOfIterations = 3;
        boolean limitedHoldFlag = true;
        long limitedHold = 5 * 1000;

        //Create timer
        try
        {
            mTimer = new BehaviorCountDownTimer(timerValue, intervalValue,
                    randomFlag, style, minRandom, maxRandom, numberOfIterations,
                    limitedHoldFlag, limitedHold)
            {
                @Override
                public void onTick()
                {
                    Log.d(TAG, "Interval: " + getCurrentTimerValue());
                }

                @Override
                public void onFinish()
                {
                    Log.d(TAG, "finished.");

                }

                @Override
                public void onIntervalReached()
                {
                    if(!!mTimer.getCurrentLimitedHoldFlag())
                    {
                        assertThat(maxRandom, is(greaterThanOrEqualTo(getNextIntervalValue())));
                        if(minRandom <= mTimer.getCurrentTimerValue())
                            assertThat(minRandom, is(lessThanOrEqualTo(getNextIntervalValue())));
                    }
                    Log.d(TAG, "New Interval:" + getNextIntervalValue() + " Timer Value: " + getCurrentTimerValue());
                }
            };
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        runTimer();

    }

    @Test
    public void randomDeviation_isCorrect() throws InterruptedException
    {
        long timerValue = 20 * 1000;
        final long intervalValue = 5 * 1000;
        boolean randomFlag = true;
        RandomStyleEnum style = RandomStyleEnum.DEVIATION;
        final long minRandom = 2 * 1000;
        final long maxRandom = 4 * 1000;
        int numberOfIterations = 3;
        boolean limitedHoldFlag = false;
        long limitedHold = 5 * 1000;

        //Create timer
        try
        {
            mTimer = new BehaviorCountDownTimer(timerValue, intervalValue,
                    randomFlag, style, minRandom, maxRandom, numberOfIterations,
                    limitedHoldFlag, limitedHold)
            {
                @Override
                public void onTick()
                {
                    Log.d(TAG, "Interval: " + getCurrentTimerValue());
                }

                @Override
                public void onFinish()
                {
                    Log.d(TAG, "finished.");

                }

                @Override
                public void onIntervalReached()
                {
                    assertThat(maxRandom + intervalValue, is(greaterThanOrEqualTo(getNextIntervalValue())));
                    if(minRandom + intervalValue <= mTimer.getCurrentTimerValue())
                        assertThat(minRandom, is(lessThanOrEqualTo(getNextIntervalValue())));
                    Log.d(TAG, "New Interval:" + getNextIntervalValue() + " Timer Value: " + getCurrentTimerValue());

                }
            };
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        runTimer();

    }

    @Test
    public void randomDevLimited_isCorrect() throws InterruptedException
    {
        long timerValue = 20 * 1000;
        final long intervalValue = 5 * 1000;
        boolean randomFlag = true;
        RandomStyleEnum style = RandomStyleEnum.DEVIATION;
        final long minRandom = 2 * 1000;
        final long maxRandom = 4 * 1000;
        int numberOfIterations = 3;
        boolean limitedHoldFlag = true;
        long limitedHold = 5 * 1000;

        //Create timer
        try
        {
            mTimer = new BehaviorCountDownTimer(timerValue, intervalValue,
                    randomFlag, style, minRandom, maxRandom, numberOfIterations,
                    limitedHoldFlag, limitedHold)
            {
                @Override
                public void onTick()
                {
                    Log.d(TAG, "Interval: " + getCurrentTimerValue());
                }

                @Override
                public void onFinish()
                {
                    Log.d(TAG, "finished.");

                }

                @Override
                public void onIntervalReached()
                {
                    if(!mTimer.getCurrentLimitedHoldFlag())
                    {
                        assertThat(maxRandom + intervalValue, is(greaterThanOrEqualTo(getNextIntervalValue())));
                        if(minRandom + intervalValue <= mTimer.getCurrentTimerValue())
                            assertThat(minRandom, is(lessThanOrEqualTo(getNextIntervalValue())));
                        Log.d(TAG, "New Interval:" + getNextIntervalValue() + " Timer Value: " + getCurrentTimerValue());
                    }

                }
            };
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        runTimer();

    }

    @Test
    public void randomIteration_isCorrect() throws InterruptedException
    {
        //Initialize expected
        long expectedTimerValue = 0;
        long expectedIntervalValue = 0;
        int expectedNumberOfIterations = 5;

        //Initialize parameters
        createDefaultTimer();

        try
        {
            mTimer.setTimerValue(20 * 1000);
            mTimer.setTimerRandom(true,
                    RandomStyleEnum.ITERATION,
                    1 * 1000,
                    1 * 1000,
                    5);
        } catch (Exception e)
        {
            e.printStackTrace();
        }


        runTimer();

        //Verify end values are correct
        assertEquals(expectedTimerValue, mTimer.getCurrentTimerValue());
        assertEquals(expectedIntervalValue, mTimer.getCurrentIntervalValue());
        assertEquals(expectedNumberOfIterations, mTimer.getCurrentIterationValue());

    }

    @Test
    public void randomIterLimited_isCorrect() throws InterruptedException
    {
        //Initialize expected
        long expectedTimerValue = 0;
        long expectedIntervalValue = 0;
        int expectedNumberOfIterations = 5;

        //Initialize parameters
        createDefaultTimer();

        try
        {
            mTimer.setTimerValue(20 * 1000);
            mTimer.setTimerRandom(true,
                    RandomStyleEnum.ITERATION,
                    1 * 1000,
                    1 * 1000,
                    5);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            mTimer.setLimitedHold(true, 2 * 1000);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        runTimer();

        //Verify end values are correct
        assertEquals(expectedTimerValue, mTimer.getCurrentTimerValue());
        assertEquals(expectedIntervalValue, mTimer.getCurrentIntervalValue());
        assertEquals(expectedNumberOfIterations, mTimer.getCurrentIterationValue());

    }

    @Test
    public void oddCases() throws InterruptedException
    {
        //Initialize expected
        createDefaultTimer();

        try
        {
            mTimer.setTimerValue(0);
            fail("No exception was thrown. Exception for 0 timer was expected.");
        } catch (Exception e)
        {
            Log.d(TAG, "Exception was thrown as expected.");
        }

        try
        {
            mTimer.setTimerValue(-5000);
            fail("No exception was thrown. Exception for negative timer value was expected.");

        } catch (Exception e)
        {
            Log.d(TAG, "Exception was thrown as expected.");
        }

        try
        {
            mTimer.setLimitedHold(true, -5000);
            fail("No exception was thrown. Exception for negative limited hold value was expected.");
        } catch (Exception e)
        {
            Log.d(TAG, "Exception was thrown as expected.");
        }

        try
        {
            mTimer.setIntervalValue(-1000);
            fail("No exception was thrown. Exception for interval value was expected.");

        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception was thrown as expected.");

        }

        try
        {
            mTimer.setTimerRandom(true, RandomStyleEnum.REGULAR, -1000, -3000, 5);
            fail("No exception was thrown. Exception for interval value was expected.");

        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception was thrown as expected.");

        }

        try
        {
            mTimer.setTimerRandom(true, RandomStyleEnum.ITERATION, 1000, 3000, -5);
            fail("No exception was thrown. Exception for interval value was expected.");

        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception was thrown as expected.");

        }


    }

}