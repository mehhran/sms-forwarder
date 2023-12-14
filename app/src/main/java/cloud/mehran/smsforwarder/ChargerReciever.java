package cloud.mehran.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class ChargerReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case Intent.ACTION_POWER_CONNECTED:
                if (!ForegroundService.isServiceRunning) {
                    Intent serviceIntent = new Intent(context, ForegroundService.class);
                    serviceIntent.putExtra("inputExtra", "Listening for new messages...");
                    ContextCompat.startForegroundService(context, serviceIntent);
                }
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                break;
        }

    }

}
