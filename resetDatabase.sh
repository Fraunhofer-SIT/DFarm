#!/bin/bash

end=6

for dev in $(seq 1 $end)
do
	ssh -t udoo$dev 'cd /home/udoo/Repositories/d-farm/BEAST-Server/target && rm Server.db*'
	echo "removed db on Udoo$dev" 
done

