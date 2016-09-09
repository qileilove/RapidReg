package org.unicef.rapidreg.network;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class ConnectionInfo implements Parcelable {

    private final static String TAG = "cadroid.Fetch";

    private Exception exception;
    private X509Certificate[] certificates;

    public ConnectionInfo() { }

    public ConnectionInfo(Exception exception) { this.exception = exception; }

    protected ConnectionInfo(Parcel in) {
    }

    public static ConnectionInfo fetch(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionInfo info = new ConnectionInfo();

        SSLContext sc = SSLContext.getInstance("TLS");
        InfoTrustManager tm = new InfoTrustManager(info);
        sc.init(null, new X509TrustManager[] { tm }, null);

        Log.i(TAG, "Connecting to URL: " + url);

        // Reusing HTTP connections is buggy with versions before Android 4.1 (API Level 16)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            System.setProperty("http.keepAlive", "false");

        HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "CAdroid/1.0.3");
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(20000);
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        SSLSocketFactory socketFactory = sc.getSocketFactory();
        // Before Android 5.0 (Lollipop), the default SSL socket factory doesn't
        // use TLSv1.2 and TLSv1.1 by default, so we have to enable it using our own SSLSocketFactory
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            socketFactory = new TLSSocketFactory(socketFactory);
        urlConnection.setSSLSocketFactory(socketFactory);

        try {
            InputStream in = urlConnection.getInputStream();

            // read one byte to make sure the connection has been established
            int c = in.read();
        } catch(IOException e) {
            String httpStatus = urlConnection.getHeaderField(null);
            if (httpStatus != null)
                Log.i(TAG, "HTTP error when fetching resource: " + httpStatus + " (ignoring)");
            else
                throw e;
        }

        if (info.getCertificates() == null)
            throw new IOException("No certificates found!");

        return info;
    }

    public static final Creator<ConnectionInfo> CREATOR = new Creator<ConnectionInfo>() {
        @Override
        public ConnectionInfo createFromParcel(Parcel in) {
            ConnectionInfo info = new ConnectionInfo();
            info.exception = (Exception)in.readSerializable();
            info.certificates = (X509Certificate[])in.readSerializable();
            return info;
        }

        @Override
        public ConnectionInfo[] newArray(int size) {
            return new ConnectionInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(exception);
        dest.writeSerializable(certificates);
    }

    public Exception getException() {
        return exception;
    }

    public X509Certificate[] getCertificates() {
        return certificates;
    }

    public void setCertificates(X509Certificate[] certificates) {
        this.certificates = certificates;
    }
}
