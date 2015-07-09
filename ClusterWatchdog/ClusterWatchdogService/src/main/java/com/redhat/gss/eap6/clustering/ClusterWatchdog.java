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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
@Startup
@Singleton
public class ClusterWatchdog {
	
	private static final Logger log = Logger.getLogger( ClusterWatchdog.class.getName() );
	
	private JgroupsViewChangeReceiverAdapter viewChangeReceiver = new JgroupsViewChangeReceiverAdapter();
	
	private Channel watchdogChannel;
	
	@PostConstruct
	protected void init() {
		
		PropertiesHelper propsHelper = PropertiesHelper.getInstance();
		
		StringTokenizer changeListenerListTokenizer = new StringTokenizer(propsHelper.getProperty(PropertiesHelper.WATCHDOG_JOIN_LISTENER_LIST_KEY), ",;:");
		while (changeListenerListTokenizer.hasMoreElements()) {
			String className = "N/A";
			Class<?> clazz;
			try {
				className = changeListenerListTokenizer.nextToken();
				clazz = Class.forName(className);
				JgroupsViewChangeListener listenerInstance = (JgroupsViewChangeListener) clazz.newInstance();
				viewChangeReceiver.registerJoinViewChangeListener(listenerInstance);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Failed to add join listener "+className+", due to " +e+ " It's mandatory to take actions, as the watchdog can't protect your system!");
			}
			
		}
		
		changeListenerListTokenizer = new StringTokenizer(propsHelper.getProperty(PropertiesHelper.WATCHDOG_SPLIT_LISTENER_LIST_KEY), ",;:");
		while (changeListenerListTokenizer.hasMoreElements()) {
			String className = "N/A";
			Class<?> clazz;
			try {
				className = changeListenerListTokenizer.nextToken();
				clazz = Class.forName(className);
				JgroupsViewChangeListener listenerInstance = (JgroupsViewChangeListener) clazz.newInstance();
				viewChangeReceiver.registerSplitViewChangeListener(listenerInstance);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Failed to add split listener "+className+", due to " +e+ " It's mandatory to take actions, as the watchdog can't protect your system!");
			}
			
		}
			
		ChannelFactory factory = (ChannelFactory) CurrentServiceContainer.getServiceContainer().getService(ServiceName.of("jboss", "jgroups", "stack")).getService().getValue();		
		try {
			
			watchdogChannel = factory.createChannel("clusterWatchdog");
			
			changeListenerListTokenizer = new StringTokenizer(propsHelper.getProperty(PropertiesHelper.WATCHDOG_CHANNEL_LISTENER_LIST_KEY), ",;:");
			while (changeListenerListTokenizer.hasMoreElements()) {
				String className = "N/A";
				Class<?> clazz;
				try {
					className = changeListenerListTokenizer.nextToken();
					clazz = Class.forName(className);
					ChannelListener listenerInstance = (ChannelListener) clazz.newInstance();
					watchdogChannel.addChannelListener(listenerInstance);
				} catch (Exception e) {
					log.log(Level.SEVERE, "Failed to add listener "+className+", due to " +e+ " It's mandatory to take actions, as the watchdog can't protect your system!");
				}
				
			}
			
			watchdogChannel.setReceiver(viewChangeReceiver);
			watchdogChannel.connect("clusterWatchdog");
					
		} catch (Exception e) {
			log.log(Level.SEVERE,"Failed to start watchdog due to: "+e + " It's mandatory to take actions, as the watchdog can't protect your system!");
			stop();
			throw new RuntimeException("Failed to start watchdog due to: "+e + " It's mandatory to take actions, as the watchdog can't protect your system!");
		}
		
		log.log(Level.INFO, "Cluster Watchdog started");
		
	  }
		
	@PreDestroy
	public void stop() {
		log.log(Level.INFO, "Cluster Watchdog get stopped!");
		if (null != watchdogChannel) {
			watchdogChannel.clearChannelListeners();
			watchdogChannel.close();
		}
	}
	
}
