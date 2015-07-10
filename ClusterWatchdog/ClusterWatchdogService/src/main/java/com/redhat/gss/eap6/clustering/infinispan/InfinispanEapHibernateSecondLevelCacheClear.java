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

import java.util.logging.Logger;

import com.redhat.gss.eap6.clustering.AbstractJmxViewChangeListener;

/**
 * Invoke a <code>clear()</code> via JMX on Infinispan Hibernate 2-Cache. 
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class InfinispanEapHibernateSecondLevelCacheClear extends AbstractJmxViewChangeListener {
	private static final Logger log = Logger.getLogger( InfinispanEapHibernateSecondLevelCacheClear.class.getName() );
	
	
	
	/* (non-Javadoc)
	 * @see com.redhat.gss.eap6.jgroups.JgroupsViewChangeListener#getName()
	 */
	public String getName() {
		
		return this.getClass().getName();
	}
	
    public String getObjectName() {
    	return "jboss.infinispan:type=Cache,name=\"entity(repl_sync)\",manager=\"hibernate\",component=Cache";
    }
	
    public String getOperationName(){
		return "clear";
	}
	
    public Object[] getParameters(){
		return new Object[] { };
	}
	
    public String[] getSignature(){
		return new String[] { };
	}

}
