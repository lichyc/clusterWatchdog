/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgroups.Address;
import org.jgroups.View;

import com.redhat.gss.eap6.clustering.jmx.AbstractJmxViewChangeListener;

/**
 * Starts/stops the Infinispan Hibernate 2nd-lavel Cache via JMX base on cluster status.
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class InfinispanEapHibernateSecondLevelCacheStartStop extends AbstractJmxViewChangeListener {

	private static final Logger log = Logger.getLogger( InfinispanEapHibernateSecondLevelCacheStartStop.class.getName() );
	
	private static final String HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME = "jboss.infinispan:type=Cache,name=\"entity(repl_sync)\",manager=\"hibernate\",component=Cache";
	private static final String INFINISPAN_CACHE_STOP_OPERATIONNAME = "stop";
	private static final String INFINISPAN_CACHE_START_OPERATIONNAME = "start";
	private static final String INFINISPAN_CACHE_START_ATTRIBUTENAME = "start";
	
	private boolean byDefaultStarted= false;
	
	private List<Address> currentlyMembersInFailure = new ArrayList<Address>();
	
	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.clustering.JgroupsViewChangeListener#executeOnRegistration()
	 */
	@Override
	public void executeOnRegistration() {
		checkIfStartedByDefault();

	}

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.clustering.JgroupsViewChangeListener#executeOnUnregistration()
	 */
	@Override
	public void executeOnUnregistration() {
		if (byDefaultStarted) {
			try {
				callOperation(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_START_OPERATIONNAME, new Object[] { }, new String[] { });
				log.log(Level.INFO, "Hibernate 2nd-level cache is now started what is normal mode.");
			} catch (Exception e) {
				log.log(Level.SEVERE, "System wasn't able start cache! Manual admin action required! " +e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.clustering.JgroupsViewChangeListener#executeOnJoin(org.jgroups.View, java.util.List)
	 */
	@Override
	public void executeOnJoin(View view, List<Address> membersJoinCluster) {
		
		if (currentlyMembersInFailure.isEmpty()) checkIfStartedByDefault();
		
		currentlyMembersInFailure.removeAll(membersJoinCluster);
		if (byDefaultStarted && currentlyMembersInFailure.isEmpty()) {
			try {
				callOperation(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_START_OPERATIONNAME, new Object[] { }, new String[] { });
				log.log(Level.INFO, "Hibernate 2nd-level cache is now started again.");
			} catch (Exception e) {
				log.log(Level.SEVERE, "System wasn't able start cache! Manual admin action required! " +e);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.clustering.JgroupsViewChangeListener#executeOnExit(org.jgroups.View, java.util.List)
	 */
	@Override
	public void executeOnExit(View view, List<Address> membersExitCluster) {
		currentlyMembersInFailure.removeAll(membersExitCluster);
		log.log(Level.INFO, "Recognized that members are graceful leaving. No action required");

	}

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.clustering.JgroupsViewChangeListener#executeOnFailure(org.jgroups.View, java.util.List)
	 */
	@Override
	public void executeOnFailure(View view, List<Address> membersFailureCluster) {
		
		currentlyMembersInFailure.addAll(membersFailureCluster);
		
		try {
			callOperation(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_STOP_OPERATIONNAME, new Object[] { }, new String[] { });
		} catch (Exception e) {
			log.log(Level.SEVERE, "System wasn't able to react as expected on cluster failure! Manual admin action required! " +e);
		}

	}

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.clustering.JgroupsViewChangeListener#assumeNormalOperationsMode()
	 */
	@Override
	public void assumeNormalOperationsMode() {
		if (byDefaultStarted) {
			try {
				callOperation(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_START_OPERATIONNAME, new Object[] { }, new String[] { });
				log.log(Level.INFO, "Hibernate 2nd-level cache is now started.");
			} catch (Exception e) {
				log.log(Level.SEVERE, "System wasn't able start cache! Manual admin action required! " +e);
			}
		} else {
			log.log(Level.FINE, "Hibernate 2nd-level cache is not started by default it will not get started.");
		}
	}
	
	private void checkIfStartedByDefault() {
		try {
			byDefaultStarted = (boolean) readConfigParameter(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_START_ATTRIBUTENAME);
		} catch (Exception e) {
			log.log(Level.WARNING, "System wasn't able check if cache is started due to: " +e);
		}
		
	}

}
