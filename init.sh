#!/bin/bash

parallel-ssh -i -h /home/controller/.pssh/hostFile -- 'cd /home/udoo && ./startServerOnUdoo.sh' &

