#include <bits/stdc++.h>
using namespace std;

struct Point {
    int x = 0;
    int y = 0;
    Point (int newX, int newY);
};

Point::Point (int newX, int newY){
    x = newX;
    y = newY;
}

int getX (Point pnt) {
    return pnt.x;
}

int getY (Point pnt) {
    return pnt.y;
}

string toString(Point pnt) {
    return "(" + to_string(pnt.x) + "," + to_string(pnt.y) + ")";
}

ostream & operator<<(ostream & os, Point & pnt) {
    os << toString(pnt);
}

int main () {
    Point p1(9,4);
    Point p2(9,4);
    Point p3(3,6);

    cout << "\nP1 = " << p1;
    cout << "\nP2 = " << p2;
    cout << "\nP3 = " << p3;
}