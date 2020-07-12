#!/bin/bash


end=10
for dev in $(seq 1 $end)
do
	ssh -t udoo$dev 'cd ~/Repositories/d-farm/ && git pull origin develop'
	scp /home/controller/Repositories/d-farm/BEAST-Server/target/BEAST-Server-0.0.1-SNAPSHOT.jar udoo$dev:/home/udoo/Repositories/d-farm/BEAST-Server/target  
done
