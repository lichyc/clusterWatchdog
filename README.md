# Cluster Watchdog for JBoss EAP6

If a JEE-AppServer clustering fails, e.g. due to network outage, this can cause harm to your application. 

- By default distributed caches get out of sync, so you have to decide whether you can trust/use cache content.
- How to deal e.g. with Hibernate L2-Cache?
- If the size of an application cluster falls below a threshold a reduction of functionality might get forced to survive
- and plenty more scenarios

The intention of this watchdog application is to provide a framework and core service to allow an active management on little effort.
The service provides 2 interfaces which can get implemented to react on changes in cluster topology.

- To take action if the number of cluster members change, please implement `JgroupsViewChangeListener`. 
- To listen to changes on the state of the local node, please implement `ChannelListener`.

## Cluster Watchdog Architecture
This basically is an EJB-Jar wrapped by a trivial EAR. So you can use it as a separate deployment-unit or re-use the EJB-Jar inside your application.
The watchdog creates a JGroups channel based on the default JGroups configuration of the JBoss EAP instance it gets deployed into. So it uses the same configuration as used by the web, ejb, inifispan cluster, unless you tweaked the default JBoss configuration.  
Status changes (changeView, disconnect, etc.) on this JGroups channel will trigger events the listener can process. A new channel is used to not predict any established channel or channel name, while reusing the default configuration this channel should behave in-sync with your working channels.  
The Watchdog is not simply using a `org.infinispan.notifications.Listener` as this is limited to view changes. Only on the JGroups level we have a chance to get closer to the reason of a state change, that might be of interest to derive the required action.  

**Please note:** Whether a shutdown or a network issue causes a disconnect of a node can get detected on this node only. This is due to the fact that a ping is used to test the connections. So a node is able to detect whether another node is connected or not. If a node doesn't answer any more, there is no chance from outside to figure out why. Only locally it is possible to check whether the connection is closed (intentionally) or disconnected (failure). Same as applies to a ping on OS level.  

## How to extend
If you like to take action on:
- a member disappearing from the cluster: Implement `com.redhat.gss.eap6.clustering.JgroupsViewChangeListener` and register your class.
- a member joining the cluster: Implement `com.redhat.gss.eap6.clustering.JgroupsViewChangeListener` and register your class.
- local channel status change (connect/disconnect/close): Implement `org.jgroups.ChannelListener` and register your class.
If you like to `clear` an Infinispan cache on disappear &|join of a member, you can extend from `com.redhat.gss.eap6.clustering.AbstractJmxViewChangeListener`.

## How to configure
The configuration of the watchdog is in `clusterWatchdog.properties`. Simply edit to register you classes. The parameters can get overwritten by using `System-Properties`, so feel free to use command-line parameters.

## Example Implementations of Listeners
- `com.redhat.gss.eap6.clustering.LoggingClusterChannelListener`: a trivial implementation of `org.jgroups.ChannelListener`, which simply logs the state changes on the channel fired by JGroups.
- `com.redhat.gss.eap6.clustering.AbstractJmxViewChangeListener`: an implementation of `com.redhat.gss.eap6.clustering.JgroupsViewChangeListener` which invoke a `clear()` operation via JMX on an Infinispan cache.

## How to build
It's a Maven project, so arrest the usual suspects.
