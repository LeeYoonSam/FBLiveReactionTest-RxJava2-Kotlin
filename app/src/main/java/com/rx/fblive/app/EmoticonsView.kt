package com.rx.fblive.app

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.util.DisplayMetrics
import android.graphics.Bitmap
import java.util.*


/**
 * Created by Albert-IM on 07/03/2018.
 *
 * 코틀린에서 커스텀뷰 만들기
 * @JvmOverloads 사용해서 3개의 생성자를 모두 작성하지 않고 만들기
 * JvmOverloads: 생성자의 parameter가 기본값으로 대체하도록 컴파일러에 지시한다는 의미이다.
 */
class EmoticonsView @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {

    private var mPaint: Paint? = null
    private var mAnimPath: Path? = null
    private var mMatrix: Matrix? = null
    var mLike48: Bitmap? = null
    var mLove48: Bitmap? = null
    var mHaha48: Bitmap? = null
    var mWow48: Bitmap? = null
    var mSad48: Bitmap? = null
    var mAngry48: Bitmap? = null

    private val mLiveEmoticons = ArrayList<LiveEmoticon>()
    private val X_CORDINATE_STEP = 8
    private val Y_CORDINATE_OFFSET = 100
    private val Y_CORDINATE_RANGE = 200
    private var mScreenWidth: Int = 0

    init {
        mPaint = Paint()
        mAnimPath = Path()
        mMatrix = Matrix()
    }

    fun initView(activity: Activity) {
        val displayMetrix = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrix)

        mScreenWidth = displayMetrix.widthPixels

        val res: Resources = resources
        mLike48 = BitmapFactory.decodeResource(res, R.drawable.like_48)
        mLove48 = BitmapFactory.decodeResource(res, R.drawable.love_48)
        mHaha48 = BitmapFactory.decodeResource(res, R.drawable.haha_48)
        mWow48 = BitmapFactory.decodeResource(res, R.drawable.wow_48)
        mSad48 = BitmapFactory.decodeResource(res, R.drawable.sad_48)
        mAngry48 = BitmapFactory.decodeResource(res, R.drawable.angry_48)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas!!.drawPath(mAnimPath, mPaint)
        drawAllLiveEmoticons(canvas)
    }

    private fun drawAllLiveEmoticons(canvas: Canvas) {
        val iterator = mLiveEmoticons.listIterator()

        while (iterator.hasNext()) {
            val liveEmoticon: LiveEmoticon = iterator.next()

            val xCordinate: Int = liveEmoticon.xCordinate - X_CORDINATE_STEP
            val yCordinate: Int = liveEmoticon.yCordinate

            // -8만큼씩 계속 이동
            liveEmoticon.xCordinate = xCordinate

            // 이모티콘이 사라지기 전이면
            if(xCordinate > 0) {
                mMatrix!!.reset()
                mMatrix!!.postTranslate(xCordinate.toFloat(), yCordinate.toFloat())
                resizeImageSizeBasedOnXCoordinates(canvas, liveEmoticon);
                invalidate()
            } else {

                // 다 보여진후에 지운다.
                iterator.remove()
            }

        }
    }

    private fun resizeImageSizeBasedOnXCoordinates(canvas: Canvas, liveEmoticon: LiveEmoticon) {
        val xCordinate = liveEmoticon.xCordinate
        var bitMap48: Bitmap? = null
        var scaled: Bitmap? = null

        val emoticons = liveEmoticon.emoticons

        bitMap48 = when(emoticons) {
            Emoticons.LIKE -> mLike48
            Emoticons.LOVE -> mLove48
            Emoticons.HAHA -> mHaha48
            Emoticons.WOW -> mWow48
            Emoticons.SAD -> mSad48
            Emoticons.ANGRY -> mAngry48
        }

        // 화면의 반이상을 지날때 아이콘 크기 그대로 표시
        if(xCordinate > mScreenWidth / 2) {
            canvas.drawBitmap(bitMap48, mMatrix, null)
        }
        // 화면의 1/4(시작할때쯤)이상을 지나갈때 아이콘 크기 3/4 으로 변경해서 표시
        else if (xCordinate > mScreenWidth / 4) {
            scaled = Bitmap.createScaledBitmap(bitMap48, 3 * bitMap48!!.width / 4, 3 * bitMap48!!.height / 4, false);
            canvas.drawBitmap(scaled, mMatrix, null);
        }
        // 화면의 양쪽 끝일때 아이콘 크기를 반으로 변경해서 표시
        else {
            scaled = Bitmap.createScaledBitmap(bitMap48, bitMap48!!.width / 2, bitMap48!!.height / 2, false);
            canvas.drawBitmap(scaled, mMatrix, null);
        }
    }

    fun addView(emoticons: Emoticons) {

        // 오른쪽에서 왼쪽으로 흘러감 - startXCoordinate는 화면의 제일 오른쪽에서 시작
        val startXCoordinate = mScreenWidth

        // 높이는 100~300사이 랜덤으로 결정
        val startYCoordinate = Random().nextInt(Y_CORDINATE_RANGE) + Y_CORDINATE_OFFSET

        // 전달된 enum 타입의 이모티콘을 LiveEmoticon 객체에 담아서 mLiveEmoticons 이모티콘 리스트에 추가
        val liveEmoticon = LiveEmoticon(emoticons, startXCoordinate, startYCoordinate)
        mLiveEmoticons.add(liveEmoticon)

        // 다시 그리기
        invalidate()
    }
}