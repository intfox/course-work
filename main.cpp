#include <iostream>
#include <cstdlib>
#include <SDL2/SDL.h>
#include <ctime>

using namespace std;



int main() {
    srand(time(NULL));
    SDL_Init(SDL_INIT_EVERYTHING);
    int size_windows_w = 1200, size_windows_h = 800;
    SDL_Window *win = SDL_CreateWindow("Myprj", 0, 0, size_windows_w, size_windows_h, SDL_WINDOW_OPENGL);
    SDL_Renderer *ren = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);
    SDL_Event event;
    bool quit = true;
    while(quit) {
        SDL_SetRenderDrawColor(ren, 255, 255, 255, 255);
	    SDL_RenderClear(ren);
	    SDL_PollEvent(&event);
	    if (event.type == SDL_QUIT) quit = false;
        SDL_RenderPresent(ren);
    }
	SDL_Quit;
}