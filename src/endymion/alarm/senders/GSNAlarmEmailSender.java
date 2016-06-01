package endymion.alarm.senders;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Nikola on 14.04.2015.
 * This class extends GSNAlarmSender and implements email client
 */
public class GSNAlarmEmailSender extends GSNAlarmSender {

    /**
     * The list of recipients - email addresses
     */
    private List<String> sendToList;

    /**
     * Constructor
     */
    public GSNAlarmEmailSender () {
        sendToList = new ArrayList<String>();
    }

    /**
     * Sets a list of recipients
     * @param parameters - send parameters
     * @throws EndymionException
     */
    @Override
    public void setSendParameters(String... parameters) throws EndymionException {
        if (parameters.length == 1) {
            String[] sendToArray = parameters[0].split(";");
            Collections.addAll(sendToList, sendToArray);

        } else {
            throw new EndymionException("Wrong parameter number!", EndymionLoggerEnum.ERROR);
        }
    }

    /**
     * This method instantiates email client which then sends alarmMessage
     * @param subjectLine - general message (subject line in email)
     * @param alarmMessage - message regarding the concrete alarm
     * @throws EndymionException
     */
    @Override
    public void sendAlarm(String subjectLine, String alarmMessage) throws EndymionException {
        Email email = new SimpleEmail();

        trustAllCertificates();

        try {
            email.setSubject(subjectLine);
            email.setMsg(alarmMessage);
            email.setFrom("endymion.gsn@gmail.com");
            email.setHostName("smtp.gmail.com");
            email.setSmtpPort(587);
            email.setAuthenticator(new DefaultAuthenticator("endymion.gsn@gmail.com", "EndymionGsn2015"));
            email.setSSL(false);
            email.setTLS(true);

            for (String to : sendToList) {
                email.addTo(to);
            }

            email.setDebug(true);
            email.send();

        } catch (Exception e) {
            e.printStackTrace();
            throw new EndymionException (e.getMessage(), EndymionLoggerEnum.ERROR);
        }



    }

    /**
     * Helper function - disables certificate checking in SSL
     */
    void trustAllCertificates () {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}
