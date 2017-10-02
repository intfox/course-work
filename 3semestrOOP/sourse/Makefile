all: main.o bin
	g++ main.o -o bin/Town

main.o: main.cpp build
	g++ -c main.cpp -o build/main.o

build:
	mkdir build

bin:
	mkdir bin

clear:
	rm -r build
