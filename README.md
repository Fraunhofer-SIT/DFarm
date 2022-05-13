# DFarm: Massive-Scaling Dynamic Android App Analysis on Real Hardware

This repository contains the DFarm tool as presented in our MobileSoft'2020 paper. With DFarm,
you can scale dynamic app analyses for Android to dozens or even hundreds of devices, all
controlled through a single REST API. DFarm abstracts from the hardware connectivity details
such as device plug'n'play and USB charging. Note that DFarm consists of a software part and
a hardware part. For more details, please refer to the paper.


## How does DFarm work?

DFarm uses a main controller (a desktop PC or server), which interacts with subcontrollers,
usually single-board computers such as the UDOO x86. The Android phones are connected to
the subcontrollers via active USB hubs, while the subcontrollers interact with the main
controller via ethernet. This allows for adding a practically unlimited number of devices
by simply adding more subcontrollers, which in turn bring in the next block of devices.

## Is there a reference installation?

The reference installation is explained in the paper. One main controller, ten subcontrollers,
each with one active USB hub and ten Android phones.

## How can I install DFarm?

The DFarm software is identical for the main controller and the subcontroller. The configuration
file defines the role of the instance (main controller or subcontroller).

## Is there an iOS variant of DFarm?

Not at the moment. However, we are confident that the general concepts of DFarm can be ported
to iOS devices as well, with minor modifications. Feel free to contribute such a ported version.
