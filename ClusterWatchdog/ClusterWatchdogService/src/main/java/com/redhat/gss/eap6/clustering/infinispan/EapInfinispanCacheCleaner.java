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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgroups.Channel;

import com.redhat.gss.eap6.clustering.jmx.AbstractJmxChannelListener;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class EapInfinispanCacheCleaner extends AbstractJmxChannelListener {
	
	public static final String HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME = "jboss.infinispan:type=Cache,name=\"entity(repl_sync)\",manager=\"hibernate\",component=Cache";
	public static final String INFINISPAN_CACHE_CLEAN_OPERATIONNAME = "clean";
	
	private static final Logger log = Logger.getLogger( EapInfinispanCacheCleaner.class.getName() );
	
	private boolean wasDisconnected = false;
	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelConnected(org.jgroups.Channel)
	 */
	@Override
	public void channelConnected(Channel channel) {
		if(wasDisconnected) {
			log.log(Level.INFO, ("This is a reconnect, so we need clean the caches"));
			try {
				callOperation(HIBERNATE_2ND_LEVEL_CACHE_OBJECTNAME, INFINISPAN_CACHE_CLEAN_OPERATIONNAME, new Object[] { }, new String[] { });
			} catch (Exception e) {
				log.log(Level.SEVERE, "System wasn't able to react as expected on cluster failure! Manual admin action required! " +e);
			}		
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
	
}
