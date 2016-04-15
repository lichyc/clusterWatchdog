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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * The {@link ClusterWatchsog}'s JGroups {@link ReceiverAdapter}. </BR>
 * it processes view changes and messages from other cluster members.
 * 
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class JgroupsViewChangeReceiverAdapter extends ReceiverAdapter {
	
	private static final Logger log = Logger.getLogger( JgroupsViewChangeReceiverAdapter.class.getName() );
	
	private HashSet<JgroupsViewChangeListener> registeredListeners = new HashSet<JgroupsViewChangeListener>();
	
	private List<Address> clusterMembers;
	private List<Address> membersJoinCluster;
	private List<Address> membersExitCluster;
	private List<Address> membersFailureCluster;
	private List<Address> membersSendShutdownCluster = new ArrayList<Address>();
	
	/** 
	 * up to <link>http://www.jgroups.org/manual/html/user-channel.html#ReceivingViewChanges</link> 
	 * <code>viewAccepted()</code> callback of ReceiverAdapter can be used to get callbacks whenever a cluster membership change occurs. The receiver needs to be set via <code>JChannel.setReceiver(Receiver)</code>.
	 *  
	 *  @see org.jgroups.ReceiverAdapter#viewAccepted(org.jgroups.View)
	 */
	public void viewAccepted(View view) {
		
		updateMemberList(view);
		if ((null != membersFailureCluster) && 0 < membersFailureCluster.size()) {
			log.log(Level.INFO, "Recognized a cluster member FAILURE, so watchdog will engage.");
			Iterator<JgroupsViewChangeListener> registeredListenersIterator = registeredListeners.iterator();
			while(registeredListenersIterator.hasNext()) {
				registeredListenersIterator.next().executeOnFailure(view, membersFailureCluster);
			}
		} 
		if ((null != membersExitCluster) && 0 < membersExitCluster.size()) {
			log.log(Level.INFO, "Recognized a cluster member decrease, so watchdog will engage.");
			Iterator<JgroupsViewChangeListener> registeredListenersIterator = registeredListeners.iterator();
			while(registeredListenersIterator.hasNext()) {
				registeredListenersIterator.next().executeOnExit(view, membersExitCluster);
			}
		} 
		if ((null != membersJoinCluster) && 0 < membersJoinCluster.size())  {
			log.log(Level.INFO, "Recognized a cluster member increase, so watchdog will engage.");
			Iterator<JgroupsViewChangeListener> registeredListenersIterator = registeredListeners.iterator();
			while(registeredListenersIterator.hasNext()) {
				registeredListenersIterator.next().executeOnJoin(view, membersJoinCluster);
			}
		}		
		clusterMembers = view.getMembers();		
	}
	
	private void updateMemberList(View view) {
		
		membersJoinCluster = new ArrayList<Address>();
		membersExitCluster = new ArrayList<Address>();
		membersFailureCluster = new ArrayList<Address>();
		
		if (null != clusterMembers) {
			Iterator<Address> clusterMemberIterator =  clusterMembers.iterator();
			while (clusterMemberIterator.hasNext()) {
				Address clusterMember = (Address) clusterMemberIterator.next();
				if (!view.containsMember(clusterMember))  {
					if(membersSendShutdownCluster.contains(clusterMember)) {
						membersExitCluster.add(clusterMember);
						membersSendShutdownCluster.remove(clusterMember);
					} else {
						membersFailureCluster.add(clusterMember);
					}
				}
			}
			clusterMemberIterator =  view.iterator();
			while (clusterMemberIterator.hasNext()) {
				Address clusterMember = (Address) clusterMemberIterator.next();
				if (!clusterMembers.contains((Address) clusterMember))  membersJoinCluster.add(clusterMember);
			}
		}		
	}

	/**
	 * @param jgroupsViewChangeListener
	 */
	public void registerViewChangeListener(JgroupsViewChangeListener jgroupsViewChangeListener) {
		
		registeredListeners.add(jgroupsViewChangeListener);
		log.log(Level.INFO, "registered ViewChange-Listener named: " + jgroupsViewChangeListener.getName());	
		jgroupsViewChangeListener.executeOnRegistration();
	}
	
	/**
	 * @param jgroupsViewChangeListener
	 */
	public void unregisterViewChangeListener(JgroupsViewChangeListener jgroupsViewChangeListener) {
		
		if (registeredListeners.contains(jgroupsViewChangeListener)) {
			registeredListeners.remove(jgroupsViewChangeListener);
			log.log(Level.INFO, "un-registered ViewChange-Listener named: " + jgroupsViewChangeListener.getName());
			jgroupsViewChangeListener.executeOnUnregistration();
		} else {
			log.log(Level.WARNING, "Not un-registered ViewChange-Listener named: " + jgroupsViewChangeListener.getName() + " due to not registered before!");
		}		
	}
		
	/**
	 * TODO: update as outdated 
	 * By convention the watchdog just send a message, when it's get stopped.</BR>
	 * The message payload is by convention the current server state.
	 * If the state is "stopping" it's a shutdown, so this node will do any processing any more.
	 * On any other server state, we need to assume that something is wrong, to we assume a failure.
	 * 
	 * @see org.jgroups.ReceiverAdapter#receive(org.jgroups.Message)
	 */
	public void receive(Message msg) {
		
		Address sender = msg.getSrc();
		String msgPayload = new String(msg.getBuffer());	
		
		switch (msgPayload) {
		case "stopping":
			log.log(Level.INFO, "Cluster node " + sender +" has send shutdown notification.");
			membersSendShutdownCluster.add(sender);
			break;
			
		case ClusterWatchdogMBean.NORMAL_OPERATIONS:
			log.log(Level.INFO, "Cluster node " + sender +" has send \""+ClusterWatchdogMBean.NORMAL_OPERATIONS+"\" notification.");
			assumeNormalOperations();
			break;

		default:
			log.log(Level.WARNING, "Cluster node " + sender +" has send notification: " + msgPayload +", which is not expected/supported!");
			break;
		}
    }

	/**
	 * process the "assumeNormalOperation"-Mode request.
	 */
	private void assumeNormalOperations() {
		log.log(Level.INFO, "Watchdog was ask to assume normal operation mode.");
		Iterator<JgroupsViewChangeListener> viewChangeListenerIter = registeredListeners.iterator();
		while (viewChangeListenerIter.hasNext()) {
			viewChangeListenerIter.next().assumeNormalOperationsMode();
			
		}
		
	}

	/**
	 * Unregister all currently registered {@code JgroupsViewChangeListener}s.
	 */
	public void unregisterAllViewChangeListeners() {
		Iterator<JgroupsViewChangeListener> viewChangeListenerIter = registeredListeners.iterator();
		while (viewChangeListenerIter.hasNext()) {
			unregisterViewChangeListener((JgroupsViewChangeListener) viewChangeListenerIter.next());
			
		}
		
	}

}
