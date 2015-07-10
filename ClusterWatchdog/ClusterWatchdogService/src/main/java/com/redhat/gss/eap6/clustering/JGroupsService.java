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

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.Value;
import org.jgroups.Channel;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class JGroupsService implements Service<Void>  {
	
	private final Value<Channel> channel;
    private final String clusterName;

    private final Logger log = Logger.getLogger(JGroupsService.class.getName());

    public JGroupsService(Value<Channel> aChannel, String aClusterName) {
        this.channel = aChannel;
        this.clusterName = aClusterName;
    }

    @Override
    public void start(StartContext aContext) throws StartException {
        log.log(Level.INFO, "Starting JGroups channel: {0}", channel.getValue());

        try {
            channel.getValue().connect(clusterName);
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext aContext) {
        try {
            channel.getValue().close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while stopping service", e);
        }
    }

    @Override
    public Void getValue() {
        return null;
    }

}
