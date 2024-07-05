package csi.server.business.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import csi.security.Authorization;

public class AuthorizationStub implements Authorization {
	
	String name;
	Set<String> roles;
	
	
	public AuthorizationStub(String name, String... role)
	{
		this.name = name;
		this.roles = new HashSet<String>();
		roles.add( name );
		
		for (String s : role) {
			roles.add( s.toLowerCase() );
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean hasRole(String name) {
		return roles.contains(name.toLowerCase());
	}

	@Override
	public boolean hasAnyRole(String[] roles) {
		for (String s : roles) {
			if( this.roles.contains( s.toLowerCase() ) )
				return true;
		}
		return false;
	}

	@Override
	public boolean hasAllRoles(String[] roles) {
		for( String s : roles ) {
			if( this.roles.contains( s.toLowerCase() ) == false ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getDistinguishedName() {
		return getName();
	}

	@Override
	public void setDistinguishedName(String dn) {
		name = dn;

	}

	@Override
	public Set<String> listRoles() {
		return Collections.unmodifiableSet(roles);
	}

}
