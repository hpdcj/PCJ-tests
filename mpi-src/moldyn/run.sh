#!/bin/bash
. /vosoft/modules/Modules/3.2.8/init/bash
module load mpich2
mpicxx -lrt -lm -O2 -o mpich2_moldyn.exe moldyn.cpp
srun /bin/hostname | sort > all_nodes.txt
cat all_nodes.txt
mpiexec -f=all_nodes.txt ./mpich2_moldyn.exe
