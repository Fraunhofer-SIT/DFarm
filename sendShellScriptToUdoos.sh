#!/bin/bash


end=10
for dev in $(seq 1 $end)
do
	if( $dev == "7" );
	then
		continue
	fi
	scp /home/controller/Repositories/d-farm/startServerOnUdoo.sh udoo$dev:/home/udoo
	ssh -t udoo$dev 'cd /home/udoo && chmod +x startServerOnUdoo.sh'
done
