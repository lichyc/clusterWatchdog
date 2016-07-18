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
package com.redhat.gss.eap6.clustering;

import java.util.List;
import java.util.logging.Logger;

import org.jgroups.Address;
import org.jgroups.View;

/**
 * Worker to process operations on <code>JgroupsViewChangeListener</code> implementations.
 * We spin a new thread to avoid <code>jgroups</code> get blocked by these operations.
 * 
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class JgroupsViewChangeWorkerThread extends Thread {
	
	private static final Logger log = Logger.getLogger( JgroupsViewChangeWorkerThread.class.getName() );
	
	private JgroupsViewChangeListener listener;
	private View view;
	private List<Address> membersCluster;
	private JGroupsEvent event;
	
	
	public JgroupsViewChangeWorkerThread(JgroupsViewChangeListener listener, View view, List<Address> membersCluster, JGroupsEvent event) {
		super ();
		this.listener = listener;
		this.view = view;
		this.membersCluster = membersCluster;
		this.event = event;
		
	}
	
	public void run() {
		
		switch (event) {
		case join:
			listener.executeOnJoin(view, membersCluster);
			break;
			
		case exit:
			listener.executeOnExit(view, membersCluster);
			break;
			
		case failure:
			listener.executeOnFailure(view, membersCluster);
			break;
			
		case assumeNormalOperationsMode:
			listener.assumeNormalOperationsMode();
			break;
		
		default:
			log.severe("Switch to operation failed.");
			break;
		}
	}
	
}