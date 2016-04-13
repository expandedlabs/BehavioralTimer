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
  
