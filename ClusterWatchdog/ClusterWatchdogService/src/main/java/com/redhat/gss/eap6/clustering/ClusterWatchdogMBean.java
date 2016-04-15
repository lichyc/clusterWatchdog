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
package com.redhat.gss.eap6.clustering;

/**
 * ClusterWatchdog {@code MBean} interface.
 * 
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public interface ClusterWatchdogMBean {
	
	public static final String NORMAL_OPERATIONS = "assumeNormalOperations";
	
	/**
	 * Ask a cluster via JMX to assume normal operation mode.
	 * <br/>Let's assume in cluster one node had to get hardly killed. 
	 * The {@code ClusterWatchdog} will recognise this as failure of this node.
	 * In case the node can't get restarted to allow a analysis, calling this operation allow all other
	 * node to resume to normal operations mode.
	 */
	public void assumeNormalOperations();

}
