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

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jgroups.Channel;
import org.jgroups.ChannelListener;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class EapInfinispanCacheCleaner implements ChannelListener {
	
	private static final Logger log = Logger.getLogger( EapInfinispanCacheCleaner.class.getName() );
	
	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	private boolean wasDisconnected = false;
	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelConnected(org.jgroups.Channel)
	 */
	@Override
	public void channelConnected(Channel channel) {
		if(wasDisconnected) {
			log.log(Level.INFO, ("This is a reconnect, so we need clean the caches"));
			executeCleanViaJmx("jboss.infinispan:type=Cache,name=\"repl(repl_async)\",manager=\"ejb34\",component=Cache");
			wasDisconnected = false;
		}

	}

	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelDisconnected(org.jgroups.Channel)
	 */
	@Override
	public void channelDisconnected(Channel channel) {
		wasDisconnected = true;

	}

	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelClosed(org.jgroups.Channel)
	 */
	@Override
	public void channelClosed(Channel channel) {
		wasDisconnected = false;

	}
	
	private void executeCleanViaJmx(String objectName) {
		log.entering(this.getClass().getName(),"execute");
		
		try {
			mBeanServer.invoke(new ObjectName(objectName), "clear", new Object[] { }, new String[] { });
		} catch (InstanceNotFoundException e) {
			log.log(Level.WARNING, "JMX command failed to execute, due to " +e);
		} catch (MalformedObjectNameException e) {
			log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
		} catch (ReflectionException e) {
			log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
		} catch (MBeanException e) {
			log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
		}
		
		log.exiting(this.getClass().getName(),"execute");

	}

}
