#ifndef TVEC
#define TVEC
#include <cmath>

struct Vec {
	/**
	 * The x coordinate
	 */
	double x; 

	/**
	 * The y coordinate
	 */
	double y;

	/**
	 * The z coordinate
	 */
	double z;

	/**
	 * Constructor
	 * @param a the x coordinate
	 * @param b the y coordinate
	 * @param c the z coordinate
	 */
	Vec(double a, double b, double c) {
		x = a;
		y = b;
		z = c;
	}

	/**
	 * Copy constructor
	 */
	Vec(const Vec& a) {
		x = a.x;
		y = a.y;
		z = a.z;
	}
	/**
	 * Default (0,0,0) constructor
	 */
	Vec() {
		x = 0.0;
		y = 0.0; 
		z = 0.0;
	}

	/**
	 * Add a vector to the current vector
	 * @param: a The vector to be added
	 */
	void add(Vec* a) {
		x+=a->x;
		y+=a->y;
		z+=a->z;
	}  

	/**
	 * adds: Returns a new vector such as
	 * new = sA + B
	 */
	static Vec* adds(double s, Vec* a, Vec* b) {
		return new Vec(s * a->x + b->x, s * a->y + b->y, s * a->z + b->z);
	}

	/**
	 * Adds vector such as:
	 * this+=sB
	 * @param: s The multiplier
	 * @param: b The vector to be added
	 */
	void adds(double s,Vec* b){
		x+=s*b->x;
		y+=s*b->y;
		z+=s*b->z;
	}

	/**
	 * Substracs two vectors
	 */
	static Vec* sub(Vec* a, Vec* b) {
		return new Vec(a->x - b->x, a->y - b->y, a->z - b->z);
	}

	/**
	 * Substracts two vects and places the results in the current vector
	 * Used for speedup with local variables -there were too much Vec to be gc'ed
	 * Consumes about 10 units, whether sub consumes nearly 999 units!! 
	 * cf thinking in java p-> 831,832
	 */
	void sub2(Vec* a,Vec* b) {
		this->x=a->x-b->x;
		this->y=a->y-b->y;
		this->z=a->z-b->z;
	}

	static Vec* mult(Vec* a, Vec* b) {
		return new Vec(a->x * b->x, a->y * b->y, a->z * b->z);
	}

	static Vec* cross(Vec* a, Vec* b) {
		return new Vec(a->y*b->z - a->z*b->y,
					a->z*b->x - a->x*b->z,
					a->x*b->y - a->y*b->x);
	}

	static double dot(Vec* a, Vec* b) {
		return a->x*b->x + a->y*b->y + a->z*b->z;
	}

	static Vec* comb(double a, Vec* A, double b, Vec* B) {
		return new Vec(a * A->x + b * B->x,
					a * A->y + b * B->y,
					a * A->z + b * B->z);
	}

	void comb2(double a,Vec* A,double b,Vec* B) {
		x=a * A->x + b * B->x;
		y=a * A->y + b * B->y;
		z=a * A->z + b * B->z;      
	}

	void scale(double t) {
		x *= t;
		y *= t;
		z *= t;
	}

	void negate() {
		x = -x;
		y = -y;
		z = -z;
	}

	double normalize() {
		double len;
		len = sqrt(x*x + y*y + z*z);
		if (len > 0.0) {
			x /= len;
			y /= len;
			z /= len;
		}
		return len;
	}

};

#endif

