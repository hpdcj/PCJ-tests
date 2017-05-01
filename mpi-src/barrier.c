#include <stdio.h>
#include <stdlib.h>
#include <float.h>
#include "mpi.h"

#define NUMBER_OF_TESTS 10

int main(int argc, char *argv[])
{
    double       *buf;
    int          rank;
    int          nproc;
    int          n;
    double       t1, t2, tmin ;
    int          i, j, k, ntimes;
    MPI_Status   status;
    MPI_Request  r;
    int          dummy;

    MPI_Init(&argc, &argv);

    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &nproc);

    ntimes = 10000;

    tmin  = DBL_MAX;
    for (k=0; k<NUMBER_OF_TESTS; k++) {
        /* Make sure both processes are ready */
        MPI_Barrier(MPI_COMM_WORLD);
        t1 = MPI_Wtime();
        for (j=0; j<ntimes; j++) {
            MPI_Barrier(MPI_COMM_WORLD);
        }
        t2 = (MPI_Wtime() - t1) / ntimes;
        if (t2 < tmin) tmin = t2;
    }

    if (rank == 0) {
        printf("barrier\t%5d\ttime %12.7f\n", nproc, tmin);
    }

    MPI_Finalize();
    return 0;
}

