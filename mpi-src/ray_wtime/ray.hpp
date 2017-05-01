#ifndef TRAY
#define TRAY

struct Ray {
	Vec* P, *D;

	Ray(Vec *pnt, Vec *dir) {
		P = new Vec(pnt->x, pnt->y, pnt->z);
		D = new Vec(dir->x, dir->y, dir->z);
		D->normalize();
	}

	Ray() {
		P = new Vec();
		D = new Vec();
	}

	Vec* point(double t) {
		return new Vec(P->x + D->x * t, P->y + D->y * t, P->z + D->z * t);
	}
};

#endif

