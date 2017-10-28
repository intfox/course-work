//интерфейс модуля objects
class Objects {
public:

private:

};

class MapObjects {

};

class SpisObjects {
public:

private:

};

class ElemSpisObject {
public:

private:
    VirtualObjects *elem;
    ElemSpisObject *next;
};

class VirtualObjects {
public:
    int getx();
    int gety();
    int checkCollision(int, int);
protected:
    int x;
    int y;
    int size_in_x;
    int size_in_y;
};

class StaticObjects: public VirtualObjects {

};

class DynamicObjects: public VirtualObjects {
public:
    int move();
protected:
    int speed;
    int route;
};

class CarObjects: public DynamicObjects {

};

class HumanObjects: public DynamicObjects {

};

class HouseObjects: public StaticObjects {

};
