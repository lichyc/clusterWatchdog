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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgroups.Channel;
import org.jgroups.ChannelListener;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class LoggingClusterChannelListener implements ChannelListener {
	
	private static final Logger log = Logger.getLogger( LoggingClusterChannelListener.class.getName() );

	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelConnected(org.jgroups.Channel)
	 */
	@Override
	public void channelConnected(Channel channel) {
		log.log(Level.INFO, channel.getName() + " was connect.");
		
	}

	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelDisconnected(org.jgroups.Channel)
	 */
	@Override
	public void channelDisconnected(Channel channel) {
		log.log(Level.WARNING, channel.getName() + " was disconnect - failure");
		
	}

	/* (non-Javadoc)
	 * @see org.jgroups.ChannelListener#channelClosed(org.jgroups.Channel)
	 */
	@Override
	public void channelClosed(Channel channel) {
		log.log(Level.INFO, channel.getName() + " was closed - shutdown");

	}

}
