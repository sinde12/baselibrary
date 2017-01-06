package hohistar.sinde.baselibrary.utility;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by sinde on 16/3/8.
 */
public abstract class IHttpResponse {

    public abstract int getStatusLineCode() throws IOException;

    public abstract InputStream getContent() throws IOException;

    public abstract byte[] getContentData() throws IOException;

    public abstract String getContentString() throws IOException;

    public HttpURLConnection getBaseConnection(){
        throw  new RuntimeException();
    }

    public HttpResponse getHttpResponse(){
        throw  new RuntimeException();
    }

    public abstract Header[] getHeader(String key);

    public static class Header{

        public String name,value;

        public String getName(){
            return name;
        }

        public String getValue(){
            return value;
        }

    }

}
