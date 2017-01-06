package hohistar.sinde.baselibrary.utility;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sinde on 16/1/21.
 */
public class STHttpUrlConnection extends IHttpRequest{

    public  static final String HEADER_CONTENT_TYPE	 		= "Content-type";
    public  static final String HEADER_TYPE_JSON	 		= "application/json";
    public  static final String HEADER_TYPE_JSON_UTF_8	 		= "application/json; charset=UTF-8";

    private URL mURL = null;
    private HttpURLConnection mConnection = null;
    private String mRequestMethod = "POST";

    public STHttpUrlConnection(String url) throws MalformedURLException {
        if (url != null && !(url.trim()).equalsIgnoreCase(""))
            mURL = new URL(url);
    }

    public void setUrl(String url) throws MalformedURLException {
        if (url != null && !(url.trim()).equalsIgnoreCase(""))
            mURL = new URL(url);
    }

    public String getUrl(){
        return mURL.toString();
    }

    /**
     * collection head messages
     * **/
    private Map<String, String> mHeads = new HashMap<String, String>();

    /**
     * add a head message
     * **/
    @Override
    public void addHead(String key, String value) {
        mHeads.put(key, value);
    }

    @Override
    public void setRequestMethod(String method) {
        mRequestMethod = method;
    }

    public STConnectionResponse connect() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)mURL.openConnection();
        mConnection = connection;
        for (String key : mHeads.keySet()) {
            connection.setRequestProperty(key, mHeads.get(key));
        }
        connection.setConnectTimeout(mConnectTimeoutMillis);
        connection.setReadTimeout(mSocktTimeoutMillis);
        STConnectionResponse response = null;
        if (!mRequestMethod.equalsIgnoreCase("POST")){
            connection.setRequestMethod(mRequestMethod);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.getOutputStream().flush();
            connection.connect();
            connection.getOutputStream().close();
        }else {
            connection.setRequestMethod("GET");
            connection.connect();
        }
        try {
            response = new STConnectionResponse(connection);
            int code = connection.getResponseCode();
            if (code == 200){
                response.mInputStream = connection.getInputStream();
            }else {
                Log.e("TAG","respond code:"+code);
            }
        }catch (Exception e){
            e.printStackTrace();
            response = new STConnectionResponse(connection);
            if (e instanceof SocketTimeoutException){
                response.mStatusCode = STHttpRequest.STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO;
            }else{
                response.mStatusCode = STHttpRequest.STHttpResponse.RESPONSE_CODE_UNKNOW_ERRO;
            }
        }
        return response;
    }

    public STConnectionResponse connectPostData(byte[] data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)mURL.openConnection();
        mConnection = connection;
        connection.setRequestMethod("POST");
        for (String key : mHeads.keySet()) {
            connection.setRequestProperty(key, mHeads.get(key));
        }
        connection.setDoOutput(true);
        connection.setConnectTimeout(mConnectTimeoutMillis);
        connection.setReadTimeout(mSocktTimeoutMillis);
        connection.getOutputStream().write(data);
        connection.getOutputStream().flush();
        connection.connect();
        connection.getOutputStream().close();
        STConnectionResponse response = new STConnectionResponse(connection);
        response.mInputStream = connection.getInputStream();
        return response;
    }

    public STConnectionResponse connectPosFile(File file) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)mURL.openConnection();
        mConnection = connection;
        connection.setRequestMethod("POST");
        for (String key : mHeads.keySet()) {
            connection.setRequestProperty(key, mHeads.get(key));
        }
        connection.setDoOutput(true);
        connection.setConnectTimeout(mConnectTimeoutMillis);
        connection.setReadTimeout(mSocktTimeoutMillis);
        if (file.exists()){
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[512];
            int length = -1;
            while ((length = fis.read(bytes,0,512)) != -1){
                if (length == 512){
                    connection.getOutputStream().write(bytes);
                }else {
                    byte[] temp = new byte[length];
                    System.arraycopy(bytes,0,temp,0,length);
                }
            }
            connection.getOutputStream().flush();
        }
        connection.connect();
        connection.getOutputStream().close();
        STConnectionResponse response = new STConnectionResponse(connection);
        response.mInputStream = connection.getInputStream();
        return response;
    }

    public STConnectionResponse connectPostData(String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)mURL.openConnection();
        mConnection = connection;
        connection.setRequestMethod(mRequestMethod);
        for (String key : mHeads.keySet()) {
            connection.setRequestProperty(key, mHeads.get(key));
        }
        connection.setConnectTimeout(mConnectTimeoutMillis);
        connection.setReadTimeout(mSocktTimeoutMillis);
        connection.setDoOutput(true);
        connection.getOutputStream().write(data.getBytes());
        connection.getOutputStream().flush();
        connection.connect();
        connection.getOutputStream().close();
        STConnectionResponse response = new STConnectionResponse(connection);
        response.mInputStream = connection.getInputStream();
        return response;
    }

    String mPostData = null;
    public void setPostData(String data){
        this.mPostData = data;
    }

    File mPostFile = null;
    public void setPostFile(File file){
        this.mPostFile = file;
    }

    private int mConnectTimeoutMillis = 60000;
    public void setConnectTimeout(int timeoutMillis){
        this.mConnectTimeoutMillis = timeoutMillis;
    }

    private int mSocktTimeoutMillis = 60000;
    public void setReadTimeout(int timeoutMillis){
        this.mSocktTimeoutMillis = timeoutMillis;
    }

    @Override
    public int getConnectTimeout() {
        return mConnectTimeoutMillis;
    }

    @Override
    public int getReadTimeout() {
        return mSocktTimeoutMillis;
    }

    @Override
    public boolean abort() {
        if (mConnection == null){
            return false;
        }
        mConnection.disconnect();
        return true;
    }

    public static class STConnectionResponse extends IHttpResponse{

        private HttpURLConnection mConnection = null;
        private InputStream mInputStream = null;

        Integer mStatusCode;

        STConnectionResponse(HttpURLConnection connection){
            mConnection = connection;
        }

        public int getResponseCode() throws IOException {
            return mConnection == null?mStatusCode:mConnection.getResponseCode();
        }

        public HttpURLConnection getBaseConnection(){
            return mConnection;
        }

        @Override
        public Header[] getHeader(String key) {
            String value = mConnection.getHeaderField(key);
            if (value != null){
                Header h = new Header();
                h.name = key;
                h.value = value;
                Header[] headers = new Header[1];
                headers[0] = h;
                return headers;
            }
            return null;
        }

        @Override
        public int getStatusLineCode() throws IOException{
            if (mStatusCode == null){
                mStatusCode = mConnection == null?-1:mConnection.getResponseCode();
            }
            return mStatusCode;
        }

        @Override
        public InputStream getContent() throws IOException {
            return mInputStream;
        }

        @Override
        public byte[] getContentData() throws IOException {
            InputStream is = getContent();
            if (is != null){
                ByteArrayOutputStream baos = null;
                try {
                    baos = new ByteArrayOutputStream();
                    int length = 512;
                    int tempLength = -1;
                    byte[] buffer = new byte[length];
                    while ((tempLength = is.read(buffer, 0, length)) != -1){
                        if (tempLength < length) {
                            byte[] temp = new byte[tempLength];
                            System.arraycopy(buffer, 0, temp, 0, tempLength);
                            baos.write(temp);
                        }else
                            baos.write(buffer);
                    }
                    is.close();
                    return baos.toByteArray();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    try {
                        if (baos != null)
                            baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        public String getContentString() throws IOException {
            byte[] data = getContentData();
            if (data != null){
                return new String(data);
            }
            return null;
        }

    }

}
