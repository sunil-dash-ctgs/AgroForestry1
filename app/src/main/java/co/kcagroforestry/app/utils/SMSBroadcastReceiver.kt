package co.kcagroforestry.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SMSBroadcastReceiver: BroadcastReceiver() {
    var smsBroadCastReceiverListener: SmsBroadCastReceiverListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action){
            val extras = intent.extras
            val smsRetrieverStatusCodes = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when(smsRetrieverStatusCodes.statusCode){

                CommonStatusCodes.SUCCESS -> {
                    val messageIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    smsBroadCastReceiverListener?.onSuccess(messageIntent!!)

                }

                CommonStatusCodes.TIMEOUT -> {
                    smsBroadCastReceiverListener?.onFailure()
                }
            }
        }


    }


    interface SmsBroadCastReceiverListener{

        fun onSuccess(intent: Intent)
        fun onFailure()
    }

}