package coll.zhanxun.test;

import android.content.Context;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="Caller_Activity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");
        loadJar();
    }

    private void loadJar() {


        final File optimizedDexOutputPath = new File(getStoragePath(this,false).toString()
                + File.separator + "loader_dex.jar");
        Log.d(TAG,"loadJar,"+optimizedDexOutputPath.getAbsolutePath()+",,"+optimizedDexOutputPath.exists());



        //jar的包名和主工程的包名一致时可以用下面的代码   特点：方便简单  缺点：具有包名一致的限制
        /*BaseDexClassLoader cl = new BaseDexClassLoader(optimizedDexOutputPath.getAbsolutePath(),
                this.getFilesDir(),null, this.getClass().getClassLoader());
        Class libProviderClazz = null;
        try {
            // 载入JarLoader类， 并且通过反射构建JarLoader对象， 然后调用sayHi方法
            libProviderClazz = cl.loadClass("fota.adups.myapplication.JarLoader");
            ILoader loader = (ILoader) libProviderClazz.newInstance();
            Toast.makeText(MainActivity.this, loader.sayHi(), Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            // Handle exception gracefully here.
            exception.printStackTrace();
        }*/


        //jar的包名和主工程的可以不一致，通用性强，可以适用于动态加载apk
        // 4.1以后不能够将optimizedDirectory设置到sd卡目录， 否则抛出异常.
        DexClassLoader classLoader = new DexClassLoader(optimizedDexOutputPath.getAbsolutePath(), getFilesDir().getAbsolutePath(),
                null, getClassLoader());

        try {
            // 通过反射机制调用， 包名为com.example.loaduninstallapkdemo, 类名为UninstallApkActivity
            Class mLoadClass = classLoader.loadClass("fota.adups.myapplication.JarLoader");
            Constructor constructor = mLoadClass.getConstructor(new Class[] {});
            Object testActivity = constructor.newInstance(new Object[] {});

            // 获取sayHello方法
            Method helloMethod = mLoadClass.getMethod("sayHi", new  Class[]{});
            helloMethod.setAccessible(true);
            Object content = helloMethod.invoke(testActivity, new Object[] {});
            Toast.makeText(MainActivity.this, content.toString(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
