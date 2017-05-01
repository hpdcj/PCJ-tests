#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <mpi.h>
using namespace std;

MPI_Status status;
int nprocess;
int rank;
int size;
#include "random.hpp"
#include "md.hpp"
#include "particle.hpp"

void JGFsetsize(int _size){
	size = _size;
}

void JGFinitialise(){

	initialise();

}

void JGFapplication() { 
	runiters();
} 


void JGFvalidate(){
	double refval[] = {1731.4306625334357,7397.392307839352,
    13774.625810229074, 46548.77475182352, 372757.1114270106};
	double dev = fabs(ek - refval[size]);
	if (dev > 1.0e-8 ){
		printf("Validation failed\n");
		printf("Kinetic Energy = %g %g %d\n", ek, dev, size);
	}
}

void JGFrun(int size) {
	JGFsetsize(size); 

    JGFinitialise(); 

    MPI_Barrier(MPI_COMM_WORLD);

    double time = MPI_Wtime();
    JGFapplication();
    MPI_Barrier(MPI_COMM_WORLD);

    if (rank==0) {
        time = MPI_Wtime() - time;

        JGFvalidate();

        printf("moldyn[%d]\t%5d\ttime %12.7f\n",
                datasizes[size], nprocess, time);
	}
}

int main(int argc, char *argv[]) {
	MPI_Init(&argc, &argv);

	MPI_Comm_size(MPI_COMM_WORLD, &nprocess);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	if (rank==0) {
		printf("Starting calculations on %d processors\n", nprocess);
	}
	int size=0;
	if (argc==2) {
		if (argv[1][0]=='A') size=0;
		if (argv[1][0]=='B') size=1;
		if (argv[1][0]=='C') size=2;
		if (argv[1][0]=='D') size=3;
		if (argv[1][0]=='E') size=4;
	}

	JGFrun(size);
	MPI_Finalize();
}
