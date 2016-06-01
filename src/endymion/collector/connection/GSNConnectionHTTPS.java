package endymion.collector.connection;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * Created by Nikola on 24.03.2015.
 */
public class GSNConnectionHTTPS extends GSNConnectionHTTP {

    public GSNConnectionHTTPS () {
        super();
        disableSslVerification();
    }

    /**
     *
     * @return "https"
     */
    public String getProtocol () {
        return "https";
    }

    /**
     * This is a helper function and it is used if the server
     * has no valid certificate
     */
    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
