package cloud.mehran.smsforwarder;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker{
    private final Context context;

    public MyWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!ForegroundService.isServiceRunning) {
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            serviceIntent.putExtra("inputExtra", "Listening for new messages...");
            ContextCompat.startForegroundService(context, serviceIntent);
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}
