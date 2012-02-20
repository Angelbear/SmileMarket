
package cn.edu.tsinghua.hpc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LaunchAppService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
 

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Intent i = new Intent(this, NotificationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtras(intent.getExtras());
        startActivity(i);
    }

}
