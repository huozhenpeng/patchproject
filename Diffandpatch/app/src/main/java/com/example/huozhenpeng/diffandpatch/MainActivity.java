package com.example.huozhenpeng.diffandpatch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv_patch;
    private TextView tv_show;
    private TextView tv_getpermission;

    static {
        System.loadLibrary("patch-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_show = (TextView) findViewById(R.id.sample_text);
        tv_show.setText("旧版本版本应用");
        tv_patch= (TextView) findViewById(R.id.tv_patch);
        tv_patch.setOnClickListener(this);
        tv_getpermission= (TextView) findViewById(R.id.tv_getpermission);
        tv_getpermission.setOnClickListener(this);


    }


    public  int getVersionCode (Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取已安装apk文件的原apk文件
     * 如：/data/app/_.apk
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getSourceApkPath(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return null;

        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            return appInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x01:
                    Toast.makeText(MainActivity.this,"合并完成",Toast.LENGTH_LONG).show();
                    installApk(MainActivity.this,newPath);
                    break;
                case 0x02:
                    Toast.makeText(MainActivity.this,"合并失败",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native int patch(String oldPath,String newPath,String diffPath);

    private String oldPath;
    private String newPath;
    private String diffPath;
    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.tv_getpermission:
                getPermission();
                break;
            case R.id.tv_patch:
                if(getVersionCode(MainActivity.this,getPackageName().toString())==2)
                {
                    Toast.makeText(MainActivity.this,"已经是最新版",Toast.LENGTH_LONG).show();
                }
                else
                {
                    //获取差分包，合并差分包,我们把差分包放在sd卡下面
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            oldPath=getSourceApkPath(MainActivity.this,getPackageName().toString());
                            newPath= Environment.getExternalStorageDirectory()+ File.separator+"patch.apk";
                            diffPath=Environment.getExternalStorageDirectory()+ File.separator+"diff.patch";
                            int result=patch(oldPath,newPath,diffPath);
                            if(result==0)
                            {
                                handler.sendEmptyMessage(0x01);

                            }
                            else
                            {
                                handler.sendEmptyMessage(0x02);
                            }

                        }
                    }).start();
                }
                break;

        }
    }

    /**
     * 安装Apk
     *
     * @param context
     * @param apkPath
     */
    public static void installApk(Context context, String apkPath) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkPath),
                "application/vnd.android.package-archive");

        context.startActivity(intent);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public void getPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
