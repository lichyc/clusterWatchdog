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
package com.redhat.gss.eap6.clustering;

import java.util.List;

import org.jgroups.Address;
import org.jgroups.View;


/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public interface JgroupsViewChangeListener {
	
	/**
	 * Called during registration of the listener.
	 * Basically a chance to initialise during startup. 
	 */
	public void executeOnRegistration();
	
	/**
	 * Called during unregistration of the listener.
	 * Basically a chance cleanup during shutdown. 
	 * <br/><b>NOTE:</b>there is no guarantee that it will called, if the server get killed. 
	 */
	public void executeOnUnregistration();
	
	/**
	 * Called if member is joining the cluster
	 * @param view
	 * @param membersJoinCluster
	 */
	public void executeOnJoin(View view, List<Address> membersJoinCluster);
		
	/**
	 * Called on regular exit of cluster member
	 * @param view
	 * @param membersExitCluster
	 */
	public void executeOnExit(View view, List<Address> membersExitCluster);
	
	/**
	 * Called if a potential failure is detected.
	 * @param view
	 * @param membersFailureCluster
	 */
	public void executeOnFailure(View view, List<Address> membersFailureCluster);
	
	
	/**
	 * Called if {@code WatchDog} is ask to assume normal operations mode.
	 * Implementation should not assume a previous failure.  
	 */
	public void assumeNormalOperationsMode();
	
	/**
	 * Name to be logged during register/un-register
	 * @return
	 */
	public String getName();

}
