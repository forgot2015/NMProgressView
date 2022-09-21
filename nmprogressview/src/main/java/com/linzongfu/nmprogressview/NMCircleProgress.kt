package com.linzongfu.nmprogressview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt

/**
 * @author zongfulin
 * Date: 2022-09-21
 * Time: 10:02
 * Description:
 */
public class NMCircleProgress(
    val mContext: Context,
    val attrs: AttributeSet?,
    val defStyleAttr: Int,
    val defStyleRes: Int
) :
    View(mContext, attrs, defStyleAttr, defStyleRes) {
    val TAG = "NMCircleProgress"

    private var DEBUG = false

    private val paint = Paint()
    private val textPaint = Paint()
    private val textBounds = Rect()
    private val arcRectF = RectF()

    fun setDebug(debug: Boolean) {
        DEBUG = debug
    }

    /**
     * 圆饼背景色
     */
    @ColorInt
    private var bgCakeColor = 0

    /**
     * 未完成的进度条颜色
     */
    @ColorInt
    private var progressBgColor = 0

    /**
     * 未完成的进度条宽度
     */
    private var progressBgWidth = 0

    /**
     * 未完成进度条的进度
     */
    private var bgProgress = 0

    /**
     * 已完成的进度条颜色
     */
    @ColorInt
    private var progressBarColor = 0

    /**
     * 已完成的进度条宽度
     */
    private var progressBarWidth = 0

    /**
     * 已完成进度条的进度
     */
    private var progress = 0

    /**
     * 最大进度值,默认一百
     */
    private var maxProgress = 0

    /**
     * 进度条旋转方向,0 顺时针,1 逆时针
     */
    private var progressDirection = 0

    /**
     * 顺时针
     */
    val DIRECTION_CLOCKWISE = 0

    /**
     * 逆时针
     */
    val DIRECTION_ANTI_CLOCKWISE = 1

    /**
     * 中间文字颜色
     */
    @ColorInt
    private var textColor = 0

    /**
     * 中间文字失效时的颜色
     * 默认与textColor相同
     */
    @ColorInt
    private var textColorDisable = 0

    /**
     * 文字状态,true正常, false失效
     */
    private var textEnable = true

    /**
     * 中间文字大小
     */
    private var textSize = 0

    /**
     * 中间文字内容
     */
    private var textContent: String? = null

    /**
     * 要显示的文字类型
     */
    private var progressTextType = 0

    /**
     * 显示进度值
     */
    val TEXT_TYPE_PROGRESS = 1

    /**
     * 显示固定文字
     */
    val TEXT_TYPE_FIXED = 2

    /**
     * 文字样式
     * <flag name="normal" value="0"></flag>
     * <flag name="bold" value="1"></flag>
     * <flag name="italic" value="2"></flag>
     */
    private var textStyle = 0

    /**
     * 文字是否加删除线
     */
    private var strikeThruText = false

    /**
     * 文字是否加下划线
     */
    private var underlineText = false

    /**
     * 开始的角度,默认 0,即 3 点钟顺时针转圈
     */
    private var startAngle = 0

    /**
     * 结束的角度,默认 360
     */
    private var endAngle = 0

    /**
     * 每个进度值代表的角度, 如总进度 100,总角度 360,则每进度代表 3.6 度
     */
    private var anglePerProgress = 0f

    val DEFAULT_BG_COLOR = Color.CYAN
    val DEFAULT_PROGRESS_BG_COLOR = Color.GRAY
    val DEFAULT_PROGRESS_BG_WIDTH: Int = 9
    val DEFAULT_PROGRESS_BAR_COLOR = Color.YELLOW
    val DEFAULT_PROGRESS_BAR_WIDTH: Int = 15
    val DEFAULT_TEXT_CONTENT = ""
    val DEFAULT_TEXT_SIZE = 15
    val DEFAULT_TEXT_COLOR = Color.BLACK
    val DEFAULT_TEXT_COLOR_DISABLE = Color.GRAY
    val DEFAULT_PROGRESS = 0
    val DEFAULT_BG_PROGRESS = 100
    val DEFAULT_MAX_PROGRESS = 100
    val DEFAULT_TEXT_STYLE = 0
    val DEFAULT_TEXT_TYPE = 1
    val DEFAULT_START_ANGLE = 0
    val DEFAULT_END_ANGLE = 360
    val DEFAULT_DIRECTION = 0

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
            R.styleable.NMCircleProgress,
            defStyleAttr,
            defStyleRes
        )
        if (DEBUG) {
            Log.e(TAG, "density = " + Resources.getSystem().displayMetrics.density)
        }
        bgCakeColor = a.getColor(R.styleable.NMCircleProgress_nmcp_cakeBgColor, DEFAULT_BG_COLOR)
        progressBgColor =
            a.getColor(R.styleable.NMCircleProgress_nmcp_progressBgColor, DEFAULT_PROGRESS_BG_COLOR)
        progressBgWidth = a.getDimensionPixelSize(
            R.styleable.NMCircleProgress_nmcp_progressBgWidth,
            DEFAULT_PROGRESS_BG_WIDTH
        )
        progressBarColor = a.getColor(
            R.styleable.NMCircleProgress_nmcp_progressBarColor,
            DEFAULT_PROGRESS_BAR_COLOR
        )
        progressBarWidth = a.getDimensionPixelSize(
            R.styleable.NMCircleProgress_nmcp_progressBarWidth,
            DEFAULT_PROGRESS_BAR_WIDTH
        )
        maxProgress = a.getInt(R.styleable.NMCircleProgress_nmcp_maxProgress, DEFAULT_MAX_PROGRESS)
        bgProgress = a.getInt(R.styleable.NMCircleProgress_nmcp_bgProgress, DEFAULT_BG_PROGRESS)
        if (bgProgress > maxProgress) {
            bgProgress = maxProgress
        }
        progress = a.getInt(R.styleable.NMCircleProgress_nmcp_progress, DEFAULT_PROGRESS)
        if (progress > maxProgress) {
            progress = maxProgress
        }
        startAngle = a.getInt(R.styleable.NMCircleProgress_nmcp_startAngle, DEFAULT_START_ANGLE)
        endAngle = a.getInt(R.styleable.NMCircleProgress_nmcp_endAngle, DEFAULT_END_ANGLE)
        progressDirection = a.getInt(R.styleable.NMCircleProgress_nmcp_direction, DEFAULT_DIRECTION)
        anglePerProgress = (endAngle - startAngle) * 1.0f / maxProgress
        Log.e(
            TAG,
            "endAngle = $endAngle, startAngle = $startAngle, maxProgress = $maxProgress"
        )
        Log.e(TAG, "anglePerProgress = $anglePerProgress")
        textColor =
            a.getColor(R.styleable.NMCircleProgress_nmcp_progressTextColor, DEFAULT_TEXT_COLOR)
        textColorDisable =
            a.getColor(R.styleable.NMCircleProgress_nmcp_progressTextColorDisable, textColor)
        textSize = a.getDimensionPixelSize(
            R.styleable.NMCircleProgress_nmcp_progressTextSize,
            DEFAULT_TEXT_SIZE
        )
        textStyle = a.getInt(R.styleable.NMCircleProgress_nmcp_textStyle, DEFAULT_TEXT_STYLE)
        progressTextType =
            a.getInt(R.styleable.NMCircleProgress_nmcp_progressTextType, DEFAULT_TEXT_TYPE)
        textContent = a.getString(R.styleable.NMCircleProgress_nmcp_progressText)
        if (TextUtils.isEmpty(textContent)) {
            textContent = DEFAULT_TEXT_CONTENT
        }
        strikeThruText = a.getBoolean(R.styleable.NMCircleProgress_nmcp_strikeThruText, false)
        underlineText = a.getBoolean(R.styleable.NMCircleProgress_nmcp_underlineText, false)

        a.recycle()
    }

    fun getProgress(): Int {
        return progress
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        if (this.progress > maxProgress) {
            this.progress = maxProgress
        }
        invalidate()
    }

    fun setText(text: String?) {
        textContent = text
        invalidate()
    }

    fun setTextEnable(enable: Boolean) {
        textEnable = enable
        invalidate()
    }

    fun setTextType(textType: Int) {
        progressTextType = textType
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()

        //绘制里面的圆饼---------------------------------------------------------
        paint.color = bgCakeColor
        //设置画笔样式为填充
        paint.style = Paint.Style.FILL
        //设置画笔为圆头
        paint.strokeCap = Paint.Cap.ROUND
        //计算内圆饼的坐标偏移值, 即实际绘制的坐标 progressBarWidth
        val offset = progressBarWidth / 2 + progressBgWidth / 2
        canvas.drawOval(
            offset.toFloat(),
            offset.toFloat(),
            (width - offset).toFloat(),
            (height - offset).toFloat(),
            paint
        )

        //绘制外面进度条背景------------------------------------------------------
        paint.color = progressBgColor
        //设置画笔样式为描边
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        //设置画笔宽度为进度条背景的宽度
        paint.strokeWidth = progressBgWidth.toFloat()
        //进度条背景的偏移值
        val progressBgOffset = (progressBarWidth / 2).toFloat()
        //半径要减去进度条的宽度,否则会超过view显示.
        //注意画笔宽度导致的矩形尺寸变化, 如本来矩形长 200 (0~200),而画笔宽 20, 则实际绘制线在中间 10,即矩形内部实际宽为 180 (真实绘制线 10~190)
        arcRectF[progressBgOffset, progressBgOffset, width - progressBgOffset] =
            height - progressBgOffset
        //将进度值转换为 0~100
        val realBgProgress = (bgProgress * 1.0 / maxProgress * 100).toInt()
        //一圈角度为 360°
        val bgSweepAngle = realBgProgress * anglePerProgress
        var realBgStartAngle = startAngle.toFloat()
        if (progressDirection == DIRECTION_ANTI_CLOCKWISE) {
            //若是反方向,若重新计算开始点
            realBgStartAngle = (startAngle - bgSweepAngle).toInt().toFloat()
        }
        canvas.drawArc(arcRectF, realBgStartAngle, bgSweepAngle, false, paint)

        //绘制外面已完成的进度条-----------------------------------------------------
        paint.color = progressBarColor
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = progressBarWidth.toFloat()
        arcRectF[progressBgOffset, progressBgOffset, width - progressBgOffset] =
            height - progressBgOffset
        val realProgress = (progress * 1.0 / maxProgress * 100).toInt()
        val sweepAngle = realProgress * anglePerProgress
        var realStartAngle = startAngle.toFloat()
        if (progressDirection == DIRECTION_ANTI_CLOCKWISE) {
            //若是反方向,若重新计算开始点
            realStartAngle = (startAngle - sweepAngle).toInt().toFloat()
        }
        canvas.drawArc(arcRectF, realStartAngle, sweepAngle, false, paint)
        Log.e(TAG, "progressDirection = $progressDirection")
        Log.e(TAG, "realStartAngle = $realStartAngle , sweepAngle = $sweepAngle")

        //绘制圆饼中间的文字---------------------------------------------------------
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = textSize.toFloat()
        textPaint.color = if (textEnable) textColor else textColorDisable
        val font = Typeface.create(Typeface.DEFAULT, textStyle)
        textPaint.typeface = font
        textPaint.isStrikeThruText = strikeThruText
        textPaint.isUnderlineText = underlineText
        val text: String?
        text = if (progressTextType == TEXT_TYPE_PROGRESS) {
            "$realProgress%"
        } else {
            textContent
        }

        //获取文字长度,并使文字居中显示
        textPaint.getTextBounds(text, 0, text!!.length, textBounds)
        val textWidth = (textBounds.right - textBounds.left).toFloat()
        val textHeight = (textBounds.bottom - textBounds.top).toFloat()
        //文字绘制实际是从左下角开始的, 所以取值是 startX 和 bottomY
        val textStartX = centerX - textWidth / 2
        val textBottomY = centerY + textHeight / 2
        canvas.drawText(text, textStartX, textBottomY, textPaint)
        if (DEBUG) {
            Log.i(
                TAG,
                "width = $width, textWidth = $textWidth , textHeight = $textHeight , textStartX = $textStartX , textBottomY = $textBottomY"
            )
        }

        //文字居中的方法二
//        textPaint.setTextAlign(Paint.Align.CENTER);
//        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
//        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
//        int baseLineY = (int) (centerY - top / 2 - bottom / 2);
////        canvas.drawText(text, centerX, baseLineY, textPaint);
//        Log.e(TAG, "centerX = " + centerX + ", baseLineY = " + baseLineY);
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
    }
}