#ifndef TTIMER
#define TTIMER

struct TTimer {
	double start_time;
	double time;
	double opcount;
	void reset() {
		time = 0.0;
		opcount = 0.0;
	}
	double perf() {
		return opcount/time;
	}
	void addops(double count) {
		opcount += count;
	}
	void start() {
		start_time = MPI_Wtime();
	}
	void stop() {
        time += MPI_Wtime() - start_time;
	}
};

#endif // TTIMER
