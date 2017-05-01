for i in openmpi mpich2 pgi
do
    module load $i
    mpicxx -lm -lrt -O2 -o $i\_raytracer.exe raytracer.cpp
    module unload $i 
done
