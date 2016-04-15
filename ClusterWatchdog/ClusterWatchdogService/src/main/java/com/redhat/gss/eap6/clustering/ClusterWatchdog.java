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

import java.lang.management.ManagementFactory;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.Message;

/**
 * The Watchdog itself is a {@link javax.ejb.Singleton}.
 * During <code>init()</code> all listener get created and added before connecting the channel. 
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$ $Date$: Date of last commit
 *
 */
@Startup
@Singleton
public class ClusterWatchdog implements ClusterWatchdogMBean {

	private static final Logger log = Logger.getLogger(ClusterWatchdog.class
			.getName());

	private static final String CHANNEL_NAME = JGroupsChannelServiceActivator.CHANNEL_NAME;

	private JgroupsViewChangeReceiverAdapter viewChangeReceiver = new JgroupsViewChangeReceiverAdapter();
	
	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	@Resource(lookup = JGroupsChannelServiceActivator.JNDI_NAME)
	private Channel watchdogChannel;


	@PostConstruct
	protected void init() {

		PropertiesHelper propsHelper = PropertiesHelper.getInstance();

		StringTokenizer changeListenerListTokenizer = new StringTokenizer(
				propsHelper
						.getProperty(PropertiesHelper.WATCHDOG_LISTENER_LIST_KEY),
				",;:");
		while (changeListenerListTokenizer.hasMoreElements()) {
			String className = "N/A";
			Class<?> clazz;
			try {
				className = changeListenerListTokenizer.nextToken();
				clazz = Class.forName(className);
				JgroupsViewChangeListener listenerInstance = (JgroupsViewChangeListener) clazz
						.newInstance();
				viewChangeReceiver
						.registerViewChangeListener(listenerInstance);
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Failed to add ViewChange-listener "
								+ className
								+ ", due to "
								+ e
								+ " It's mandatory to take actions, as the watchdog can't protect your system!");
			}

		}	

		try {
			changeListenerListTokenizer = new StringTokenizer(
					propsHelper
							.getProperty(PropertiesHelper.WATCHDOG_CHANNEL_LISTENER_LIST_KEY),
					",;:");
			while (changeListenerListTokenizer.hasMoreElements()) {
				String className = "N/A";
				Class<?> clazz;
				try {
					className = changeListenerListTokenizer.nextToken();
					clazz = Class.forName(className);
					ChannelListener listenerInstance = (ChannelListener) clazz
							.newInstance();
					watchdogChannel.addChannelListener(listenerInstance);
					log.log(Level.INFO, "registered Channel-Listener named: " + className);		
				} catch (Exception e) {
					log.log(Level.SEVERE,
							"Failed to add listener "
									+ className
									+ ", due to "
									+ e
									+ " It's mandatory to take actions, as the watchdog can't protect your system!");
				}

			}

			watchdogChannel.setReceiver(viewChangeReceiver);
			watchdogChannel.connect(CHANNEL_NAME);

		} catch (Exception e) {
			log.log(Level.SEVERE,
					"Failed to start watchdog due to: "
							+ e
							+ " It's mandatory to take actions, as the watchdog can't protect your system!");
			stop();
			throw new RuntimeException(
					"Failed to start watchdog due to: "
							+ e
							+ " It's mandatory to take actions, as the watchdog can't protect your system!");
		}

		log.log(Level.INFO, "Cluster Watchdog started");

	}

	@PreDestroy
	public void stop() {
		log.log(Level.INFO, "Cluster Watchdog get stopped!");
		if (null != watchdogChannel) {
			try {
				watchdogChannel.send(new Message().setBuffer(getServerState()));
				if (watchdogChannel.flushSupported()) watchdogChannel.startFlush(true);
			} catch (Exception e) {
				log.log(Level.WARNING, "Failed to send server state to other members due to: "+e);
			}
			viewChangeReceiver.unregisterAllViewChangeListeners();
			watchdogChannel.clearChannelListeners();
			watchdogChannel.setReceiver(null);
			watchdogChannel.close();
		}
	}

	private byte[] getServerState() {
		String serverState = "unknown"; 
		try {
			serverState = (String) mBeanServer.getAttribute(new ObjectName("jboss.as:management-root=server"), "serverState");
		} catch (AttributeNotFoundException | InstanceNotFoundException
				| MalformedObjectNameException | MBeanException
				| ReflectionException e) {
			log.log(Level.WARNING, "Failed to read server state due to: "+e);

		}
		return serverState.getBytes();
	}

	@Override
	public void assumeNormalOperations() {
		if (null != watchdogChannel) {
			try {
				watchdogChannel.send(new Message().setBuffer(ClusterWatchdogMBean.NORMAL_OPERATIONS.getBytes()));
				if (watchdogChannel.flushSupported()) watchdogChannel.startFlush(true);
			} catch (Exception e) {
				log.log(Level.WARNING, "Failed to send request to other members due to: "+e);
			}
		}
		
	}
}
