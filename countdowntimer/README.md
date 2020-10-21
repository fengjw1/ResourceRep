# CountDownTimer
This is a custom countdowntimer API, you can use it to realize a countdown.
This API depend on android.os.CountDownTimer

How to use:
# dependencies
implementation 'com.pipishrimp.lib:countdowntimer:1.0.0'
# for example
CountDownTimerManager.startTimer(object : ITimer {
            override fun callback() {
                Log.d("MainActivity", "callback")
                finish()
                CountDownTimerManager.cancel()
            }
        }, tv_timer)

There have four params:
1.ITimer: callback for result
2.Textview ： can be null
3.CountDownTime: total count down time, can be null
4.Interval: countdown time interval, can be null

