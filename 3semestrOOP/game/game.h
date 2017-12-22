#include <SFML/Graphics.hpp>
#include <iostream>
#include <cstdlib>
#include <ctime>

class Cell {
public:
	Cell(int, int);
	sf::Sprite Sprite();
	virtual int ChangeActive();
	bool BelongsPointCell(int, int);
	virtual bool getStatusBomb();
	bool getStatusActive();
private:
	sf::Texture passive_texture;
	int x;
	int y;
protected:
	sf::Sprite *sprite;
	sf::Texture active_texture;
	sf::Texture *texture;
};

class CellBomb: public Cell {
public:
	CellBomb(int, int);
	int ChangeActive();
	bool getStatusBomb();
};

class CellClear: public Cell {
public:
	CellClear(int, int);
	int ChangeActive();
};

class CellNumber1: public Cell {
public:
	CellNumber1(int, int);
};

class CellNumber2: public Cell {
public:
	CellNumber2(int, int);
};

class CellNumber3: public Cell {
public:
	CellNumber3(int, int);
};

class CellNumber4: public Cell {
public:
	CellNumber4(int, int);
};

class CellNumber5: public Cell {
public:
	CellNumber5(int, int);
};

class CellNumber6: public Cell {
public:
	CellNumber6(int, int);
};

class CellNumber7: public Cell {
public:
	CellNumber7(int, int);
};

class FactoryCell {
public:
	static const int BOMB = -1;
	static const int CLEAR = 0;
	Cell *getCell(int, int, int);
private:

};

class Game {
public:
	Game(int, int, int);
	int Render();
	static const int size_cell_grid = 20;
	static const int WIN = 1;
	static const int LOSS = -1;
private:
	Cell ***grid;
	sf::RenderWindow window;
	sf::Event event;
	int quant_x_grid;
	int quant_y_grid;
	int quant_bomb;
	int getQuantBesideBomb(int, int);
	void changeActiveClearCell(int, int);
	int getStatusWin();
};
