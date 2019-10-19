# BehavioralTimer 
[![Build Status](https://travis-ci.org/expandedlabs/BehavioralTimer.svg?branch=master)](https://travis-ci.org/expandedlabs/BehavioralTimer) 

Timer based off of Android CountDownTimer but tailored to behavioral analysis.
This is an android library (module).

## Unique Features ##

#### Limited Hold ####
A limited hold is conducted after every given interval. In behavior analysis, this is can be a short window to reinforce a behavior.
Example: Given a 5 minute interval with a 30 second limited hold. After a 5 minute interval, the individual has 30 seconds to exhibit the target behavior to get reinforcement. 

#### Random Styles ####
* **REGULAR** - Regular random intervals in the range of `minRandom` to `maxRandom`.

* **ITERATION** - A total number of iterations given by `numberOfIterations` with completely random intervals.
<br/>e.g. if the timer is for 30 sec and we want 3 iterations, the timer can be
itervals 3, 16, 11 or 10, 15, 5 etc. as long as the iterations are 3.
           
* **DEVIATION** - The `minRandom` and `maxRandom` will be used as deviations from the `intervalValue` specified.
<br/>e.g. If `intervalValue` is 60 seconds, `minRandom` is 3, and `maxRandom` is 10, then it will randomize
values such as 63, 57, 70, 50, 68, etc. but not 62, 61, 71, 72, etc.
<br/>The math here is random interval between `minRandom` to `maxRandom` +/- `intervalValue`. Or using our example, 3 to 10 and +/- 60.

## Install ##
To add this library to an existing android project, edit your settings.gradle and add these lines

  ``` gradle
    include 'BehavioralTimerBase'
    project (':BehavioralTimerBase').projectDir = new File('PATH/TO/LIBRARY')
   ```
  
e.g.

  ```gradle
    include 'BehavioralTimerBase'
    project (':BehavioralTimerBase').projectDir = new File('../BehavioralTimerBase/library')
  ```
  
  Then edit your application's build.gradle add this line as a dependency
  
  ```gradle
  dependencies {
  compile project (':BehavioralTimerBase')
  }
  ```
  
## Usage ##
  ```java
BehaviorCountDownTimer timer = new BehaviorCountDownTimer(long timerValue,
                                  long intervalValue,
                                  boolean randomFlag,
                                  RandomStyleEnum style,
                                  long minRandom, long maxRandom,
                                  int numberOfIterations,
                                  boolean limitedHoldFlag, long limitedHold)
  ```
  
| Parameter | Description |
| ---: | :--- |
| timerValue         | The full duration of the timer. |
| intervalValue      | When `randomFlag` is false, this is the length specified in milliseconds to call `onIntervalReached()`. |
| randomFlag         | Specifies if the intervalValues will be randomized. |      
| style              | The random style -- Regular, Iteration or Deviation. |
| minRandom          | For Regular and Deviation random styles. Lowest interval value used. |
| maxRandom          | For Regular and Deviation random styles. Highest interval value used. | 
| numberOfIterations | For  random Iteration style. The number of random interval iterations given the max `timerValue`.|
| limitedHoldFlag    | Specifies if a limited hold interval will be conducted after every interval. |
| limitedHold        | Duration of the limited hold interval. |

#### Callbacks ####
There are callback functions in which you can add any commands you want to execute either after every tick (100ms), every interval or when the timer is finished.
```java
    public abstract void onTick();
    public abstract void onFinish();
    public abstract void onIntervalReached();
```

## TODO ##
* Add to gradle portal https://plugins.gradle.org/docs/submit
* Add more testing
  * Test limited hold with each case of random
  * Test deformed inputs (e.g. negative numbers, 0s, flipped min/max random, really high iterations)
  
