package example;

import java.util.Calendar;
import java.util.GregorianCalendar;

import csi.security.CsiSecurityManager;
import csi.security.spi.AuthorizationContext;
import csi.security.spi.AuthorizationGuard;

/**
 * This AuthorizationGuard implementation allows access if the current
 * user is an Administrator or the request falls within standard working
 * hours (8 AM through 6 PM).  
 * <p>
 * @author Centrifuge Systems, Inc.
 *
 */
public class AdminOrWorkingHoursGuard
    implements AuthorizationGuard
{

    @Override
    public boolean isAuthorized(AuthorizationContext context) {
        boolean authorized = CsiSecurityManager.isAdmin() || isWorkingHours();
        return authorized;
    }
    
    /**
     * Determines whether the current time is between 8 AM and 6 PM
     * @return
     */
    private boolean isWorkingHours() {
        Calendar now = new GregorianCalendar();
        
        int currentHour = now.get( Calendar.HOUR_OF_DAY );
        
        boolean isWorkingDay = 8 <= currentHour && currentHour <= 18;
        return isWorkingDay;
    }


}
