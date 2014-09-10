package carstendressler.com.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

/**
 * Created by Carsten on 7/27/2014.
 */
public class SimpleHttpClient {

    private static final String TAG = SimpleHttpClient.class.getSimpleName();

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000; // [msec]
    private static final int DEFAULT_READ_TIMEOUT = 10000; // [msec]

    /**
     * Send HTTP GET request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    public static String httpGet(String url) throws IOException {
        return httpGet(url, DEFAULT_READ_TIMEOUT, null);
    }

    public static String httpGet(String url, List<HTTPHeader> Headers) throws IOException {
        return httpGet(url, DEFAULT_READ_TIMEOUT, Headers);
    }


    /**
     * Send HTTP GET request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @param timeout Request timeout
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    public static String httpGet(String url, int timeout, List<HTTPHeader> Headers) throws IOException {
        HttpURLConnection httpConn = null;
        InputStream inputStream = null;

        // Open connection and input stream
        try {
            final URL _url = new URL(url);
            httpConn = (HttpURLConnection) _url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            httpConn.setReadTimeout(timeout);

            if (Headers != null) {
                for (HTTPHeader header : Headers) {
                    httpConn.addRequestProperty(header.getName(), header.getValue());
                }
            }


            httpConn.connect();

            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
            if (inputStream == null) {
                Log.w(TAG, "httpGet: Response Code Error: " + responseCode
                        + ": " + url);
                throw new IOException("Response Error:" + responseCode);
            }
        } catch (final SocketTimeoutException e) {
            Log.w(TAG, "httpGet: Timeout: " + url);
            throw new IOException();
        } catch (final MalformedURLException e) {
            Log.w(TAG, "httpGet: MalformedUrlException: " + url);
            throw new IOException();
        } catch (final IOException e) {
            Log.w(TAG, "httpGet: " + e.getMessage());
            if (httpConn != null) {
                httpConn.disconnect();
            }
            throw e;
        }

        // Read stream as String
        BufferedReader reader = null;
        try {
            StringBuilder responseBuf = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            int c;
            while ((c = reader.read()) != -1) {
                responseBuf.append((char) c);
            }
            return responseBuf.toString();
        } catch (IOException e) {
            Log.w(TAG, "httpGet: read error: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "IOException while closing BufferedReader");
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "IOException while closing InputStream");
            }
        }
    }

    /**
     * Send HTTP POST request to the indicated url. Then returns response as
     * string.
     *
     * @param url request target
     * @param postData POST body data as string (ex. JSON)
     * @return response as string
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    public static String httpPost(String url, String postData)
            throws IOException {
        return httpPost(url, postData, DEFAULT_READ_TIMEOUT, null);
    }

    public static String httpPost(String url, String postData, List<HTTPHeader> Headers)
            throws IOException {
        return httpPost(url, postData, DEFAULT_READ_TIMEOUT, Headers);
    }


    public static String httpPost(String url, String postData, int timeout, List<HTTPHeader> Headers )
            throws IOException {
        HttpURLConnection httpConn = null;
        OutputStream outputStream = null;
        OutputStreamWriter writer = null;
        InputStream inputStream = null;

        // Open connection and input stream
        try {
            final URL _url = new URL(url);
            httpConn = (HttpURLConnection) _url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            httpConn.setReadTimeout(timeout);
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);

            if (Headers != null) {
                for (HTTPHeader header : Headers) {
                    httpConn.addRequestProperty(header.getName(), header.getValue());
                }
            }

            outputStream = httpConn.getOutputStream();
            writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(postData);
            writer.flush();
            writer.close();
            writer = null;
            outputStream.close();
            outputStream = null;

            httpConn.connect();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
            if (inputStream == null) {
                Log.w(TAG, "httpPost: Response Code Error: " + responseCode
                        + ": " + url);
                throw new IOException("Response Error:" + responseCode);
            }
        } catch (final SocketTimeoutException e) {
            Log.w(TAG, "httpPost: Timeout: " + url);
            throw new IOException();
        } catch (final MalformedURLException e) {
            Log.w(TAG, "httpPost: MalformedUrlException: " + url);
            throw new IOException();
        } catch (final IOException e) {
            Log.w(TAG, "httpPost: IOException: " + e.getMessage());
            if (httpConn != null) {
                httpConn.disconnect();
            }
            throw e;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "IOException while closing OutputStreamWriter");
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "IOException while closing OutputStream");
            }
        }

        // Read stream as String
        BufferedReader reader = null;
        try {
            StringBuilder responseBuf = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            int c;
            while ((c = reader.read()) != -1) {
                responseBuf.append((char) c);
            }
            return responseBuf.toString();
        } catch (IOException e) {
            Log.w(TAG, "httpPost: read error: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "IOException while closing BufferedReader");
            }
        }
    }

}
