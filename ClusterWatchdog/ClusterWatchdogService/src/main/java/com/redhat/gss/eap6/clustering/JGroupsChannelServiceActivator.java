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

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.clustering.jgroups.subsystem.ChannelService;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ManagedReferenceInjector;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.value.InjectedValue;
import org.jgroups.Channel;

/**
 * thanks to Piotr Nowicki for inventing {@link https://github.com/PiotrNowicki/JGroups_AS7/blob/master/server/src/main/java/com/piotrnowicki/jgroups/JGroupsChannelServiceActivator.java}
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class JGroupsChannelServiceActivator implements ServiceActivator  {
	
	private static final String JNDI_NAME = "java:jboss/channel/watchdogChannel";

    private static final String JGROUPS_CHANNEL_SERVICE_PREFIX = "watchdog.jgroups";

    private static final String STACK_NAME = "udp";

    public static final String CHANNEL_NAME = "clusterWatchdog";

    private ServiceName channelServiceName;

    @Override
    public void activate(ServiceActivatorContext context) {
        channelServiceName = ChannelService.getServiceName(CHANNEL_NAME);

        createChannel(context.getServiceTarget());

        bindChannelToJNDI(context.getServiceTarget());
    }

    void createChannel(ServiceTarget target) {
        InjectedValue<ChannelFactory> channelFactory = new InjectedValue<>();
        ServiceName serviceName = ChannelFactoryService.getServiceName(STACK_NAME);
        ChannelService channelService = new ChannelService(CHANNEL_NAME, channelFactory);

        target.addService(channelServiceName, channelService)
                .addDependency(serviceName, ChannelFactory.class, channelFactory).install();

        // Our own service that will just connect to already configured channel
        ServiceName cService = ServiceName.of(JGROUPS_CHANNEL_SERVICE_PREFIX, CHANNEL_NAME);

        InjectedValue<Channel> channel = new InjectedValue<>();
        target.addService(cService, new JGroupsService(channel, CHANNEL_NAME))
                .addDependency(ServiceBuilder.DependencyType.REQUIRED, ChannelService.getServiceName(CHANNEL_NAME), Channel.class, channel)
                .setInitialMode(ServiceController.Mode.ACTIVE).install();
    }

    void bindChannelToJNDI(ServiceTarget target) {
        ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(JNDI_NAME);

        BinderService binder = new BinderService(bindInfo.getBindName());

        ServiceBuilder<ManagedReferenceFactory> service =
                target.addService(bindInfo.getBinderServiceName(), binder);

        service.addAliases(ContextNames.JAVA_CONTEXT_SERVICE_NAME.append(JNDI_NAME));
        service.addDependency(channelServiceName, Channel.class, new ManagedReferenceInjector<Channel>(
                binder.getManagedObjectInjector()));
        service.addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class,
                binder.getNamingStoreInjector());

        service.setInitialMode(ServiceController.Mode.PASSIVE).install();
    }

}
