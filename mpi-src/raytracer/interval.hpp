#ifndef TINTERVAL
#define TINTERVAL

struct Interval {
	int number;
	int width;
	int height;
	int yfrom;
	int yto;
	int total;

	Interval(int number, int width, int height, int yfrom, int yto, int total)
	{
		this->number = number;
		this->width = width;
		this->height = height;
		this->yfrom = yfrom;
		this->yto = yto;
		this->total = total;
	}
};

#endif

