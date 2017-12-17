all: main.o game.o
	g++ main.o game/game.o -g -o main -lsfml-graphics -lsfml-window -lsfml-system

main.o:
	g++ -c -g main.cpp -o main.o

game.o:
	g++ -c -g game/game.cpp -o game/game.o 

clear:
	rm game/game.o
	rm main.o
	rm main