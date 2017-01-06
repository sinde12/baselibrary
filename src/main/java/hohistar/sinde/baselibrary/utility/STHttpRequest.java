package hohistar.sinde.baselibrary.utility;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STHttpRequest extends IHttpRequest {

    public  static final String HEADER_CONTENT_TYPE	 		= "Content-type";
    public  static final String HEADER_TYPE_JSON	 		= "application/json";
    public  static final String HEADER_TYPE_JSON_UTF_8	 		= "application/json; charset=UTF-8";

    private static final int CONNECTION_TIMEOUT = 30*1000;
    private static final int SO_TIMEOUT = 30*1000;
    String mRequestMethod = "POST";

    public static class STHttpResponse extends IHttpResponse {

        private HttpResponse mHttpResponse = null;

        private Integer mStatusCode;

        public static int RESPONSE_CODE_UNKNOW_ERRO = 100000;//未知错误

        public static int RESPONSE_CODE_CONNECTING_ERRO = 100001;//连接超时

        public static int RESPONSE_CODE_SOCKET_ERRO = 100002;//服务器返回超时

        STHttpResponse(){}

        public STHttpResponse(HttpResponse response) {
            this.mHttpResponse = response;
        }

        /**
         * the response to the request. This is always a final response, never
         * an intermediate response with an 1xx status code. Whether redirects
         * or authentication challenges will be returned or handled
         * automatically depends on the implementation and configuration of this
         * client.
         * **/
        public HttpResponse getHttpResponse() {
            return mHttpResponse;
        }

        /**
         * Obtains the message entity of this response, if any. The entity is
         * provided by calling setEntity.
         * **/
        public HttpEntity getEntity() {
            return mHttpResponse.getEntity();
        }

        /**
         * Creates a new InputStream object of the entity. It is a programming
         * error to return the same InputStream object more than once. Entities
         * that are not repeatable will throw an exception if this method is
         * called multiple times.
         * **/
        public InputStream getContent() {
            try {
                return getEntity().getContent();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        /**
         *
         * Creates a new byte[] object of the entity. It is a programming error
         * to return the same InputStream object more than once. Entities that
         * are not repeatable will throw an exception if this method is called
         * multiple times.
         * @throws IOException
         * */
        public byte[] getContentData() throws IOException {
            return EntityUtils.toByteArray(getEntity());
        }

        public String getContentString() throws IOException {
            return new String(getContentData());
        }

        /**
         * Returns all the headers of this message. Headers are orderd in the
         * sequence they will be sent over a connection.
         * **/
        public org.apache.http.Header[] getAllHeader() {
            return mHttpResponse.getAllHeaders();
        }
        /**
         * get status of request result
         * **/
        public int getStatusLineCode() {
            if (mStatusCode == null){
                mStatusCode = mHttpResponse == null?RESPONSE_CODE_UNKNOW_ERRO:mHttpResponse.getStatusLine().getStatusCode();
            }
            return mStatusCode;
        }

        public Header[] getHeader(String name) {
            org.apache.http.Header[] headers = mHttpResponse.getHeaders(name);
            if (headers != null){
                Header[] headers1 = new Header[headers.length];
                int index = 0;
                for (org.apache.http.Header header:headers){
                    Header h =new Header();
                    h.name = header.getName();
                    h.value = header.getValue();
                    headers1[index++] = h;
                }
                return headers1;
            }
            return null;
        }

    }

    private final String TAG = "STHttpRequest";

    private HttpRequestBase mHttpRequest;

    private OnRespondListener mOnRespondListener;

    public interface OnRespondListener {
        void onRespond(STHttpRequest request, STHttpResponse response);
    }

    public interface OnHttpRequestStateListener{
        void onHttpRequestStart(STHttpRequest request);
        void OnHttpRequestEnd(STHttpRequest request);
    }

    private static OnHttpRequestStateListener mHttpRequestStateListener;

    private URL mURL;

    public STHttpRequest() {

    }

    public STHttpRequest(String url) throws MalformedURLException {
        if (url != null && !(url.trim()).equalsIgnoreCase(""))
            mURL = new URL(url);
    }

    public STHttpRequest(URL url) {
        mURL = url;
    }

    public String getUrl(){
        return mURL.toString();
    }

    public STHttpResponse startSyncRequest() {
        STHttpResponse response;
        try {
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
            httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT,SO_TIMEOUT);
            for (String key :map.keySet()){
                Object obj= map.get(key);
                httpParams.setParameter(key,obj);
            }
            HttpClient client = new DefaultHttpClient(httpParams);
            if(mHttpRequestStateListener != null)
                mHttpRequestStateListener.onHttpRequestStart(this);
            if (mValuePairs.size() > 0)
                response = post(client);
            else
                response = get(client);
            if(mHttpRequestStateListener != null)
                mHttpRequestStateListener.OnHttpRequestEnd(this);
            Log.e(TAG, "request return!");
        } catch (Exception e) {
            response = new STHttpResponse();
            if (e instanceof HttpHostConnectException){
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO;
            }else if (e instanceof HttpServerConnection || e instanceof SocketTimeoutException){
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_SOCKET_ERRO;
            }else {
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_UNKNOW_ERRO;
            }
            e.printStackTrace();
            if (e.getMessage() != null && mLogDir != null)
                Utility_File.write(("fail connect to "+mURL+"\n"+e.getMessage()).getBytes(), mLogDir.getAbsolutePath()+"/network.log",true);
        }
        return response;
    }

    Map<String,Object> map = new HashMap<String, Object>();
    public Map<String,Object> setParameter(String key, Object obj){
        map.put(key,obj);
        return map;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        map.put(CoreConnectionPNames.CONNECTION_TIMEOUT,timeout);
    }

    @Override
    public void setReadTimeout(int timeout) {
        map.put(CoreConnectionPNames.SO_TIMEOUT,timeout);
    }

    @Override
    public int getConnectTimeout() {
        if (map.containsKey(CoreConnectionPNames.CONNECTION_TIMEOUT)){
            return (Integer) map.get(CoreConnectionPNames.CONNECTION_TIMEOUT);
        }
        return CONNECTION_TIMEOUT;
    }

    @Override
    public int getReadTimeout() {
        if (map.containsKey(CoreConnectionPNames.SO_TIMEOUT)){
            return (Integer) map.get(CoreConnectionPNames.SO_TIMEOUT);
        }
        return SO_TIMEOUT;
    }

    public STHttpResponse startSyncRequest(String postData) {
        STHttpResponse response;
        try {
            HttpParams httpParams = new BasicHttpParams();
            HttpClient client = new DefaultHttpClient(httpParams);
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,CONNECTION_TIMEOUT);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,SO_TIMEOUT);
            for (String key :map.keySet()){
                Object obj= map.get(key);
                client.getParams().setParameter(key,obj);
            }
            if(mHttpRequestStateListener != null)
                mHttpRequestStateListener.onHttpRequestStart(this);
            if (mRequestMethod.equalsIgnoreCase("POST")){
                response = post(client,postData);
            }else {
                response = put(client,postData);
            }
            if(mHttpRequestStateListener != null)
                mHttpRequestStateListener.OnHttpRequestEnd(this);
            Log.e(TAG, "request return!");
        } catch (Exception e) {
            response = new STHttpResponse();
            if (e instanceof HttpHostConnectException){
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO;
            }else if (e instanceof HttpServerConnection || e instanceof SocketTimeoutException){
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_SOCKET_ERRO;
            }else {
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_UNKNOW_ERRO;
            }
            e.printStackTrace();
            if (e.getMessage() != null && mLogDir != null)
                Utility_File.write(("fail connect to "+mURL+"\n"+e.getMessage()).getBytes(), mLogDir.getAbsolutePath()+"/network.log",true);
        }
        return response;
    }

    public STHttpResponse startSyncRequestFile(String filePath) {
        STHttpResponse response;
        try {
            HttpParams httpParams = new BasicHttpParams();
            HttpClient client = new DefaultHttpClient(httpParams);
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,CONNECTION_TIMEOUT);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,SO_TIMEOUT);
            for (String key :map.keySet()){
                Object obj= map.get(key);
                client.getParams().setParameter(key,obj);
            }
            if(mHttpRequestStateListener != null)
                mHttpRequestStateListener.onHttpRequestStart(this);
            response = postFile(client, filePath);
            if(mHttpRequestStateListener != null)
                mHttpRequestStateListener.OnHttpRequestEnd(this);
            Log.e(TAG, "request return!");
        } catch (Exception e) {
            response = new STHttpResponse();
            if (e instanceof HttpHostConnectException){
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO;
            }else if (e instanceof HttpServerConnection || e instanceof SocketTimeoutException){
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_SOCKET_ERRO;
            }else {
                response.mStatusCode = STHttpResponse.RESPONSE_CODE_UNKNOW_ERRO;
            }
            e.printStackTrace();
            if (e.getMessage() != null && mLogDir != null)
                Utility_File.write(("fail connect to "+mURL+"\n"+e.getMessage()).getBytes(), mLogDir.getAbsolutePath()+"/network.log",true);
        }
        return response;
    }

    public boolean abort() {
        if (mHttpRequest != null) {
            mHttpRequest.abort();
            return true;
        }
        return false;
    }

    private STHttpResponse get(HttpClient client) throws URISyntaxException, IOException {
        HttpGet httpGet = new HttpGet(mURL.toURI());
        httpGet.setHeader("Range","bytes="+"");
        mHttpRequest = httpGet;
        for (String key:mHeads.keySet()){
            httpGet.addHeader(key,mHeads.get(key));
//                httpGet.setHeader(key,mHeads.get(key));
        }
//            httpGet.addHeader(HEADER_CONTENT_TYPE,HEADER_TYPE_JSON);
        HttpResponse httpResponse = client.execute(httpGet);
        return new STHttpResponse(httpResponse);
    }

    private STHttpResponse post(HttpClient client) throws Exception {
        HttpPost httpPost = new HttpPost(mURL.toURI());
        for (String key : mHeads.keySet()) {
            httpPost.addHeader(key, mHeads.get(key));
        }
        httpPost.setHeader("Range","bytes="+"");
        HttpEntity entity = getRequestEntity();
        httpPost.setEntity(entity);
        mHttpRequest = httpPost;
        HttpResponse response = client.execute(httpPost);
        return new STHttpResponse(response);
    }

    private STHttpResponse post(HttpClient client, String postData) throws Exception {
        HttpPost httpPost = new HttpPost(mURL.toURI());
        for (String key : mHeads.keySet()) {
            httpPost.addHeader(key, mHeads.get(key));
        }
        HttpEntity entity = new StringEntity(postData, HTTP.UTF_8);
        httpPost.setEntity(entity);
        mHttpRequest = httpPost;
        HttpResponse response = client.execute(httpPost);
        return new STHttpResponse(response);
    }

    private STHttpResponse put(HttpClient client, String putData) throws Exception {
        HttpPut httpPut = new HttpPut(mURL.toURI());
        for (String key : mHeads.keySet()) {
            httpPut.addHeader(key, mHeads.get(key));
        }
        HttpEntity entity = new StringEntity(putData, HTTP.UTF_8);
        httpPut.setEntity(entity);
        mHttpRequest = httpPut;
        HttpResponse response = client.execute(httpPut);
        return new STHttpResponse(response);
    }

    String mPostData = null;
    public void setPostData(String data){
        this.mPostData = data;
    }

    File mPostFile = null;
    public void setPostFile(File file){
        this.mPostFile = file;
    }

    private STHttpResponse postFile(HttpClient client, String filePath) throws Exception {
        HttpPost httpPost = new HttpPost(mURL.toURI());
        for (String key : mHeads.keySet()) {
            httpPost.addHeader(key, mHeads.get(key));
        }
        FileInputStream fis = new FileInputStream(filePath);
        HttpEntity entity = new InputStreamEntity(fis,fis.available());
        httpPost.setEntity(entity);
        mHttpRequest = httpPost;
        HttpResponse response = client.execute(httpPost);
        return new STHttpResponse(response);
    }

    private HttpEntity getRequestEntity() throws JSONException, UnsupportedEncodingException {
        boolean isJson = false;
        for (String key : mHeads.keySet()) {
            if((mHeads.get(key)).equalsIgnoreCase(STHttpRequest.HEADER_TYPE_JSON) || (mHeads.get(key)).equalsIgnoreCase(STHttpRequest.HEADER_TYPE_JSON_UTF_8))
                isJson = true;
        }
        if (isJson) {
            JSONObject jobj = new JSONObject();
            for(NameValuePair valuePair: mValuePairs){
                jobj.put(valuePair.getName(), valuePair.getValue());
            }
            return new StringEntity(jobj.toString(), HTTP.UTF_8);
        }else{
            return new UrlEncodedFormEntity(mValuePairs, HTTP.UTF_8);
        }
    }

    public static String checkResponse(IHttpResponse response){
        String msg = null;
        int status = STHttpRequest.STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO;
        try {
            status = response.getStatusLineCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (status == 404){
            msg = "访问错误";
        } else if (status == 403){
            msg = "账号禁用";
        }else if (status == 500){
            try {
                msg = response.getContentString();
            } catch (IOException e) {
                e.printStackTrace();
                msg = "服务器错误，请联系相关人员";
            }
        }else if (status == STHttpRequest.STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO){
            msg = "请检查网络";
        }else if (status == STHttpRequest.STHttpResponse.RESPONSE_CODE_SOCKET_ERRO){
            msg = "服务器超时";
        }else if (status == STHttpRequest.STHttpResponse.RESPONSE_CODE_UNKNOW_ERRO){
            msg = "未知错误";
        }else if (status > 500 && status <600){
            msg = "服务器错误，请联系相关人员";
        }
        return msg;
    }

    public static String parserResponseCode(int responseCode){
        String msg = null;
        int status = responseCode;
        if (status == 403){
            msg = "账号禁用";
        }else if (status == STHttpRequest.STHttpResponse.RESPONSE_CODE_CONNECTING_ERRO){
            msg = "请检查网络";
        }else if (status == STHttpRequest.STHttpResponse.RESPONSE_CODE_SOCKET_ERRO){
            msg = "服务器超时";
        }else if (status == STHttpRequest.STHttpResponse.RESPONSE_CODE_UNKNOW_ERRO){
            msg = "未知错误";
        }else if (status >= 500 && status <600){
            msg = "服务器错误，请联系相关人员";
        }
        return msg;
    }

    /**
     * collection entity valuePair
     * **/
    private List<NameValuePair> mValuePairs = new ArrayList<NameValuePair>();

    /**
     * if you want to do post request,you could set value to pass
     *
     * @param keyField
     *            param name
     * @param value
     *            param value
     * **/
    public void setEntityValuePair(String keyField, String value) {
        mValuePairs.add(new BasicNameValuePair(keyField, value));
    }

    /**
     * collection head messages
     * **/
    private Map<String, String> mHeads = new HashMap<String, String>();

    /**
     * add a head message
     * **/
    public void addHead(String key, String value) {
        mHeads.put(key, value);
    }

    @Override
    public void setRequestMethod(String method) {

    }

    public void setOnRepondListener(final OnRespondListener listener) {
        this.mOnRespondListener = listener;
    }

    /**
     * this interface is used to listen httpRequest state,usual you can use this abort request which doing.
     * **/
    public static void setOnHttpRequestListener(final OnHttpRequestStateListener listener){
        mHttpRequestStateListener = listener;
    }

    // 默认 HttpRequest
    public static STHttpRequest getDefaultHttpRequest(String url) throws MalformedURLException {
        STHttpRequest request = new STHttpRequest(url);
        request.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30 * 1000);
        request.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30 * 1000);
        request.addHead(STHttpRequest.HEADER_CONTENT_TYPE, STHttpRequest.HEADER_TYPE_JSON_UTF_8);
        return request;
    }


}

