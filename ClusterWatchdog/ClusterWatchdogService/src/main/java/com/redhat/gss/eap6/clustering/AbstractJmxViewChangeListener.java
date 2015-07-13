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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jgroups.Address;
import org.jgroups.View;

/**
 * Abstract class as base to call JMX operation when listener get activated. 
 * 
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public abstract class AbstractJmxViewChangeListener implements
		JgroupsViewChangeListener {
	
	private static final Logger log = Logger.getLogger( AbstractJmxViewChangeListener.class.getName() );
	
	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.jgroups.JgroupsViewChangeListener#execute(org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent)
	 */
	@Override
	public void execute(View view, List<Address> membersJoinCluster, List<Address> membersExitCluster) {
		log.entering(this.getClass().getName(),"execute");
		
		try {
			mBeanServer.invoke(new ObjectName(getObjectName()), getOperationName(), getParameters(), getSignature());
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

	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.jgroups.JgroupsViewChangeListener#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getName();
	}
	
	abstract public String getObjectName();
	
	abstract public String getOperationName();
	
	abstract public Object[] getParameters();
	
	abstract public String[] getSignature();
	

}
