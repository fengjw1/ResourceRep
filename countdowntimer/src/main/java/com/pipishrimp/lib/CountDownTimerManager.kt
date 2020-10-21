package com.pipishrimp.lib

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.TextView

object CountDownTimerManager {

    /**
     * 默认的倒计时时长:60s
     */
    private const val MILLIS_IN_FUTURE: Long = 60*1000

    /**
     * 默认的计数间隔时长：1s
     */
    private const val COUNTDOWN_INTERVAL: Long = 1000

    private var countDownTimer: CountDownTimer? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 开始计数
     */
    fun startTimer(iTimer: ITimer,
                   textView: TextView? = null,
                   millisInFuture: Long = MILLIS_IN_FUTURE,
                   countDownInterval: Long = COUNTDOWN_INTERVAL){
        //取消之前存在的计数器
        cancel()

        val mMillisInFuture = millisInFuture + 1500
        if (Looper.myLooper() == Looper.getMainLooper()){
            createCountDownTimer(iTimer, textView, mMillisInFuture, countDownInterval)
        }else{
            //非主线程处理
            mainHandler.post {
                createCountDownTimer(iTimer, textView, mMillisInFuture, countDownInterval)
            }
        }
    }

    /**
     * 关闭计数器
     */
    fun cancel(){
        countDownTimer?.cancel()
    }

    /**
     * 计数器创建
     */
    private fun createCountDownTimer(iTimer: ITimer,
                                     textView: TextView? = null,
                                     millisInFuture: Long,
                                     countDownInterval: Long){

        countDownTimer = object :CountDownTimer(millisInFuture, countDownInterval){
            override fun onTick(millisUntilFinished: Long) {
                val remainderTime = millisUntilFinished/1000 -1
                if (textView != null){
                    textView.text = remainderTime.toString()
                }
            }

            override fun onFinish() {
                iTimer.callback()
            }
        }.start()
    }

}

/**
 * 计数器结果回调接口
 */
interface ITimer{
    fun callback()
}