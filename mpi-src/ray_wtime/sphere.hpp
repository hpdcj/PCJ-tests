#ifndef TSPHERE
#define TSPHERE
#include <cmath>
#include "primitive.hpp"
#include "vec.hpp"
#include "isect.hpp"
#include "surface.hpp"
#include "ray.hpp"

struct Sphere : public Primitive {
	Vec      *c;
	double   r, r2;
	Vec      *v,*b; // temporary vecs used to minimize the memory load

	Sphere(Vec* center, double radius) {
		c = center;
		r = radius;
		r2 = r*r;
		v=new Vec();
		b=new Vec();
	}

	virtual Isect* intersect(Ray* ry) {
		double b, disc, t;
		Isect* ip;
		v->sub2(c, ry->P);
		b = Vec::dot(v, ry->D);
		disc = b*b - Vec::dot(v, v) + r2;
		if (disc < 0.0) {
			return NULL;
		}
		disc = sqrt(disc);
		t = (b - disc < 1e-6) ? b + disc : b - disc;
		if (t < 1e-6) {
			return NULL;
		}
		ip = new Isect();
		ip->t = t;
		ip->enter = Vec::dot(v, v) > r2 + 1e-6 ? 1 : 0;
		ip->prim = this;
		ip->surf = surf;
		return ip;
	}

	virtual Vec* normal(Vec* p) {
		Vec* r;
		r = Vec::sub(p, c);
		r->normalize();
		return r;
	}

	virtual Vec* getCenter() {
		return c;
	}
	virtual void setCenter(Vec* c) {
		this->c = c;
	}
};

#endif

