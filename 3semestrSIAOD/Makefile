all: main graphics objects bin
	g++ build/main.o build/graphics.o build/objects.o -o bin/Town

main: build
	g++ -c sourse/main.cpp -o build/main.o

graphics: build
	g++ -c sourse/graphics/graphics.cpp -o build/graphics.o

objects: build
	g++ -c sourse/objects/objects.cpp -o build/objects.o

build:
	mkdir build

bin:
	mkdir bin

clear:
	rm -r build
