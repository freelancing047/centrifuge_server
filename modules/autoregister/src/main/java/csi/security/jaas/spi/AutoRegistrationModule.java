package csi.security.jaas.spi;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import centrifuge.dao.Users;
import centrifuge.dao.jpa.GroupDAOBean;
import centrifuge.license.LicenseManager;
import csi.server.common.identity.Group;
import csi.server.common.identity.User;
import csi.server.common.exception.CentrifugeException;
import centrifuge.ws.rest.services.user.RoleSupport;

import csi.security.jaas.spi.callback.X509Callback;
import csi.server.model.persistence.CsiPersistenceManager;

public class AutoRegistrationModule
        extends SimpleLoginModule
{

    @Override
    public boolean login()
            throws LoginException
    {
        try {
            Callback[] callbacks = new Callback[ 1 ];
            callbacks[ 0 ] = new X509Callback();
            handler.handle( callbacks );
            
            
            X509Callback certCallback = (X509Callback)callbacks[0];
            X509Certificate[] peerChain = certCallback.getChain();
            if( peerChain == null || peerChain.length == 0 ) {
                return false;
            }
            
            
            String userName = certCallback.getName();
            
            User user = Users.findByName( userName );
            if( user == null ) {
                addUser( userName );
                user = Users.findByName( userName );
            }
        } catch( IOException e ) {
            log.warn( "Auto-Register security module encountered an error", e );
            e.printStackTrace();
        } catch( UnsupportedCallbackException e ) {
            log.warn( "Auto-Register security module is not configured properly.  Please verify that there are not multiple copies of the csi-security-jaas.jar installed with your server" );
        }
        
        return false;
    }
    
    private boolean addUser( String username )
    {
        boolean rc = false;
        User user = new User();
        user.setName( username );
        user.setRemark( "Auto-registered" );

        UUID random = UUID.randomUUID();
        user.setPassword( random.toString() );

        try {
            long allowed = LicenseManager.getUserCount();
            long current = Users.getUserCount();

            if( ++current > allowed ) {
                log.warn( "Cannot auto-register user " + username + "; licensed user count (" + allowed
                        + ") exceeded" );
            } else {
                EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
                EntityTransaction txn = entityManager.getTransaction();
                txn.begin();
                Users.add( user );
                
                if( log.isInfoEnabled() ) {
                    log.info( "Auto-registering new user " + username );
                }
                GroupDAOBean bean = new GroupDAOBean();
                Group group = bean.findByName( RoleSupport.AUTHENTICATED_ROLE );
                if( group != null ) {
                    group.getMembers().add( user );
                }
                bean.merge( group );
                
                txn.commit();
                rc = true;
            }
        } catch( CentrifugeException cex ) {
            log.error( "Auto-Registration encountered errors while attempting to add a new user " + username, cex );
        }
        return rc;
    }
    

}
