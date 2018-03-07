package com.rx.fblive.app

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import android.view.animation.Animation
import kotlinx.android.synthetic.main.act_fb_live_reaction.*
import android.view.animation.AnimationUtils
import io.reactivex.*
import io.reactivex.schedulers.Timed
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe


/**
 * Created by Albert-IM on 07/03/2018.
 */
class FbLiveVideoReactionAct: AppCompatActivity() {

    private var emoticonSubscription: Subscription? = null

    private var subscriber: Subscriber<Timed<Emoticons>>? = null
    private val MINIMUM_DURATION_BETWEEN_EMOTICONS = 300 // in milliseconds

    private var emoticonClickAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_fb_live_reaction)
    }

    override fun onStart() {
        super.onStart()

        val flowableOnSubscribe = FlowableOnSubscribe<Emoticons> ( {
            emitter -> convertClickEventToStream(emitter)
        })

        //Give the backpressure strategy as BUFFER, so that the click items do not drop.
        val emoticonsFlowable = Flowable.create<Emoticons>(flowableOnSubscribe, BackpressureStrategy.BUFFER)
//        val emoticonsFlowable = Flowable.create<Emoticons>({emitter -> convertClickEventToStream(emitter) }, BackpressureStrategy.BUFFER)

        //Convert the stream to a timed stream, as we require the timestamp of each event
        val emoticonsTimedFlowable = emoticonsFlowable.timestamp()
        subscriber = getSubscriber()

        //Subscribe
        emoticonsTimedFlowable.subscribeWith(subscriber)
    }

    private fun getSubscriber(): Subscriber<Timed<Emoticons>> {
        return object: Subscriber<Timed<Emoticons>> {
            override fun onComplete() {
                if (emoticonSubscription != null) {
                    emoticonSubscription!!.cancel()
                }
            }

            // 구독이 시작되면 CustomView인 emoticonsView를 초기화한다
            override fun onSubscribe(s: Subscription?) {
                emoticonSubscription = s
                emoticonSubscription!!.request(1)

                // for lazy evaluation.
                emoticonsView.initView(this@FbLiveVideoReactionAct)
            }

            override fun onError(t: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            // 아이템이 발행되면 emoticonsView에 전달한다.
            override fun onNext(t: Timed<Emoticons>?) {
                emoticonsView.addView(t!!.value())

                // 딜레이를 주기위해 현재 시간과 아이템발행 시간을 비교해서 300밀리세컨이 지나지 않았으면 300밀리세컨에 맞게 연속으로 발행한다.
                val currentTimeStamp = System.currentTimeMillis()
                val diffInMillis = currentTimeStamp - (t as Timed<*>).time()
                if (diffInMillis > MINIMUM_DURATION_BETWEEN_EMOTICONS) {
                    emoticonSubscription!!.request(1)
                } else {
                    val handler = Handler()
                    handler.postDelayed({ emoticonSubscription!!.request(1) }, MINIMUM_DURATION_BETWEEN_EMOTICONS - diffInMillis)
                }
            }
        }
    }


    public override fun onStop() {
        super.onStop()
        emoticonSubscription!!.cancel()
    }

    private fun convertClickEventToStream(emitter: FlowableEmitter<Emoticons>) {
        ivLikeEmoticon.setOnClickListener( {
            doOnClick(ivLikeEmoticon, emitter, Emoticons.LIKE)
        })

        ivLoveEmoticon.setOnClickListener( {
            doOnClick(ivLoveEmoticon, emitter, Emoticons.LOVE)
        })

        ivHahaEmoticon.setOnClickListener( {
            doOnClick(ivHahaEmoticon, emitter, Emoticons.HAHA)
        })

        ivWowEmoticon.setOnClickListener( {
            doOnClick(ivWowEmoticon, emitter, Emoticons.WOW)
        })

        ivSadEmoticon.setOnClickListener( {
            doOnClick(ivSadEmoticon, emitter, Emoticons.SAD)
        })

        ivAngryEmoticon.setOnClickListener( {
            doOnClick(ivAngryEmoticon, emitter, Emoticons.ANGRY)
        })
    }

    private fun doOnClick(view: View, emitter: FlowableEmitter<Emoticons>, emoticons: Emoticons) {
        // 하단 이모티콘 애니메이션
        emoticonClickAnimation = AnimationUtils.loadAnimation(this, R.anim.emoticon_click_animation)
        view.startAnimation(emoticonClickAnimation)

        // 아이템 emit
        emitter.onNext(emoticons)
    }
}