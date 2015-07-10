== Cluster Watchdog

If a JEE-AppServer clustering fails, e.g. due to network outage, this can cause potential harm to your application. 

- By default distributed caches get out of sync, so you have to decide whether you can trust/use cache content.
-- How to deal e.g. with Hibernate L2-Cache?
- If the size of an application cluster falls below a threshold a reduction of functionallity might get forced to survive
- and plenty more scenrios

The intention of this watchdog application is to provide a framework and core service to allow an active management on little effort.
The service provides 2 interfaces which can get implemented to react on changes in cluster topology.

- To take action if the number of cluster members change, please implement JgroupsViewChangeListener. 
- To listen to changes on the state of the local node, please implement ChannelListener.

...

 
