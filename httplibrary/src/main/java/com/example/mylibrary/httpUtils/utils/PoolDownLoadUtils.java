package com.example.mylibrary.httpUtils.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PoolDownLoadUtils {

    private ExecutorService pool;
    private static volatile PoolDownLoadUtils sInstance;
    private final int MAX_DOWNLOADING_TASK = 5; // 最大同时下载数
    public static String TAG = "测试多文件下载";
    public static String TAG_LOAD_APK = "测试APK下载";
    public static String SP_SAVE_LOADFILENAME = "SP_SAVE_LOADFILENAME";//下载好的svg保存姓名

    private PoolDownLoadUtils() {
        pool = new ThreadPoolExecutor(
                MAX_DOWNLOADING_TASK, MAX_DOWNLOADING_TASK, 40, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2000));
    }

    public static PoolDownLoadUtils getInstance() {
        if (sInstance == null) {
            synchronized (PoolDownLoadUtils.class) {
                if (sInstance == null) {
                    sInstance = new PoolDownLoadUtils();
                }
            }
        }
        return sInstance;
    }

    public static String getNativePlayUrl(Context context, String netUrl) {
        String names[] = netUrl.split("/");
        String fileName = names[names.length - 1].substring(0, names[names.length - 1].length() - 5);
        String allFileName = (String) SPUtil.get(context, SP_SAVE_LOADFILENAME, "");
        //fixme SP有，还要判断本地，免得本地被删除了
        if (allFileName.contains(fileName)) {
            File fileFactory = createFileDir(context, "svgadir");
            String trueName = fileName + ".svg";
            File newFile = new File(fileFactory, trueName);
            if (newFile.exists()) {
                return newFile.getAbsolutePath();
            }
        }
        return "";
    }

    //添加下载队列任务
    public void addDownLoadTask(final Context context, final String fileName, final String imgNetUrl) {
        final File file = createFileDir(context, "svgadir");
        boolean needThisTask = true;
        //fixme 本地有 1：没卸载，sp有值      2.本地有,但是卸载了,sp没值，删除重新下
        if (isFileExists(file, fileName)) {
            String SpSaveLoadFileName = (String) SPUtil.get(context, SP_SAVE_LOADFILENAME, "");
            logLoadSvg("################本地存在相同文件：" + fileName);
            logLoadSvg("################已下载的文件名拼接=" + SpSaveLoadFileName);
            //fixme 1：没卸载，sp有值
            if (!SpSaveLoadFileName.equals("")) {
                String allSaveLoadFileName[] = SpSaveLoadFileName.split(",");
                for (int i = 0; i < allSaveLoadFileName.length; i++) {
                    if (allSaveLoadFileName[i].equals(fileName)) {
                        logLoadSvg("文件名=" + fileName + "################已经被下载完成了,废弃此下载任务");
                        needThisTask = false;//fixme sp有文件，不下载
                        break;
                    }
                }
                //fixme sp没这个文件，需要删除源文件
                if (needThisTask) {
                    new File(file, fileName).delete();
                    logLoadSvg("文件名=" + fileName + "################sp有值，但是sp没存储此值，删除重新下载");
                }
            }
            //fixme 2.本地有,但是卸载了,sp没值，删除重新下
            else {
                new File(file, fileName).delete();
                needThisTask = true;
                LogUtils.logE(TAG, "文件名=" + fileName + "################本地有,但是卸载了,sp没值，删除原下载好的文件");
            }
        }
        if (!needThisTask) {
            LogUtils.logE(TAG, "文件名=" + fileName + "################不需要此任务,不下载");
            return;
        }
        LogUtils.logE(TAG, "文件名=" + fileName + "---------------------开始下载开启1~");
        //添加任务
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int readTimeout = 300000;
                    int connectTimeout = 300000;
                    URL url = new URL(imgNetUrl);
                    URLConnection conn = url.openConnection();
//                    conn.setUseCaches(true);
                    conn.setConnectTimeout(connectTimeout);
                    conn.setReadTimeout(readTimeout);
                    InputStream in = conn.getInputStream();
//                    long contentLength = conn.getContentLength();
//                   LogUtils.logE(TAG, fileName + "文件的服务器大小contentLength=" + contentLength);
                    File f = new File(file, fileName);
                    FileOutputStream fo = new FileOutputStream(f);
                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    LogUtils.logE(TAG, fileName + "---------------------开始下载开启2~");
                    while ((len = in.read(buffer)) > 0) {
                        fo.write(buffer, 0, len);
                    }
                    in.close();
                    fo.flush();
                    fo.close();
                    LogUtils.logE(TAG, "---------------------下载完成了,保存进记录~" + fileName);
                    String allFileName = (String) SPUtil.get(context, SP_SAVE_LOADFILENAME, "");
                    SPUtil.put(context, SP_SAVE_LOADFILENAME, allFileName + fileName + ",");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void logLoadSvg(String content) {
        LogUtils.logE(TAG, content);
    }

    public void downLoadApk(final Context context, final String fileName, final String imgNetUrl, UpdateProgress updateProgress) {
        this.updateProgress = updateProgress;
        final File file = createFileDir(context, "apkfile");
        //本地文件存在,如果下载完成,此下载任务delect
        if (isFileExists(file, fileName)) {
            LogUtils.logE(TAG_LOAD_APK, fileName + "本地存在相同文件安装包");
            long nativeLength = getFileSize(new File(file, fileName));
            LogUtils.logE(TAG_LOAD_APK, fileName + "本地大小nativeLength=" + nativeLength);
            new File(file, fileName).delete();
        }
        //添加任务
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int readTimeout = 300000;
                    int connectTimeout = 300000;
                    URL url = new URL(imgNetUrl);
                    URLConnection conn = url.openConnection();
//                    conn.setUseCaches(true);
                    conn.setConnectTimeout(connectTimeout);
                    conn.setReadTimeout(readTimeout);
                    InputStream in = conn.getInputStream();
                    long contentLength = conn.getContentLength();//总的下载进度
                    LogUtils.logE(TAG_LOAD_APK, fileName + "文件的服务器大小contentLength=" + contentLength);
                    File f = new File(file, fileName);
                    FileOutputStream fo = new FileOutputStream(f);
                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    LogUtils.logE(TAG_LOAD_APK, fileName + "开始下载~");
                    long currentLoad = 0;//当前下载进度
                    while ((len = in.read(buffer)) > 0) {
                        currentLoad += len;
                        fo.write(buffer, 0, len);
                        if (PoolDownLoadUtils.this.updateProgress != null) {
                            int pro = (int) (currentLoad * 100 / contentLength);
                            LogUtils.logE(TAG_LOAD_APK, "下载了=" + currentLoad + "    总共=" + contentLength + "  进度=" + pro);
                            PoolDownLoadUtils.this.updateProgress.updatePro(pro);
                        }
                    }
                    in.close();
                    fo.flush();
                    fo.close();
                    LogUtils.logE(TAG_LOAD_APK, "下载完成了,保存进记录~" + fileName);
                    if (PoolDownLoadUtils.this.updateProgress != null) {
                        PoolDownLoadUtils.this.updateProgress.updateOk(f.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //检查是否存在SD卡
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    //创建目录
    public static File createFileDir(Context context, String dirName) {
        String filePath;
        // 如SD卡已存在，则存储；反之存在data目录下
        if (hasSdcard()) {
            // SD卡路径
            filePath = Environment.getExternalStorageDirectory()
                    + File.separator + dirName;
        } else {
            filePath = context.getCacheDir().getPath() + File.separator
                    + dirName;
        }
        File destDir = new File(filePath);
        if (!destDir.exists()) {
            boolean isCreate = destDir.mkdirs();
            Log.d(TAG, filePath + " has created. " + isCreate);
        }
        return destDir;
    }

    //删除文件（若为目录，则递归删除子目录和文件）
    public static void delFile(File file, boolean delThisPath) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                int num = subFiles.length;
                // 删除子目录和文件
                for (int i = 0; i < num; i++) {
                    delFile(subFiles[i], true);
                }
            }
        }
        if (delThisPath) {
            file.delete();
        }
    }

    //获取文件大小，单位为byte（若为目录，则包括所有子目录和文件）
    public static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if (subFiles != null) {
                    int num = subFiles.length;
                    for (int i = 0; i < num; i++) {
                        size += getFileSize(subFiles[i]);
                    }
                }
            } else {
                size += file.length();
            }
        }
        return size;
    }

    //判断文件是否存在
    public static boolean judgeFielEs(File file) {
        if (file.exists()) {
            return true;
        }
        return false;
    }

    // 判断某目录下文件是否存在
    public static boolean isFileExists(File dir, String fileName) {
        return new File(dir, fileName).exists();
    }

    //--------------------下载进度的更新--------------------
    public interface UpdateProgress {
        void updatePro(int pro);

        void updateOk(String filePath);
    }

    public UpdateProgress updateProgress;
}
