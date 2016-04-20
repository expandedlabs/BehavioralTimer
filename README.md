# BehavioralTimer

Timer based off of Android CountDownTimer but tailored to behavioral analysis.
This is an android library (module).

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
  TODO
  * Add to gradle portal https://plugins.gradle.org/docs/submit
  * Add more testing
  * * Test limited hold with each case of random
  * * Test each case of random
  * * Test deformed inputs (e.g. negative numbers, 0s, flipped min/max random, really high iterations)
  
