#ifndef TISECT
#define TISECT
#include "primitive.hpp"
#include "surface.hpp"

struct Isect {
	double     t;
	int        enter;
	Primitive*    prim;
	Surface*   surf;
	Isect() {
		prim = new Primitive();
		surf = new Surface();
	}
};

#endif
