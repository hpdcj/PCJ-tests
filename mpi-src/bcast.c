#include <stdio.h>
#include <stdlib.h>
#include <float.h>
#include "mpi.h"

#define NUMBER_OF_TESTS 1

int main(int argc, char *argv[])
{
    double       *buf;
    int          rank;
    int          n;
    double       t1, t2, tmin;
    int          i, j, k, nloop;
    MPI_Status   status;
    MPI_Request  r;
    int          dummy;

    MPI_Init( &argc, &argv );

    MPI_Comm_rank( MPI_COMM_WORLD, &rank );
    int transmit[16] = {
                    1, 10, 100, 1024, 2048, 4096, 8192, 16384,
                    32768, 65536, 131072, 262144, 524288, 1048576, 2097152,
                    4194304
                };
    nloop=100;
    for (i=0; i<16; ++i) {
        n=transmit[i];

        buf = (double *) malloc( n * sizeof(double) );
        if (!buf) {
            fprintf( stderr,
                     "Could not allocate send/recv buffer of size %d\n", n );
            MPI_Abort( MPI_COMM_WORLD, 1 );
        }
        tmin = DBL_MAX;
        for (k=0; k<NUMBER_OF_TESTS; k++) {
                /* Make sure both processes are ready */
                MPI_Barrier(MPI_COMM_WORLD);
                t1 = MPI_Wtime();
                for (j=0; j<nloop; j++) {
                    MPI_Bcast( buf, n, MPI_DOUBLE, 0, MPI_COMM_WORLD );
  //                  MPI_Wait( &r, &status );
                }
                t2 = (MPI_Wtime() - t1) / nloop;
                if (t2 < tmin) tmin = t2;
        }
        if (rank == 0) {
            double rate;
            if (tmin > 0) rate = n * sizeof(double) * 1.0e-6 /tmin;
            else          rate = 0.0;
            printf( "Bcast\t%d\t%f\t%f\t%f\n", n, tmin,  (double) n * sizeof(double) / 1024.0  , rate );
        }
        free( buf );
    }

    MPI_Finalize( );
    return 0;
}

