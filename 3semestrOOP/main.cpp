#include "game/game.h"

int main() {
	int statusGame = 0;
	Game game(10, 10, 10);
	statusGame = game.Render();
	if(statusGame == Game::LOSS) {
		std::cout << "Вы проиграли!" << std::endl;
	} else if(statusGame == Game::WIN) {
		std::cout << "Вы выиграли!" << std::endl;
	} else {
		std::cout << "Вы не доиграли, ало" << std::endl;
	}
}