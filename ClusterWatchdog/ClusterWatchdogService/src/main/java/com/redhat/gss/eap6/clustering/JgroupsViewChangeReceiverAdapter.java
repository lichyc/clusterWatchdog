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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgroups.Address;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * 
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class JgroupsViewChangeReceiverAdapter extends ReceiverAdapter {
	
	private static final Logger log = Logger.getLogger( JgroupsViewChangeReceiverAdapter.class.getName() );
	
	private HashSet<JgroupsViewChangeListener> registeredListeners4Split = new HashSet<JgroupsViewChangeListener>();
	private HashSet<JgroupsViewChangeListener> registeredListeners4Join = new HashSet<JgroupsViewChangeListener>();
	
	private List<Address> clusterMembers;
	
	/** 
	 * up to <link>http://www.jgroups.org/manual/html/user-channel.html#ReceivingViewChanges</link> 
	 * <code>viewAccepted()</code> callback of ReceiverAdapter can be used to get callbacks whenever a cluster membership change occurs. The receiver needs to be set via <code>JChannel.setReceiver(Receiver)</code>.
	 *  
	 *  @see org.jgroups.ReceiverAdapter#viewAccepted(org.jgroups.View)
	 */
	public void viewAccepted(View view) {
		if (memberLeftCluster(view)) {
			log.log(Level.INFO, "Recognized a cluster member decrease, so watchdog will engage.");
			Iterator<JgroupsViewChangeListener> registeredListenersIterator = registeredListeners4Split.iterator();
			while(registeredListenersIterator.hasNext()) {
				registeredListenersIterator.next().execute();
			}
		} 
		if (memberJoinedCluster(view))  {
			log.log(Level.INFO, "Recognized a cluster member increase, so watchdog will engage.");
			Iterator<JgroupsViewChangeListener> registeredListenersIterator = registeredListeners4Join.iterator();
			while(registeredListenersIterator.hasNext()) {
				registeredListenersIterator.next().execute();
			}
		}
		
		clusterMembers = view.getMembers();		
	}
	
	private boolean memberLeftCluster(View view) {
		
		boolean memberLeftCluster = false;
		if (null != clusterMembers) {
			Iterator<Address> clusterMemberIterator =  clusterMembers.iterator();
			while (clusterMemberIterator.hasNext()) {
				if (!view.containsMember((Address) clusterMemberIterator.next()))  memberLeftCluster = true;
			}
		}
		
		return memberLeftCluster;
	}
	
	private boolean memberJoinedCluster(View view) {
		
		boolean memberJoinedCluster = false;
		if (null != clusterMembers) {
			Iterator<Address> clusterMemberIterator =  view.iterator();
			while (clusterMemberIterator.hasNext()) {
				if (!clusterMembers.contains((Address) clusterMemberIterator.next()))  memberJoinedCluster = true;
			}
		} else {
			memberJoinedCluster = true;
		}
		return memberJoinedCluster;
	}

	/**
	 * @param jgroupsViewChangeListener
	 */
	public void registerJoinViewChangeListener(JgroupsViewChangeListener jgroupsViewChangeListener) {
		
		registeredListeners4Join.add(jgroupsViewChangeListener);
		log.log(Level.INFO, "registered Join-Listener named: " + jgroupsViewChangeListener.getName());		
	}
	
	/**
	 * @param jgroupsViewChangeListener
	 */
	public void unregisterJoinViewChangeListener(JgroupsViewChangeListener jgroupsViewChangeListener) {
		
		if (registeredListeners4Join.contains(jgroupsViewChangeListener)) {
			registeredListeners4Join.remove(jgroupsViewChangeListener);
			log.log(Level.INFO, "un-registered Join-Listener named: " + jgroupsViewChangeListener.getName());
		} else {
			log.log(Level.WARNING, "Not un-registered Join-Listener named: " + jgroupsViewChangeListener.getName() + " due to not registered before!");
		}		
	}
	
	/**
	 * @param jgroupsViewChangeListener
	 */
	public void registerSplitViewChangeListener(JgroupsViewChangeListener jgroupsViewChangeListener) {
		
		registeredListeners4Split.add(jgroupsViewChangeListener);
		log.log(Level.INFO, "registered Split-Listener named: " + jgroupsViewChangeListener.getName());		
	}
	
	/**
	 * @param jgroupsViewChangeListener
	 */
	public void unregisterSplitViewChangeListener(JgroupsViewChangeListener jgroupsViewChangeListener) {
		
		if (registeredListeners4Split.contains(jgroupsViewChangeListener)) {
			registeredListeners4Split.remove(jgroupsViewChangeListener);
			log.log(Level.INFO, "un-registered vListener named: " + jgroupsViewChangeListener.getName());
		} else {
			log.log(Level.WARNING, "Not un-registered Split-Listener named: " + jgroupsViewChangeListener.getName() + " due to not registered before!");
		}		
	}

}
