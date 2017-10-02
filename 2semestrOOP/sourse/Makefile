all: main.o
	g++ main.o -o bin/Town

main.o: main.cpp build
	g++ -c main.cpp -o build/main.o

build:
	mkdir build
