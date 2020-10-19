package com.newpos.MToast

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MToast private constructor(private val builder: Builder, context: Context){

    private val context: Context = context
    private var toast: Toast? = null
    private var tv: TextView? = null
    private var container: View? = null

    fun show(resId: Int){
        show(context.getString(resId))
    }

    fun show(message:String?, vararg any: Any){

        if(TextUtils.isEmpty(message)) return

        var result = message as String
        if (any.isNotEmpty()){
            result = String.format(message, any)
        }

        if (Looper.myLooper() == Looper.getMainLooper()){
            showInternal(result)
        }else{
            mainHandler.post{showInternal(result)}
        }
    }

    private fun showInternal(message: String){
        createToastIfNeeded()
        if (builder.isDefault){
            toast?.setText(message)
        }else{
            tv?.text = message
        }
        toast?.show()
    }

    @SuppressLint("ShowToast")
    private fun createToastIfNeeded(){
        if (toast == null){
            if (builder.isDefault){
                toast = Toast.makeText(context, "", builder.duration)
            }else{
                container = builder.layout?:LayoutInflater.from(context).inflate(builder.layoutId, null)
                tv = container?.findViewById(builder.tvId)
                toast = Toast(context)
                toast?.view = container
                toast?.duration = builder.duration
            }

            if (builder.gravity != 0){
                toast?.setGravity(builder.gravity, builder.offsetX, builder.offsetY)
            }

        }
    }

    companion object{
        private val mainHandler = Handler(Looper.getMainLooper())

        /**
         * 默认提供的Toast实例，在首次使用时进行加载。
         */
        @JvmStatic
        fun default(context: Context): MToast{
            return newBuilder(context).build()
        }

        @JvmStatic
        private fun newBuilder(context: Context): Builder{
            return Builder(isDefault = true, context = context)
        }
        @JvmStatic
        fun newBuilder(layoutId: Int, tvId: Int, context: Context): Builder{
            return Builder(isDefault = false, layoutId = layoutId, tvId = tvId, context = context)
        }
        @JvmStatic
        fun newBuilder(layout: View, tvId: Int, context: Context): Builder{
            assert(layout.parent == null)
            return Builder(isDefault = false, layout = layout, tvId = tvId, context = context)
        }

    }

    //internal : 仅模块内部可见
    class Builder internal constructor(internal val isDefault: Boolean,
                internal val layout: View?= null,
                internal val layoutId: Int = 0,
                internal var tvId: Int = 0,
                private val context: Context){

        internal var duration: Int = Toast.LENGTH_SHORT
        internal var gravity: Int = 0
        internal var offsetX: Int = 0
        internal var offsetY: Int = 0

        fun setGravity(gravity: Int, offsetX: Int, offsetY: Int): Builder{
            this.gravity = gravity
            this.offsetX = offsetX
            this.offsetY = offsetY
            return this
        }

        fun setDuration(duration: Int): Builder{
            this.duration = duration
            return this
        }

        fun build(): MToast{
            return MToast(this, context)
        }

    }

}