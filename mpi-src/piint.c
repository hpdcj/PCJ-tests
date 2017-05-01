/* int_pi2.c
 * This simple program approximates pi by computing pi = integral
 * from 0 to 1 of 4/(1+x*x)dx which is approximated by sum 
 * from k=1 to N of 4 / ((1 + (k-1/2)**2 ).  The only input data
 * required is N.

Sources:  http://www.pdc.kth.se/

*/

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <float.h>
#include "mpi.h"

#define f(x) ((double)(4.0/(1.0+x*x)))
#define PI25DT 3.141592653589793238462643 

int mynum;
int nprocs;
MPI_Status status;

double calc(int N) {
	double 	sum, w, x;
	int 	i, info, dest = 0;
	int	type = 2, nbytes = 0, EUI_SUCCEED = 0;

	/* Step (1): get a value for N */
	//MPI_Bcast(N, 1, MPI_INT, 0, MPI_COMM_WORLD);

	/* Step (2): check for exit condition. */
	//if (N <= 0) {
	//	printf("node %d left\n", mynum);
	//	exit(0);
	//}

	/* Step (3): do the computation in N steps
	 * Parallel Version: there are "nprocs" instances participating.  Each
	 * instance should do 1/nprocs of the calculation.  Since we want
	 * i = 1..n but mynum = 0, 1, 2..., we start off with mynum+1.
	 */
	w = 1.0/(double)N;
	sum = 0.0;
	for (i = mynum+1; i <= N; i+=nprocs)
		sum = sum + f(((double)i-0.5)*w);
	sum = sum * w;

	/* Step (4): print the results  
	 * Parallel version: collect partial results and let master instance
	 * print it.
	 */
	if (mynum==0) {
		for (i=1; i<nprocs; i++) {
			info = MPI_Recv(&x, 1, MPI_DOUBLE, i, type, MPI_COMM_WORLD, &status);
			sum=sum+x;
		}
		return sum;
	}
	/* Other instances just send their sum and wait for more input */
	else {
		info = MPI_Send(&sum, 1, MPI_DOUBLE, dest, type, MPI_COMM_WORLD);
		if (info != 0) {
			printf ("instance no, %d failed to send\n", mynum);
			exit(0);
		}
		return 0;
	}
}


main(int argc, char **argv) 
{
    int i;
    double pi = 0.0;
    /* All instances call startup routine to get their instance number (mynum) */
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &mynum);
    MPI_Comm_size(MPI_COMM_WORLD, &nprocs);

    int ntimes = 100;
    int points = 100000000;
    double t1, t2, tmin;

    tmin=DBL_MAX;

    t1 = MPI_Wtime();
    for (i = 1; i < ntimes; ++i) {
        pi = calc(points);
    }
    t2 = (MPI_Wtime() - t1);
    if (t2 < tmin) tmin = t2;

    if (mynum == 0) {
        double err = pi - PI25DT;
        printf("PiInt \t%d\t%f\tpi=%12.7lf,err=%10e\n", nprocs, tmin, pi, err);
    }

    MPI_Finalize();
}

