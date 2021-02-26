# Future Work

## Remote device configuration
Long term there is a requirement to build (or repurpose) a User Interface that allows someone to remotely configure devices deployed in the field. There are few requirements/considerations to take into account when designing such a system:

* Would need to build a mechanism to distinctly identify devices in the field so that they can have their configuration updated independently of other devices

* This identification system would have to be meaningful to someone remotely, i.e.  a random number / GUID would satisfy the 'distinct' part of identification, but it would be meaningless to a remote operator. Some options available for how to resolve this issue:

  * Record the device's GPS coordinates when configuring/calibrating the device, allowing a remote operator to overlay device positions on a map. 
  * Have the 'id' for the device be specified by the operator at configuration time, allowing for a 'user friendly' id to be specified (i.e. 'Super-pit West Corner')
  * Integrate with the Grafana dashboards to allow the user to modify a device configuration when reviewing its displacement.

* Need to implement pull-based configuration on the device at specific intervals (could the frequency also be controlled remotely?). Theoretically any and all of the current on-device settings menu could be overwritten remotely (not just frequency) which could lead to so fairly powerful remote management.

* Grafana does provides a mechanism for embedding interactive dashboards into external pages so it can still be used as the primary plotting application; only the administration of new/existing dashboards would need to be conducted in the Grafana application itself.