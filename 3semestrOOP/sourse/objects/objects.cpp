#include "../lib/main.h"

//VirtualObjects

int VirtualObjects::getx() {
    return x;
}

int VirtualObjects::gety() {
    return y;
}

int VirtualObject::checkCollision(int x, int y, int &x_new, int &y_new) {
    if ((x_new < (this->x + size_in_x/2))
    && (x_new > (this->x - size_in_x/2))
    && (y_new < (this->y + size_in_y/2))
    && (y_new > (this->y - size_in_y/2))) {

    }
}
