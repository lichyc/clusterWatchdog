/**
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
package com.redhat.gss.eap6.clustering.infinispan;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgroups.Address;
import org.jgroups.View;

import com.redhat.gss.eap6.clustering.jmx.AbstractJmxViewChangeListener;

/**
 * Invoke a <code>clear()</code> via JMX on Infinispan Hibernate 2nd-lavel Cache. 
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class InfinispanEapHibernateSecondLevelCacheClear extends AbstractJmxViewChangeListener {
	private static final Logger log = Logger.getLogger( InfinispanEapHibernateSecondLevelCacheClear.class.getName() );
	
	public static final String HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME = "jboss.infinispan:type=Cache,name=\"entity(repl_sync)\",manager=\"hibernate\",component=Cache";
	public static final String INFINISPAN_CACHE_CLEAN_OPERATIONNAME = "clean";
	
	
	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.jgroups.JgroupsViewChangeListener#getName()
	 */
	public String getName() {
		
		return this.getClass().getName();
	}
	
	@Override
	public void executeOnJoin(View view, List<Address> membersJoinCluster) {
		log.log(Level.INFO, "Recognized that members joined. No action required");
		
	}

	@Override
	public void executeOnExit(View view, List<Address> membersExitCluster) {
		log.log(Level.INFO, "Recognized that members are graceful leaving. No action required");
		
	}

	@Override
	public void executeOnFailure(View view, List<Address> membersFailureCluster) {
		try {
			callOperation(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_CLEAN_OPERATIONNAME, new Object[] { }, new String[] { });
		} catch (Exception e) {
			log.log(Level.SEVERE, "System wasn't able to react as expected on cluster failure! Manual admin action required! " +e);
		}	
	}

	@Override
	public void executeOnRegistration() {
		// empty by intend.
		
	}

	@Override
	public void executeOnUnregistration() {
		// empty by intend.
		
	}

	@Override
	public void assumeNormalOperationsMode() {
		// empty by intend.
		
	}

}
