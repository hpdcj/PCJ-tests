#ifndef TPRIMITIVE
#define TPRIMITIVE

struct Isect;
#include "surface.hpp"
//#include "isect.hpp"
#include "vec.hpp"
#include "ray.hpp"

struct Primitive {
    Surface* surf;

    void setColor(double r, double g, double b) {
	surf = new Surface();
        surf->color = new Vec(r, g, b);
    }

    virtual Vec* normal(Vec* pnt){}
    virtual Isect* intersect(Ray* ry){}
    virtual Vec* getCenter(){}
    virtual void setCenter(Vec* c){}
};

#endif
