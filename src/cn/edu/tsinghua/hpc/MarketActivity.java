
package cn.edu.tsinghua.hpc;

import cn.edu.tsinghua.hpc.ApplicationUtils.TransAppInfo;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

public class MarketActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        List<TransAppInfo> infos = ApplicationUtils.getInstance().downloadTAppInfo();
        mAdapter = new MarketListAdapter(this, R.layout.market_item, R.id.app_title, infos);
        setListAdapter(mAdapter);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private MarketListAdapter mAdapter;

    public static boolean appIsInstalled(Context context, String pageName) {
        try {
            PackageInfo packinfo = context.getPackageManager().getPackageInfo(pageName, 0);
            return true;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            return false;
        }
    }

    private final class MarketListAdapter extends ArrayAdapter<TransAppInfo> {

        private HashMap<String, SoftReference<Drawable>> imageCache;

        public MarketListAdapter(Context context, int resource, int textViewResourceId,
                List<TransAppInfo> objects) {
            super(context, resource, textViewResourceId, objects);
            imageCache = new HashMap<String, SoftReference<Drawable>>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            if (convertView == null) {
                v = LayoutInflater.from(MarketActivity.this).inflate(R.layout.market_item, null);
            } else {
                v = convertView;
            }
            final LoaderImageView image = (LoaderImageView) v.findViewById(R.id.imageView1);
            TextView tv_title = (TextView) v.findViewById(R.id.app_title);
            TextView tv_size = (TextView) v.findViewById(R.id.app_size);
            Button b = (Button) v.findViewById(R.id.button1);
            final TransAppInfo info = getItem(position);

            final String imageUrl = ApplicationUtils.URL + info.packageName + "/icon";
            if (imageCache.get(imageUrl) != null && imageCache.get(imageUrl).get() != null) {
                image.setLocalDrawable(imageCache.get(imageUrl).get());
            } else {
                CacheHandler h = new CacheHandler(imageUrl);
                image.setImageDrawable(imageUrl, h);
            }
            tv_title.setText(info.title);
            tv_size.setText(String.valueOf(info.size));

            if (appIsInstalled(MarketActivity.this, info.packageName)) {
                b.setText(R.string.unsubscribe);
                b.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View paramView) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                ApplicationUtils.getInstance().removePackage(info.packageName);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        MarketListAdapter.this.notifyDataSetChanged();
                                    }
                                });

                            }
                        }).start();
                    }
                });
            } else {
                b.setText(R.string.subscribe);
                b.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View paramView) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                String path = ApplicationUtils.getInstance().downloadTAppEgg(
                                        info.packageName, info.packageName, MarketActivity.this);
                                ApplicationUtils.getInstance().installPackage(path);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        MarketListAdapter.this.notifyDataSetChanged();
                                    }
                                });

                            }
                        }).start();
                    }
                });
            }

            return v;
        }

        private class CacheHandler extends Handler {

            String imageUrl;

            public CacheHandler(String imageUrl) {
                this.imageUrl = imageUrl;
            }

            @Override
            public void handleMessage(Message msg) {
                imageCache.put(imageUrl, new SoftReference<Drawable>((Drawable) msg.obj));
            }

        }
    }
}
