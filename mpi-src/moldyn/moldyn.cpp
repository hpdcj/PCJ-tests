#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <mpi.h>
using namespace std;
#include "timer.hpp"

TTimer trun,ttotal;
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

	MPI_Barrier(MPI_COMM_WORLD);
	if(rank==0) {
		trun.start();
		//      JGFInstrumentor.startTimer("Section3:MolDyn:Run");  
	}

	runiters();

	MPI_Barrier(MPI_COMM_WORLD);
	if(rank==0) {
		trun.stop();
		//      JGFInstrumentor.stopTimer("Section3:MolDyn:Run");  
	}
} 


void JGFvalidate(){
	double refval[] = {1731.4306625334357,7397.392307839352,
    13774.625810229074, 46548.77475182352, 110502.46709547556,
    0,0,0};
	double dev = fabs(ek - refval[size]);
	if (dev > 1.0e-10 ){
		printf("Validation failed\n");
		printf("Kinetic Energy = %g %g %d\n", ek, dev, size);
	}
}

void JGFrun(int size) {

	if(rank==0) {
		//      JGFInstrumentor.addTimer("Section3:MolDyn:Total", "Solutions",size);
		//      JGFInstrumentor.addTimer("Section3:MolDyn:Run", "Interactions",size);
	}

	JGFsetsize(size); 

	if(rank==0) {
		ttotal.start();
		//      JGFInstrumentor.startTimer("Section3:MolDyn:Total");
	}

	JGFinitialise(); 
	JGFapplication(); 
    if (rank==0) {
	   JGFvalidate(); 
    }

	if(rank==0) {
		ttotal.stop();
		//      JGFInstrumentor.stopTimer("Section3:MolDyn:Total");

		trun.addops((double) interactions);
		ttotal.addops(1);

		printf("Section3:MolDyn:Run:Size%c\t%f (s) \t %f \t (ops/s)\n",size+'A',trun.time,trun.perf());
		printf("Section3:MolDyn:Total:Size%c\t%f (s) \t %f \t (ops/s)\n",size+'A',ttotal.time,ttotal.perf());
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
		if (argv[1][0]=='F') size=5;
		if (argv[1][0]=='G') size=6;
		if (argv[1][0]=='H') size=7;
	}

	JGFrun(size);
	MPI_Finalize();
}
