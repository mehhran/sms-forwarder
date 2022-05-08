package cloud.mehran.smsforwarder;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Objects;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //confirm that all of the received content is valid
        if (context == null || intent == null || intent.getAction() == null) {
            return;
        }

        //check that we’ve received the SMS data
        if (!Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            return;
        }

        Log.d(TAG, "Received a message");

        //get message details
        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        String messageSender = smsMessages[0].getOriginatingAddress();
        StringBuilder message_parts = new StringBuilder();
        int i;
        for (i = 0; i < smsMessages.length; i++) {
            message_parts.append(smsMessages[i].getMessageBody());
        }
        String messageBody = message_parts.toString();

        //get the SIM's details
        String simNumber;
        Bundle intentExtras = intent.getExtras();
        int sub = intentExtras.getInt("subscription", -1);
        SubscriptionManager manager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission check failed.");
            simNumber = "Unknown";
        } else {
            SubscriptionInfo subInfo = manager.getActiveSubscriptionInfo(sub);
            simNumber = subInfo.getNumber();
            //String simName = (String) subInfo.getDisplayName();
            //String simCarrier = (String) subInfo.getCarrierName();
        }

        //bundling up the message info
        PersistableBundle messageBundle = new PersistableBundle();
        messageBundle.putString("receiver", simNumber);
        messageBundle.putString("sender", messageSender);
        messageBundle.putString("body", messageBody);

        //schedule the Forward job
        ComponentName forwarderJobService = new ComponentName(context, ForwarderJobService.class);
        JobInfo info = new JobInfo.Builder(123, forwarderJobService)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(messageBundle)
                .build();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }
}