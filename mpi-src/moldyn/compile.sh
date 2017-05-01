#!/bin/bash
. /vosoft/modules/Modules/3.2.8/init/bash
for i in mpich2 # openmpi mpich2 pgi
do
    module load $i
    mpicxx -lm -lrt -O2 -o $i\_moldyn.exe moldyn.cpp
    module unload $i 
done
