#include <iostream>
#include <cstdlib>
#include <SDL2/SDL.h>
#include <ctime>

using namespace std;

struct position {
	int x;
	int y;
};

int vay(position arr[], int size_window, int grid);

int main() {
    srand(time(NULL));
    SDL_Init(SDL_INIT_EVERYTHING);
    int size_windows_w = 800, size_windows_h = 800;
    SDL_Window *win = SDL_CreateWindow("Myprj", 0, 0, size_windows_w, size_windows_h, SDL_WINDOW_OPENGL);
    SDL_Renderer *ren = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);
    SDL_Event event;
	int grid = 10;
	int n_arr_vay = (size_windows_h * size_windows_h) / grid;
	position arr_vay[n_arr_vay];
    bool quit = true;
	int n_vay = vay(arr_vay, size_windows_h, grid);
    while(quit) {
        SDL_SetRenderDrawColor(ren, 255, 255, 255, 255);
	    SDL_RenderClear(ren);
	    SDL_PollEvent(&event);
	    SDL_SetRenderDrawColor(ren, 50, 50, 50, 255);
	    for(int i = 0; i < n_vay; i++) {
		    SDL_RenderDrawLine(ren, arr_vay[i].x, arr_vay[i].y, arr_vay[i+1].x, arr_vay[i+1].y);
	    }
	    if (event.type == SDL_QUIT) quit = false;
        SDL_RenderPresent(ren);
    }
	SDL_Quit;
}

int vay(position arr[], int size_windows, int grid) {
	bool quit = true;
	int i = 0;
	int random_numb;
	arr[i].x = size_windows / 2;
	arr[i].y = size_windows / 2;
	int status = 1 + rand() % 3;
	while(quit) {
		i++;
		random_numb = rand() % 5;
		if (random_numb == 0) {
			if (status == 4) status = 1;
			else status += 1;
		}
		else if(random_numb == 1) {
			if (status == 1) status = 4;
			else status -=1;
		}
		switch (status) {
			case 1:
				arr[i].x = arr[i-1].x + grid * 2;
				arr[i].y = arr[i-1].y;
				break;
			case 2:
				arr[i].x = arr[i-1].x - grid * 2;
				arr[i].y = arr[i-1].y;
				break;
			case 3:
				arr[i].y = arr[i-1].y + grid * 2;
				arr[i].x = arr[i-1].x;
				break;
			case 4:
				arr[i].y = arr[i-1].y - grid * 2;
				arr[i].x = arr[i-1].x;
				break;
		}
		cout << "arr[i].x = " << arr[i].x << " arr[i].y = " << arr[i].y << endl;
		if (arr[i].x > size_windows || arr[i].x < 0 || arr[i].y > size_windows || arr[i].y < 0) quit = false;
	}
	return i;
}