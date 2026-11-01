package ru.is232.lessons;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class LiveScheduleClient {
    public static final String ENDPOINT="https://script.google.com/macros/s/AKfycbwW8yTxVGMvsklnIscEP87grbdxHkWv9-Dvztm00rjhM2xeNFqL-HRtnDZuldpfRVtI/exec";
    public static String download() throws IOException {
        URL url=new URL(ENDPOINT);
        for(int redirects=0;redirects<6;redirects++) {
            if(!"https".equals(url.getProtocol()) || !("script.google.com".equals(url.getHost()) || "script.googleusercontent.com".equals(url.getHost()))) throw new IOException("Unexpected endpoint");
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(10_000); connection.setReadTimeout(10_000); connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false); connection.setRequestProperty("Accept","application/json");
            try {
                int code=connection.getResponseCode();
                if(code>=300 && code<400) {
                    String location=connection.getHeaderField("Location");
                    if(location==null) throw new IOException("Redirect without location");
                    url=new URL(url,location); continue;
                }
                if(code!=200) throw new IOException("HTTP "+code);
                try(InputStream in=connection.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                    byte[] buffer=new byte[4096]; int count,total=0;
                    while((count=in.read(buffer))!=-1) {total+=count;if(total>1_000_000) throw new IOException("Response too large");out.write(buffer,0,count);}
                    return new String(out.toByteArray(),StandardCharsets.UTF_8);
                }
            } finally {connection.disconnect();}
        }
        throw new IOException("Too many redirects");
    }
    private LiveScheduleClient() {}
}
