
package cn.edu.tsinghua.hpc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class NotificationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        String packageName = getIntent().getExtras().getString("package");
        new Thread(new DownloadRunnable(packageName)).start();
    }

    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;

    public class DownloadRunnable implements Runnable {
        private String packageName;

        public DownloadRunnable(String packageName) {
            this.packageName = packageName;
        }

        private boolean preparePackage(String pkgFile) {

            if (ApplicationUtils.getInstance().removePackage(packageName)) {
                return ApplicationUtils.getInstance().installPackage(pkgFile);
            }
            return false;

        }

        public void run() {
            Log.d("test", "download " + packageName);

            String apkName = ApplicationUtils.getInstance().downloadTApp(packageName, packageName,
                    NotificationActivity.this);
            if (apkName != null) {
                if (preparePackage(apkName)) {

                    PackageManager manager = getPackageManager();

                    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                    final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
                    for (ResolveInfo i : apps) {
                        if (i.activityInfo.packageName.equals(packageName)) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.setComponent(new ComponentName(i.activityInfo.packageName,
                                    i.activityInfo.name));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(intent);
                            break;

                        }
                    }

                }
            } else {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(NotificationActivity.this,
                                "Can not retrieve destination application!", Toast.LENGTH_LONG);

                    }
                });

            }

            finish();

        }
    }

}
