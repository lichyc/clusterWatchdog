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
package com.redhat.gss.eap6.clustering.jmx;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Impl. of common operations to deal with JMX at single place.
 * 
 * @author clichybi
 *
 */
public abstract class AbstractJmxBasedImpl {
	
	private static final Logger log = Logger.getLogger( AbstractJmxBasedImpl.class.getName() );
	
	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	private class JmxViewChangeWorkerThread extends Thread {
		
		private String objectName;
		private String operationName;
		private Object[] parameters;
		private String[] signature;
		
		public JmxViewChangeWorkerThread(String objectName, String operationName, Object[] parameters, String[] signature) {
			this.objectName = objectName;
			this.operationName = operationName;
			this.parameters = parameters;
			this.signature = signature;
		}
		
		public void run() {		
			log.entering(this.getClass().getName(),"run");
			
			try {
				mBeanServer.invoke(new ObjectName(objectName), operationName, parameters, signature);
			} catch (InstanceNotFoundException e) {
				log.log(Level.WARNING, "JMX command failed to execute, due to " +e);
			} catch (MalformedObjectNameException e) {
				log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
			} catch (ReflectionException e) {
				log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
			} catch (MBeanException e) {
				log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
			}
			
			log.exiting(this.getClass().getName(),"run");
		}
		
	}
	
	/**
	 * A-synchronous operation to invoke an operation via JMX, as it spins of a new thread to make the call.
	 * 
	 * @param objectName
	 * @param operationName
	 * @param parameters
	 * @param signature
	 */
	protected void launchOperation(String objectName, String operationName, Object[] parameters, String[] signature ) {
		new JmxViewChangeWorkerThread(objectName, operationName, parameters, signature).start();
	}
	
	
	/**
	 * Synchronous operation to invoke an operation via JMX.<br/>Please use in case you need the result only as blocking 
	 * impacts jgroups cluster communication. Therefore set to deprecated.
	 * 
	 * @param objectName
	 * @param operationName
	 * @param parameters
	 * @param signature
	 * @return
	 * @throws Exception
	 */
	@Deprecated
    protected Object callOperation(String objectName, String operationName, Object[] parameters, String[] signature ) throws Exception {
		
		try {
			return mBeanServer.invoke(new ObjectName(objectName), operationName, parameters, signature);
		} catch (InstanceNotFoundException e) {
			log.log(Level.WARNING, "JMX command failed to execute, due to " +e);
			return null;
		} catch (MalformedObjectNameException e) {
			log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
			throw e;
		} catch (ReflectionException e) {
			log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
			throw e;
		} catch (MBeanException e) {
			log.log(Level.SEVERE, "JMX command failed to execute, due to " +e);
			throw e;
		}
	}
	
    protected Object readConfigParameter(String objectName, String attributeName) throws Exception {
		return mBeanServer.getAttribute(new ObjectName(objectName), attributeName);
	}
	
    protected void setConfigParameter(String objectName, String attributeName, Object value) throws Exception {
		mBeanServer.setAttribute(new ObjectName(objectName), new Attribute(attributeName, value));
	}

}
