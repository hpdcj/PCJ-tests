#ifndef TLIGHT
#define TLIGHT
#include "vec.hpp"

struct Light {
	Vec*  pos;
	double       brightness;

	Light() {
	}

	Light(double x, double y, double z, double brightness) {
		this->pos = new Vec(x, y, z);
		this->brightness = brightness;
	}

};

#endif

