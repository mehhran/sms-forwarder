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

public class ForwarderJobService extends JobService {
    private static final String TAG = "ForwarderJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");

        //TODO check for forward options
        //email | telegram | ...

        sendToProxyServer(jobParameters);

        Log.d(TAG, "Job finished");

        jobFinished(jobParameters, false);

        return true;
    }

    private void sendToProxyServer(JobParameters jobParameters) {

        //build the POST request
        RequestBody formBody = new FormBody.Builder()
                .add("sender", jobParameters.getExtras().getString("sender"))
                .add("receiver", jobParameters.getExtras().getString("receiver"))
                .add("Message", jobParameters.getExtras().getString("body"))
                .build();

        Request postRequest = new Request.Builder()
                .url(BuildConfig.PROXY_API_URL)
                .post(formBody)
                .addHeader("authorization", BuildConfig.PROXY_API_TOKEN)
                .build();

        Thread httpThread = new httpRequestThread(postRequest);
        httpThread.setPriority(10);
        httpThread.start();
    }


    static class httpRequestThread extends Thread {
        Request request;
        httpRequestThread(Request request){
            this.request = request;
        }

        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);

            try {
                Response response = call.execute();
                Objects.requireNonNull(response.body()).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}