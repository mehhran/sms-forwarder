package cloud.mehran.smsforwarder;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmsPostJobService extends JobService {
    private static final String TAG = "SmsPostJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");
        doBackgroundWork(jobParameters);
        return true;
    }

    private void doBackgroundWork(JobParameters jobParameters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //format the message body for the telegram message
                StringBuilder text = new StringBuilder();
                text.append("<pre>");
                text.append("From: ").append(jobParameters.getExtras().getString("sender")).append("\n");
                text.append("To: ").append(jobParameters.getExtras().getString("receiver")).append("\n");
                text.append("Message: ").append(jobParameters.getExtras().getString("body"));
                text.append("</pre>");

                //build the POST request to the api
                String chat_id = BuildConfig.SMS_US_CHAT_ID;

                RequestBody formBody = new FormBody.Builder()
                        .add("chat_id", chat_id)
                        .add("text", text.toString())
                        .add("parse_mode", "HTML")
                        .build();

                Request request = new Request.Builder()
                        .url(BuildConfig.TELEGRAM_API_URL)
                        .post(formBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Call call = client.newCall(request);

                try {
                    Response response = call.execute();
                    Objects.requireNonNull(response.body()).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "Job finished");
                jobFinished(jobParameters, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}