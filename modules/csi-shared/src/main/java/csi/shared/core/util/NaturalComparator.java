package csi.shared.core.util;

import java.util.Comparator;

/**
 * Re-written natural sort taken from a free javascript implementation. 
 * Reference can be found @http://stackoverflow.com/questions/7270447/java-string-number-comparator
 * 
 */
public class NaturalComparator implements Comparator<String>{


    //TODO: could've just ported the javascript instead of making this translate, 
    //not sure what the difference would be performance-wise
    //Could also look to make a better comparator in general, this was done quickly without analysis
    @Override
    public int compare(String a, String b) {

        int la = a.length();
        int lb = b.length();
        int ka = 0;
        int kb = 0;
        while (true) {
            if (ka == la)
                return kb == lb ? 0 : -1;
            if (kb == lb)
                return 1;
            if (a.charAt(ka) >= '0' && a.charAt(ka) <= '9' && b.charAt(kb) >= '0' && b.charAt(kb) <= '9') {
                int na = 0;
                int nb = 0;
                while (ka < la && a.charAt(ka) == '0')
                    ka++;
                while (ka + na < la && a.charAt(ka + na) >= '0' && a.charAt(ka + na) <= '9')
                    na++;
                while (kb < lb && b.charAt(kb) == '0')
                    kb++;
                while (kb + nb < lb && b.charAt(kb + nb) >= '0' && b.charAt(kb + nb) <= '9')
                    nb++;
                if (na > nb)
                    return 1;
                if (nb > na)
                    return -1;
                if (ka == la)
                    return kb == lb ? 0 : -1;
                if (kb == lb)
                    return 1;

            }
            if (a.charAt(ka) != b.charAt(kb))
                return a.charAt(ka) - b.charAt(kb);
            ka++;
            kb++;
        }
    }

}
