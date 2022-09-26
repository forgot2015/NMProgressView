package com.linzongfu.nmprogressview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange

/**
 * @author zongfulin
 * Date: 2022-09-26
 * Time: 09:27
 * Description:
 */
public class NMHorizontalProgress(
    val mContext: Context,
    val attrs: AttributeSet?,
    val defStyleAttr: Int,
    val defStyleRes: Int
) : View(mContext, attrs, defStyleAttr, defStyleRes) {
    val TAG = "NMHorizontalProgress"

    var DEBUG = false

    fun setDebug(debug: Boolean) {
        DEBUG = debug
    }

    private val bgPaint = Paint()
    private val dashBgPaint = Paint()
    private val dashPaint = Paint()
    private val thumbPaint = Paint()

    var rectF = RectF()
    var rectBitmap = RectF()

    /**
     * 进度背景色
     */
    @ColorInt
    private var bgColor = 0

    /**
     * 波折线背景颜色
     */
    @ColorInt
    private var dashBgColor = 0

    /**
     * 波折线颜色, 一般指较窄的线成为波折线
     */
    @ColorInt
    private var dashColor = 0

    /**
     * 波折线宽度
     */
    private var dashWidth = 0

    /**
     * 波折线间距宽度
     */
    private var dashDividerWidth = 0

    /**
     * 波折线倾斜角度,垂直 Y 轴为 0, 顺时针转为正数,逆时针为负数
     */
    @IntRange(from = -360, to = 360)
    private var dashAngle = 0

    /**
     * 当前进度
     */
    @IntRange(from = 0, to = 100)
    private var progress = 0

    /**
     * 是否播放动画
     */
    private var animPlay = false

    /**
     * 动画速度,0~10,默认 3, 数字越大速度越快
     */
    @IntRange(from = 1, to = 5)
    private var animSpeed = 3

    /**
     * 上一次动画播放状态,若是播放,则当 view 可见时,继续播放
     */
    private var lastAnimPlay = false


    /**
     * 完成进度时要显示的头部图标
     */
    private var finishThumbBitmap: Bitmap? = null

    /**
     * 头部图标
     */
    private var thumbBitmap: Bitmap? = null

    /**
     * 头部图标偏移距离
     */
    private var thumbOffset = 0

    /**
     * 头部图标的高度. 其实宽度也一样?
     */
    private var thumbHeight = 0

    /**
     * 进度条高度, 注意这个跟整个 view 高度不一定一样
     */
    private var progressHeight = 0

    val DEFAULT_BG_COLOR = -0x1639
    val DEFAULT_DASH_BG_COLOR = -0x227a1
    val DEFAULT_DASH_COLOR = -0x230c6
    val DEFAULT_DASH_WIDTH: Int = 15
    val DEFAULT_DASH_DIVIDER_WIDTH: Int = 15
    val DEFAULT_DASH_ANGLE = 0
    val DEFAULT_PROGRESS = 0
    val DEFAULT_ANIM_SPEED = 3

    constructor(mContext: Context) : this(mContext, null)

    constructor(mContext: Context, attrs: AttributeSet?) : this(mContext, attrs, 0)

    constructor(mContext: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        mContext,
        attrs,
        defStyleAttr,
        0
    )

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.NMHorizontalProgress,
            defStyleAttr,
            defStyleRes
        )
        if (DEBUG) {
            Log.e(TAG, "density = " + Resources.getSystem().displayMetrics.density)
        }
        //        mThumb = a.getDrawable(R.styleable.NMHorizontalProgress_nmh_thumb);
        val mThumb = a.getResourceId(R.styleable.NMHorizontalProgress_nmhp_thumb, -1)
        thumbBitmap = BitmapFactory.decodeResource(resources, mThumb)
        val finishThumb = a.getResourceId(R.styleable.NMHorizontalProgress_nmhp_finish_thumb, -1)
        finishThumbBitmap = BitmapFactory.decodeResource(resources, finishThumb)
        thumbOffset = a.getDimensionPixelSize(R.styleable.NMHorizontalProgress_nmhp_thumb_offset, 0)
        thumbHeight = a.getDimensionPixelSize(
            R.styleable.NMHorizontalProgress_nmhp_thumb_height,
            height
        )
        progressHeight = a.getDimensionPixelSize(
            R.styleable.NMHorizontalProgress_nmhp_progress_height,
            height
        )
        if (DEBUG) {
            Log.e(TAG, "progressHeight = $progressHeight")
        }
        bgColor = a.getColor(R.styleable.NMHorizontalProgress_nmhp_bgColor, DEFAULT_BG_COLOR)
        dashBgColor =
            a.getColor(R.styleable.NMHorizontalProgress_nmhp_dashBgColor, DEFAULT_DASH_BG_COLOR)
        dashColor = a.getColor(R.styleable.NMHorizontalProgress_nmhp_dashColor, DEFAULT_DASH_COLOR)
        dashWidth = a.getDimensionPixelSize(
            R.styleable.NMHorizontalProgress_nmhp_dashWidth,
            DEFAULT_DASH_WIDTH
        )
        dashDividerWidth = a.getDimensionPixelSize(
            R.styleable.NMHorizontalProgress_nmhp_dashDividerWidth,
            DEFAULT_DASH_DIVIDER_WIDTH
        )
        dashAngle = a.getInt(R.styleable.NMHorizontalProgress_nmhp_dashAngle, DEFAULT_DASH_ANGLE)
        progress = a.getInt(R.styleable.NMHorizontalProgress_nmhp_progress, DEFAULT_PROGRESS)
        animSpeed = a.getInt(R.styleable.NMHorizontalProgress_nmhp_anim_speed, DEFAULT_ANIM_SPEED)
        animPlay = a.getBoolean(R.styleable.NMHorizontalProgress_nmhp_anim_play, false)
        initAnimHandler()
        if (animPlay) {
            startAnimation()
        }
        a.recycle()
    }

    fun getProgress(): Int {
        return progress
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width - thumbHeight - thumbOffset
        //        int height = getHeight();
        val centerX = (width / 2).toFloat()
        //这个也是左右两边圆弧的半径
        val centerY = (progressHeight / 2).toFloat()
        //进度条顶部开始的坐标, 注意可能不是 0
        val progressTopY = (height - progressHeight) / 2
        val progressBottomY = progressTopY + progressHeight
        if (DEBUG) {
            Log.i(
                TAG,
                "width = $width, height = $progressHeight , centerX = $centerX, centerY = $centerY"
            )
        }

//        设置图层混合模式
        val xfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

        //------绘制背景色
        bgPaint.color = bgColor
        rectF[0f, progressTopY.toFloat(), width.toFloat()] = progressBottomY.toFloat()
        canvas.drawRoundRect(rectF, progressHeight.toFloat(), progressHeight.toFloat(), bgPaint)
        //        bgPaint.setXfermode(xfermode);

        //-----绘制进度条的背景色
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        dashBgPaint.color = dashBgColor
        //当前进度条宽度
        val progressWidth = (width * progress * 0.01).toInt()
        rectF[0f, progressTopY.toFloat(), progressWidth.toFloat()] = progressBottomY.toFloat()
        canvas.drawRoundRect(rectF, progressHeight.toFloat(), progressHeight.toFloat(), dashBgPaint)
        dashBgPaint.xfermode = xfermode

        //----绘制进度条的波折线, 使用绘制多个矩形的方式, 而矩形的坐标根据角度去计算
        dashBgPaint.color = dashColor
        dashBgPaint.strokeWidth = dashWidth.toFloat()
        dashBgPaint.style = Paint.Style.FILL
        dashBgPaint.isAntiAlias = true
//        dashBgPaint.strokeCap = Paint.Cap.ROUND
        dashBgPaint.strokeCap = Paint.Cap.BUTT
        //绘制有间距,倾斜度的进度条
        //角度转弧度
        val radians = Math.PI / 180 * dashAngle
        val dx = (progressHeight * Math.tan(radians)).toInt()
        //下一个绘制点的偏移距离
        var offset = 0
        //        Log.i(TAG, "radians = " + radians + "dx = " + dx);
        val radius = progressHeight / 2
        while (dither + offset + dx < progressWidth) {
            canvas.drawLine(
                (dither + offset).toFloat(),
                progressBottomY.toFloat(),
                (dither + offset + dx).toFloat(),
                progressTopY.toFloat(),
                dashBgPaint
            )
            offset = offset + dashWidth + dashDividerWidth
        }
        dashBgPaint.xfermode = null // 用完及时清除 Xfermode
        canvas.restoreToCount(saved)

        //绘制头部图标
//        thumbBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_thumb);
        var thumbLeft = progressWidth + thumbOffset
        if (thumbLeft < 0) {
            thumbLeft = 0
        }
        val thumbRight = thumbLeft + thumbHeight
        rectBitmap[thumbLeft.toFloat(), 0f, thumbRight.toFloat()] = thumbHeight.toFloat()
        if (progress == 100) {
            if (finishThumbBitmap != null) {
                canvas.drawBitmap(finishThumbBitmap!!, null, rectBitmap, thumbPaint)
            } else {
                if (thumbBitmap == null) {
                    return
                }
                canvas.drawBitmap(thumbBitmap!!, null, rectBitmap, thumbPaint)
            }
        } else {
            if (thumbBitmap == null) {
                return
            }
            canvas.drawBitmap(thumbBitmap!!, null, rectBitmap, thumbPaint)
        }
    }

    /**
     * 设置头部图片
     */
    fun setThumbBitmap(thumbBitmap: Bitmap?) {
        this.thumbBitmap = thumbBitmap
        invalidate()
    }

    fun setThumbBitmap(@DrawableRes drawableId: Int) {
        thumbBitmap = BitmapFactory.decodeResource(resources, drawableId)
        invalidate()
    }

    /**
     * 抖动距离,用来实现动画的, 取值在 0~ (dashWidth + dashDividerWidth)之间即可,这样就实现了一个组件动画了
     */
    private var dither = 0

    private val ANIM_START = 1
    private val ANIM_STOP = 2

    var ditherHandler: Handler? = null

    fun initAnimHandler() {
        ditherHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ANIM_START -> {
                        changeDither()
                        val message = Message()
                        message.what = ANIM_START
                        this.sendMessageDelayed(message, 50)
                        if (DEBUG) {
                            Log.i(TAG, "ANIM_START")
                        }
                    }
                    ANIM_STOP -> this.removeCallbacksAndMessages(null)
                    else -> {}
                }
            }
        }
    }


    private fun changeDither() {
        val max = dashWidth + dashDividerWidth
        var divisor = 37 - 7 * animSpeed
        if (divisor <= 0) {
            divisor = 2
        }
        //divisor越小,则速度越快,一般 10 就好
        val dx = max / divisor //5,10,15,20,25
        dither += dx
        if (dither >= max) {
            dither = dither - max
        }
        invalidate()
    }

    /**
     * 开启动画
     */
    fun startAnimation() {
        lastAnimPlay = true
        animPlay = true
        ditherHandler?.removeCallbacksAndMessages(null)
        ditherHandler?.sendEmptyMessage(ANIM_START)
    }

    /**
     * 暂停动画, 当页面可见时,会重新播放
     */
    fun pauseAnimation() {
        if (DEBUG) {
            Log.e(TAG, "pauseAnimation")
        }
        ditherHandler?.sendEmptyMessage(ANIM_STOP)
    }

    /**
     * 停止动画
     */
    fun stopAnimation() {
        if (DEBUG) {
            Log.e(TAG, "stopAnimation")
        }
        lastAnimPlay = false
        animPlay = false
        ditherHandler?.sendEmptyMessage(ANIM_STOP)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (DEBUG) {
            Log.e(TAG, "hasWindowFocus = $hasWindowFocus")
        }
        if (hasWindowFocus) {
            if (lastAnimPlay) {
                startAnimation()
            } else {
                stopAnimation()
            }
        } else {
            //当失去焦点时,记录当前播放状态
            lastAnimPlay = animPlay
            pauseAnimation()
        }
    }
}