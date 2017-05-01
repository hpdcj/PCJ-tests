#ifndef TVIEW
#define TVIEW

struct View {
	Vec       *from;
	Vec        *at;
	Vec        *up;
	double dist;
	double angle;
	double aspect;

	View(){}
	View (Vec* from, Vec* at, Vec* up, double dist, double angle, double aspect)
	{
		this->from = from;
		this->at = at;
		this->up = up;
		this->dist = dist;
		this->angle = angle;
		this->aspect = aspect;               
	}

};

#endif

