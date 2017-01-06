package hohistar.sinde.baselibrary.base;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements UncaughtExceptionHandler {

    private static final String TAG_CrashHandler = "TAG_CrashHandler";
    private static CrashHandler mInstance = new CrashHandler();
    private String mPath = null;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {

        return mInstance;
    }

    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;
    private Map<String, Object> mInfos = new HashMap<String, Object>();

    public void init(Context context, String filePath) {

        this.mContext = context;
        this.mPath = filePath;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        String deviceType = android.os.Build.MODEL;
        @SuppressWarnings("deprecation") String sdk = android.os.Build.VERSION.SDK;
        String release = android.os.Build.VERSION.RELEASE;
        mInfos.put("Device Type", deviceType);
        mInfos.put("SDK Version", sdk);
        mInfos.put("android System", release);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (handleException(ex)) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG_CrashHandler, "error : ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        File menuFile = null;
        if (mPath == null) {
            File rootDirectory = Environment.getExternalStorageDirectory();
            menuFile = new File(rootDirectory, mContext.getPackageName());
        } else {
            menuFile = new File(mPath);
        }
        if (!menuFile.exists()) {
            menuFile.mkdirs();
        }
        StringBuilder builder = new StringBuilder();
        File crashFile = new File(menuFile, "crashFile.log");
        try {
            if (!crashFile.exists()) {
                crashFile.createNewFile();
            } else {
                InputStream in = new FileInputStream(crashFile);
                byte[] buffers = new byte[256];
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                while ((in.read(buffers, 0, 256)) != -1) {
                    os.write(buffers);
                }
                os.flush();
                String oldEx = new String(os.toByteArray());
                builder.append(oldEx).append("\n-----------------------\n");
                in.close();
            }
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String exception = writer.toString();
            for (Map.Entry<String, Object> entry : mInfos.entrySet()) {
                String key = entry.getKey();
                String value = (String) entry.getValue();
                builder.append(key).append(":").append(value).append("\n");
            }
            builder.append("time:").append(new Date()).append("\n");
            builder.append(exception);
            String result = builder.toString();
            OutputStream ous = new FileOutputStream(crashFile);
            ous.write(result.getBytes());
            ous.flush();
            ous.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
