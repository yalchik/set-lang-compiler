#include <iostream>
#include <conio.h>
#include <set>
#include <string>
#include <sstream>

class Object;
class Set;
class Element;

class Object {
public:
	virtual void print() = 0;
	virtual void read() = 0;
	virtual Set* toSet() = 0;
	virtual Element* toElement() = 0;
	virtual bool toBool() = 0;

	virtual Object* operator + (Object*) = 0;
	virtual Object* operator - (Object*) = 0;
	virtual Object* operator * (Object*) = 0;
	virtual Object* operator == (Object*) = 0;
	virtual Object* operator != (Object*) = 0;
};

class Set : public Object
{
public:
	std::set<Element*> value;
	std::set<Element*>::iterator value_iter;

	Set();
	Set(Set* set);
	Set(const std::set<Element*>&);
	virtual ~Set();
	Set* insert(Object* object);
	Set* remove(Object* object);
	Element* find(Object* object);
	bool contains(Object* object);

	Element* first();
	Element* setFirst();
	Element* last();
	Element* getNext();

	virtual void print();
	virtual void read();
	virtual Set* toSet();
	virtual Element* toElement();
	virtual bool toBool();

	virtual Object* operator + (Object*);
	virtual Object* operator - (Object*);
	virtual Object* operator * (Object*);
	virtual Object* operator == (Object*);
	virtual Object* operator != (Object*);

	static Set* set_union(Set*, Set*);
	static Set* set_difference(Set*, Set*);
	static Set* set_intersection(Set*, Set*);
};

class Element : public Object
{
public:
	double value;

	Element();
	Element(double v);
	Element(int v) { value = static_cast<double>(v); }
	Element(Element* element);

	virtual void print();
	virtual void read();
	virtual Set* toSet();
	virtual Element* toElement();
	virtual bool toBool();

	virtual Object* operator + (Object*);
	virtual Object* operator - (Object*);
	virtual Object* operator * (Object*);
	virtual Object* operator == (Object*);
	virtual Object* operator != (Object*);
};

///////////////////////////////////////////////////////
///// IMPLEMENTATION OF SET //////////////////////////////////
///////////////////////////////////////////////////////
	Set::Set()
	{
	}
	Set::Set(Set* set)
	{
		value = set -> value;
	}
	Set::Set(const std::set<Element*>& set)
	{
		value = set;
	}
	Set::~Set()
	{
		std::set<Element*>::iterator iter;
		for (iter = value.begin(); iter != value.end(); ++iter)
		{
			delete (*iter);
		}
	}
	Set* Set::insert(Object* object)
	{
		Element* elem = find(object);
		if (!elem)
		{
			value.insert(object->toElement());
		}
		return this;
	}
	Set* Set::remove(Object* object)
	{
		Element* elem = find(object);
		if (elem)
		{
			value.erase(elem);
		}
		return this;
	}
	Element* Set::find(Object* object)
	{
		std::set<Element*>::iterator iter;
		for (iter = value.begin(); iter != value.end(); ++iter)
		{
			// мы не может работать напрямую с классом set<Element*>
			// т.к. он содержит ссылки, и они не будут равны, если ссылаются на равные элементы
			if (((*(*iter)) == (object))->toBool())
			{
				return *iter;
			}
		}
		return NULL;
	}
	bool Set::contains(Object* object)
	{
		return true;
	}
	void Set::print()
	{
		std::set<Element*>::iterator iter;
		std::cout << "{";
		for (iter = value.begin(); iter != value.end(); ++iter)
		{
			(*iter)->print();
			std::cout << "\b, ";
		}
		std::cout << "\b\b}" << std::endl;
	}
	void Set::read()
	{
		std::string line;
		getline(std::cin, line);		
		std::istringstream istringstream(line);
		std::string bufferString;
		while(istringstream >> bufferString)
		{
			double bufferValue;
			std::istringstream(bufferString) >> bufferValue;
			value.insert(new Element(bufferValue));
		}
	}

	Element* Set::first()
	{
		return (*value.begin());
	}
	Element* Set::setFirst()
	{
		value_iter = value.begin();
		return (*value_iter);
	}
	Element* Set::last()
	{
		value_iter = value.end();
		return (*value_iter);
	}
	Element* Set::getNext()
	{
		++value_iter;
		if (value_iter == value.end())
		{
			value_iter = value.begin();
		}
		return (*value_iter);
	}


	Object* Set::operator + (Object* o)
	{
		return new Set(set_union(this, o->toSet()));
	}
	Object* Set::operator - (Object* o)
	{
		return new Set(set_difference(this, o->toSet()));
	}
	Object* Set::operator * (Object* o)
	{
		return new Set(set_intersection(this, o->toSet()));
	}
	Object* Set::operator == (Object* o)
	{
		Set* set2 = o->toSet();
		std::set<Element*>::iterator iter;
		// проход по элементам 1-го множества
		for (iter = value.begin(); iter != value.end(); ++iter)
		{
			// если не нашли каждого элемента 1-го множества во 2-м множества
			// то отдаём 0
			if (!set2->find(*iter))
			{
				return new Element(0.0);
			}
		}
		for (iter = set2->value.begin(); iter != set2->value.end(); ++iter)
		{
			// если не нашли каждого элемента 2-го множества в 1-м множестве
			// то отдаём 0
			if (!find(*iter))
			{
				return new Element(0.0);
			}
		}
		// множества равны
		return new Element(1.0);
	}
	Object* Set::operator != (Object* o)
	{
		// инвертируем результат равенста
		return new Element(!((*this) == o)->toBool());
	}	

	Set* Set::toSet()
	{
		return this;
	}

	Element* Set::toElement()
	{
		Element* elem = new Element();
		if (!value.empty())
		{
			elem -> value = (*value.begin()) -> toElement() -> value;
		}
		return elem;
	}

	bool Set::toBool()
	{
		return toElement() -> toBool();
	}

	Set* Set::set_union(Set* s1, Set* s2)
	{
		Set* s3 = new Set(s1);
		std::set<Element*>::iterator iter;
		for (iter = s2->value.begin(); iter != s2->value.end(); ++iter)
		{
			s3->insert(*iter);
		}
		return s3;
	}
	Set* Set::set_difference(Set* s1, Set* s2)
	{
		Set* s3 = new Set(s1);
		std::set<Element*>::iterator iter;
		for (iter = s2->value.begin(); iter != s2->value.end(); ++iter)
		{
			s3->remove(*iter);
		}
		return s3;
	}
	Set* Set::set_intersection(Set* s1, Set* s2)
	{
		return s1;
	}

///////////////////////////////////////////////////////
///// IMPLEMENTATION OF ELEMENT //////////////////////////////
///////////////////////////////////////////////////////
	

	Element::Element()
	{
		value = 0;
	}

	Element::Element(double v)
	{
		value = v;
	}
	Element::Element(Element* element)
	{
		value = element->value;
	}

	void Element::print()
	{
		std::cout << value << ' ';
	}

	void Element::read()
	{
		std::cin >> value;
	}

	Object* Element::operator + (Object* o)
	{
		return new Element(this->value + o->toElement()->value);
	}
	Object* Element::operator - (Object* o)
	{
		return new Element(this->value - o->toElement()->value);
	}
	Object* Element::operator * (Object* o)
	{
		return new Element(this->value * o->toElement()->value);
	}
	Object* Element::operator == (Object* o)
	{
		return new Element(this -> value == o -> toElement() -> value);
	}
	Object* Element::operator != (Object* o)
	{
		return new Element(this -> value != o -> toElement() -> value);
	}

	Set* Element::toSet()
	{
		Set* set = new Set();
		set->insert(new Element(value));
		return set;
	}

	Element* Element::toElement()
	{
		return this;
	}

	bool Element::toBool()
	{
		return value == 0 ? false : true;
	}
