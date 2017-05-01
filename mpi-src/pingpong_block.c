#include <stdio.h>
#include <stdlib.h>
#include <float.h>
#include "mpi.h"

#define NUMBER_OF_TESTS 5

int main(int argc, char *argv[])
{
    double       *buf;
    int          rank;
    int          nproc;
    int          n;
    double       t1, t2, tmin;
    int          i, j, k, ntimes;
    MPI_Status   status;
    MPI_Request  r;
    int          dummy;

    MPI_Init(&argc, &argv);

    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &nproc);

    int transmit[16] = {
                    1, 10, 100, 1024, 2048, 4096, 8192, 16384,
                    32768, 65536, 131072, 262144, 524288, 1048576, 2097152,
                    4194304
                };

    ntimes = 100;
    for (i=0; i<16; ++i) {
        n = transmit[i];

        buf = (double *) malloc(n * sizeof(double));
        if (!buf) {
            fprintf(stderr,
                     "Could not allocate send/recv buffer of size %d\n", n);
            MPI_Abort(MPI_COMM_WORLD, 1);
        }

        tmin = DBL_MAX;
        for (k=0; k<NUMBER_OF_TESTS; k++) {
            if (rank == 0) {
                /* Make sure both processes are ready */
                MPI_Barrier(MPI_COMM_WORLD);
                t1 = MPI_Wtime();
                for (j=0; j<ntimes; j++) {
                    MPI_Send(buf, n, MPI_DOUBLE, 1, k, MPI_COMM_WORLD);
                }
                t2 = (MPI_Wtime() - t1) / ntimes;
                if (t2 < tmin) tmin = t2;
            }
            else if (rank == 1) {
                /* Make sure both processes are ready */
                MPI_Barrier(MPI_COMM_WORLD);
                for (j=0; j<ntimes; j++) {
                    MPI_Recv(buf, n, MPI_DOUBLE, 0, k, MPI_COMM_WORLD, &status);
                }
            }
        }
        if (rank == 0) {
                printf("pingpong\t%5d\tsize %12.7f\tt_send_recv %12.7f\n",
                        nproc, (double) n * sizeof(double) / 1024, tmin);
        }
        free(buf);
    }

    MPI_Finalize();
    return 0;
}

