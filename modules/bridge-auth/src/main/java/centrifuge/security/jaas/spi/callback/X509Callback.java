package centrifuge.security.jaas.spi.callback;

import java.security.cert.X509Certificate;
import java.util.StringTokenizer;

import javax.security.auth.callback.Callback;

public class X509Callback implements Callback {

    static private X509Certificate[] EMPTY = new X509Certificate[0];

    protected X509Certificate[] chain;

    public X509Callback() {
        this.chain = new X509Certificate[0];
    }

    public String getName() {
        return (chain == EMPTY) ? null : getUserNameFromDN(chain[0]);
    }

    public X509Certificate[] getChain() {
        return this.chain;
    }

    public void setChain(X509Certificate[] chain) {
        this.chain = chain;
        if (this.chain == null) {
            this.chain = EMPTY;
        }
    }

    private String getUserNameFromDN(X509Certificate cert) {

        String dn = cert.getSubjectX500Principal().getName();
        String attr = null;
        String work = null;
        boolean hit = false;

        // break the string at commas (have to be careful here because you
        // might have values that contain commas. values like that will be
        // quoted and you'd be able to reconstruct the value so not too much
        // worry)
        StringTokenizer stk = new StringTokenizer(dn, ",");

        while (stk.hasMoreTokens()) {
            attr = (String) stk.nextToken();
            work = attr.toLowerCase().trim();
            // look for the common name attribute
            if (work.startsWith("cn")) {
                hit = true;
                break;
            }
        }

        String uname = null;

        if (hit) {
            int x = attr.indexOf("=");

            if (x > 0) {
                uname = attr.substring(x + 1);
                uname = uname.trim();
            }
        }
        return uname;
    }
}
