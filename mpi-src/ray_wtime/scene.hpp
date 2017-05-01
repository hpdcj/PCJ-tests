#ifndef TSCENE
#define TSCENE
#include "light.hpp"
#include "primitive.hpp"
#include "view.hpp"

struct Scene {
	vector<Light*> lights;
	vector<Primitive*> objects;
	View* view;  

	void addLight(Light* l)
	{
		this->lights.push_back(l);
	}

	void addObject(Primitive* object)
	{
		this->objects.push_back(object);
	}

	void setView(View* view)
	{
		this->view = view;
	}

	View* getView()
	{
		return this->view;
	}

	Light* getLight(int number)
	{
		return lights[number];
	}

	Primitive* getObject(int number)
	{
		return objects[number];
	}

	int getLights()
	{
		return this->lights.size();
	}

	int getObjects()
	{
		return this->objects.size();
	}

	void setObject(Primitive* object, int pos)
	{
		this->objects[pos]=object;
	}
};
#endif

