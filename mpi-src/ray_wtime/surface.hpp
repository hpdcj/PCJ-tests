#ifndef TSURFACE
#define TSURFACE
#include "vec.hpp"

struct Surface {
	Vec*  color;
	double       kd;
	double       ks;
	double       shine;
	double       kt;
	double       ior;

	Surface() {
		color = new Vec(1, 0, 0);
		kd = 1.0;
		ks = 0.0;
		shine = 0.0;
		kt = 0.0;
		ior = 1.0;
	}

};

#endif

