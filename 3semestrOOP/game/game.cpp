#include "game.h"

//class Game

Game::Game(int quant_x_grid, int quant_y_grid, int quant_bomb) {
	srand(time(NULL));
	this->quant_x_grid = quant_x_grid;
	this->quant_y_grid = quant_y_grid;
	this->quant_bomb = quant_bomb;
	FactoryCell factory_cell;
	int buff_x = 0;
	int buff_y = 0;
	window.create(sf::VideoMode(size_cell_grid * quant_x_grid, size_cell_grid * quant_y_grid), "Game");
	grid = new Cell **[quant_x_grid];
	for(int i = 0; i < quant_x_grid; i++) {
		grid[i] = new Cell *[quant_y_grid];
		for(int j = 0; j < quant_y_grid; j++) {
			grid[i][j] = NULL;
		}
	}
	for(int i = 0; i < quant_bomb; i++) {
		buff_x = rand() % quant_x_grid;
		buff_y = rand() % quant_y_grid;
		grid[buff_x][buff_y] = factory_cell.getCell(FactoryCell::BOMB, buff_x, buff_y);
	}
	for(int i = 0; i < quant_x_grid; i++) {
		for(int j = 0; j < quant_y_grid; j++) {
			if(grid[i][j] == NULL) {
				grid[i][j] = factory_cell.getCell(getQuantBesideBomb(i, j), i, j);
			}
		}
	}
}

int Game::Render() {
	int x_buff = 0;
	int y_buff = 0;
	while (window.isOpen()) {
		while (window.pollEvent(event)) {
			if (event.type == sf::Event::Closed) {
				window.close();
			}
			if (event.type == sf::Event::MouseButtonPressed) {
				x_buff = sf::Mouse::getPosition(window).x;
				y_buff = sf::Mouse::getPosition(window).y;
				for(int i = 0; i < quant_x_grid; i++) {
					for(int j = 0; j < quant_y_grid; j++) {
						if(grid[i][j]->BelongsPointCell(x_buff, y_buff)) {
							changeActiveClearCell(i, j);
						}
					}
				}
			}
		}
		if(getStatusWin() == LOSS) {

		} else if(getStatusWin() == WIN) {

		} else {

		}
		window.clear();
		for(int i = 0; i < quant_y_grid; i++) {
			for(int j = 0; j < quant_y_grid; j++) {
				window.draw(grid[i][j]->Sprite());
			}
		}
		window.display();
    }
    return getStatusWin();
}

int Game::getQuantBesideBomb(int x, int y) {
	int count_bomb;
	if(x > 0 && y > 0) {
		if(grid[x-1][y-1] != NULL) {
			count_bomb += grid[x-1][y-1]->getStatusBomb();
		}
	}
	if(x > 0 && y < quant_y_grid - 1) {
		if(grid[x-1][y+1] != NULL) {
			count_bomb += grid[x-1][y+1]->getStatusBomb();
		}
	}
	if(x < quant_x_grid - 1 && y > 0) {
		if(grid[x+1][y-1] != NULL) {
			count_bomb += grid[x+1][y-1]->getStatusBomb();
		}
	}
	if(x < quant_x_grid - 1 && y < quant_y_grid - 1) {
		if(grid[x+1][y+1] != NULL) {
			count_bomb += grid[x+1][y+1]->getStatusBomb();
		}
	}
	if(x > 0) {
		if(grid[x-1][y] != NULL) {
			count_bomb += grid[x-1][y]->getStatusBomb();
		}
	}
	if(x < quant_x_grid - 1) {
		if(grid[x+1][y] != NULL) {
			count_bomb += grid[x+1][y]->getStatusBomb();
		}
	}
	if(y > 0) {
		if(grid[x][y-1] != NULL) {
			count_bomb += grid[x][y-1]->getStatusBomb();
		}
	}
	if(y < quant_y_grid - 1) {
		if(grid[x][y+1] != NULL) {
			count_bomb += grid[x][y+1]->getStatusBomb();
		}
	}
	return count_bomb;
};

void Game::changeActiveClearCell(int x, int y) {\
	if(grid[x][y]->getStatusActive()) {
		return;
	}
	if(grid[x][y]->ChangeActive()) {
		return;
	}
	if(x > 0 && y > 0) {
		changeActiveClearCell(x-1, y-1);
	}
	if(x > 0 && y < quant_y_grid - 1) {
		changeActiveClearCell(x-1, y+1);
	}
	if(x < quant_x_grid - 1 && y > 0) {
		changeActiveClearCell(x+1, y-1);
	}
	if(x < quant_x_grid - 1 && y < quant_y_grid - 1) {
		changeActiveClearCell(x+1, y+1);
	}
	if(x > 0) {
		changeActiveClearCell(x-1, y);
	}
	if(x < quant_x_grid - 1) {
		changeActiveClearCell(x+1, y);
	}
	if(y > 0) {
		changeActiveClearCell(x, y-1);
	}
	if(y < quant_y_grid - 1) {
		changeActiveClearCell(x, y+1);
	}
	return;
}

int Game::getStatusWin() {
	int count_acitve = 0;
	for(int i = 0; i < quant_x_grid; i++) {
		for(int j = 0; j < quant_y_grid; j++) {
			if(grid[i][j]->getStatusActive()) {
				count_acitve++;
				if(grid[i][j]->getStatusBomb()) {
					return -1;
				}
			}
		}
	}
	if(count_acitve == (quant_x_grid * quant_y_grid - quant_bomb)) {
		return 1;
	} else {
		return 0;
	}
}

//class Cell

bool Cell::BelongsPointCell(int x, int y) {
	if((x > this->x) && (x < this->x + Game::size_cell_grid) && (y > this->y) && (y < this->y + Game::size_cell_grid)) {
		return true;
	} else {
		return false;
	}
}

sf::Sprite Cell::Sprite() {
	return *sprite;
}

Cell::Cell(int x, int y) {
	this->x = x;
	this->y = y;
	if(!passive_texture.loadFromFile("game/undefinde_cell.png")) {
		std::cout << "file undefinde_cell.png not found" << std::endl;
	}
	texture = &passive_texture;
	sprite = new sf::Sprite(*texture);
	sprite->setPosition(x, y);
}


int Cell::ChangeActive() {
	texture = &active_texture;
	sprite->setTexture(*texture);
	return 1;
}

bool Cell::getStatusBomb() {
	return false;
}

bool Cell::getStatusActive() {
	if(texture == &active_texture) {
		return true;
	} else {
		return false;
	}
}

//class CellBomb

CellBomb::CellBomb(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/bomb_cell.png");
}

int CellBomb::ChangeActive() {
	texture = &active_texture;
	sprite->setTexture(*texture);
	return -1;
}

bool CellBomb::getStatusBomb() {
	return true;
}

//class CellClear

CellClear::CellClear(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/clear_cell.png");
}

int CellClear::ChangeActive() {
	texture = &active_texture;
	sprite->setTexture(*texture);
	return 0;
}

//class CellNumber1

CellNumber1::CellNumber1(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/1_cell.png");
}

//class CellNumber2

CellNumber2::CellNumber2(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/2_cell.png");
}

//class CellNumber3

CellNumber3::CellNumber3(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/3_cell.png");

}

//class CellNumber4

CellNumber4::CellNumber4(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/4_cell.png");
}

//class CellNumber5

CellNumber5::CellNumber5(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/5_cell.png");
}

//class CellNumber6

CellNumber6::CellNumber6(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/6_cell.png");
}

//class CellNumber7

CellNumber7::CellNumber7(int x, int y) : Cell(x, y) {
	active_texture.loadFromFile("game/7_cell.png");
}

//class FactoryCell

Cell *FactoryCell::getCell(int status_cell, int x, int y) {
	Cell *cell;
	if(status_cell == BOMB) {
		cell = new CellBomb(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == CLEAR) {
		cell = new CellClear(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 1) {
		cell = new CellNumber1(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 2) {
		cell = new CellNumber2(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 3) {
		cell = new CellNumber3(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 4) {
		cell = new CellNumber4(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 5) {
		cell = new CellNumber5(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 6) {
		cell = new CellNumber6(x * Game::size_cell_grid, y * Game::size_cell_grid);
	} else if(status_cell == 7) {
		cell = new CellNumber7(x * Game::size_cell_grid, y * Game::size_cell_grid);
	}
	return cell;
}