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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class PropertiesHelper {
	
	private static final Logger log = Logger.getLogger( PropertiesHelper.class.getName() );
	
	public static final String WATCHDOG_LISTENER_LIST_KEY = "ClusterWatchdog.JgroupsViewChangeListener.List";
	public static final String WATCHDOG_CHANNEL_LISTENER_LIST_KEY = "ClusterWatchdog.ChannelListener.List";
	public static final String WATCHDOG_CHANGE_LISTENER_LIST_VALUE = "com.redhat.gss.eap6.clustering.infinispan.InfinispanEapHibernateSecondLevelCacheClear";
	public static final String JGROUPS_STACK_NAME = "ClusterWatchdog.jgroups.stack.name";
	
	private static PropertiesHelper me;
	
	private Properties myProps = new Properties(); 
	
	private PropertiesHelper() {
		super();
		try {
			myProps.load(PropertiesHelper.class.getResourceAsStream("/clusterWatchdog.properties"));
		} catch (IOException e) {
			log.log(Level.WARNING,"properties file not found, due to "+e);
		}
	}

	public static PropertiesHelper getInstance() {
		
		if(null==me) {
			me = new PropertiesHelper();
		}		
		return me;
	}
	
	public String getProperty(String key) {
		
		String value = System.getProperties().getProperty(key);
		if (null == value) {
			value = myProps.getProperty(key);
			if (null == value) {
				switch (key) {
				case WATCHDOG_LISTENER_LIST_KEY: 
					value = WATCHDOG_CHANGE_LISTENER_LIST_VALUE; 
					break;
				default:
					log.log(Level.WARNING,"No property value found for key: "+key );
				}
			}
		}
		
		return value;
	}
	
}
