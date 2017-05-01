#ifndef TTIMER
#define TTIMER
#include <time.h>

typedef struct timespec timespec;

struct TTimer {
	timespec start_time;
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
		clock_gettime(CLOCK_MONOTONIC, &start_time);
	}
	void stop() {
		timespec stop;
		clock_gettime(CLOCK_MONOTONIC, &stop);
		time += ((double)(stop.tv_sec - start_time.tv_sec)
				+ (double)(stop.tv_nsec - start_time.tv_nsec)/1000000000.);
	}
};

#endif // TTIMER
