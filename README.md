# OpenflowController
This project implements a simple Java based openflow controller which connects with a OVS switch and builds a routing table.

Below are steps the files handles when a connection request is made by the switch

- The Controller.java file listens on port 5555 for new connections from the switch
