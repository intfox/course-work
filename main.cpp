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

void ShakerSort(position a[], int index_x[], int n);

bool found_d(position a[], int index_x[], int n, position cursor);

bool test_move_cursor(position a[], int index_x[], int n, int course, position cursor, int grid);

void game(int size_window_h, SDL_Window* win, SDL_Renderer *ren, SDL_Event event);

int main() {
    srand(time(NULL));
    SDL_Init(SDL_INIT_EVERYTHING);
    int size_windows_w = 600, size_windows_h = 600;
    SDL_Window *win = SDL_CreateWindow("Myprj", 0, 0, size_windows_w, size_windows_h, SDL_WINDOW_OPENGL);
    SDL_Renderer *ren = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);
    SDL_Event event;
	game(size_windows_h, win, ren, event);
	SDL_Quit;
}

int vay(position arr[], int size_windows, int grid) {
	bool quit = true;
	int i = 0;
	int random_numb;
	arr[i].x = size_windows / 2;
	arr[i].y = size_windows / 2;
	int status = 1 + rand() % 3;
	int random_quant = 14 + 1;
	while(quit) {
		random_quant--;
		i += 2;
		random_numb = rand() % 6;
		if (random_numb == 0) {
			if (status == 4) status = 1;
			else status += 1;
			random_quant = 14 + 1;
		}
		else if(random_numb == 1) {
			if (status == 1) status = 4;
			else status -= 1;
			random_quant = 14 + 1;
		}
		switch (status) {
			case 1:
				arr[i-1].x = arr[i-2].x + grid;
				arr[i-1].y = arr[i-2].y;
				arr[i].x = arr[i-2].x + grid * 2;
				arr[i].y = arr[i-2].y;
				break;
			case 2:
				arr[i-1].x = arr[i-2].x - grid;
				arr[i-1].y = arr[i-2].y;
				arr[i].x = arr[i-2].x - grid * 2;
				arr[i].y = arr[i-2].y;
				break;
			case 3:
				arr[i-1].y = arr[i-2].y + grid;
				arr[i-1].x = arr[i-2].x;
				arr[i].y = arr[i-2].y + grid * 2;
				arr[i].x = arr[i-2].x;
				break;
			case 4:
				arr[i-1].y = arr[i-2].y - grid;
				arr[i-1].x = arr[i-2].x;
				arr[i].y = arr[i-2].y - grid * 2;
				arr[i].x = arr[i-2].x;
				break;
		}
		if (arr[i].x > size_windows - grid || arr[i].x < 0 + grid || arr[i].y > size_windows - grid || arr[i].y < 0 + grid) {
			if (i < size_windows / 2 || i > (4*size_windows) / 6 ) {
				cout << i <<" ";
				i = 0;
				arr[i].x = size_windows / 2;
				arr[i].y = size_windows / 2;
			}
			else {
				quit = false;
				cout << endl << i;
			}
		}
	}
	return i;
}

void ShakerSort(position a[], int index_x[], int n) {
	for (int i = 0; i < n; i++) {
		index_x[i] = i;
	}
	int  left = 0, right = n - 1, k = n - 1;
	do {
		for(int j = right; j > left; j--) {
			if(a[index_x[j]].x < a[index_x[j-1]].x) {
				swap(index_x[j], index_x[j - 1]);
				k = j;
			} else if (a[index_x[j]].x == a[index_x[j-1]].x) {
				if (a[index_x[j]].y < a[index_x[j-1]].y) {
					swap(index_x[j], index_x[j-1]);
					k = j;
				}
			}
		}
		left = k;
		for(int j = left; j < right; j++) {
			if(a[index_x[j]].x > a[index_x[j+1]].x) {
				swap(index_x[j],index_x[j+1]);
				k = j;
			} else if (a[index_x[j]].x == a[index_x[j+1]].x) {
				if (a[index_x[j]].y > a[index_x[j + 1]].y) {
					swap(index_x[j], index_x[j + 1]);
					k = j;
				}
			}
		}
		right = k;
	} while(left < right);
	return;
}

bool found_d(position a[], int index_x[], int n, position cursor) {
	bool found = false;
	int left = 0, right = n - 1, m;
	while (left < right) {
		m = (left + right) / 2;
		if (a[index_x[m]].x < cursor.x) left = m + 1;
		else right = m;
	}
	m = right;
	if (a[index_x[m]].x == cursor.x) {
		while(a[index_x[m]].x == cursor.x && !found) {
			if(a[index_x[m]].y == cursor.y) found = true;
			else m++;
		}
	}
	return found;
}


bool test_move_cursor(position a[], int index_x[], int n, int course, position cursor, int grid) {
	switch (course) {
		case 1:
			cursor.x += grid;
			break;
		case 2:
			cursor.x -= grid;
			break;
		case 3:
			cursor.y += grid;
			break;
		case 4:
			cursor.y -= grid;
			break;
	}
	return found_d(a, index_x, n, cursor);
}

void game(int size_windows_h, SDL_Window* win, SDL_Renderer *ren, SDL_Event event) {
	int grid = 12;
	int n_arr_vay = (size_windows_h * size_windows_h) / grid;
	position arr_vay[n_arr_vay];
	bool quit = true;
	int n_vay = vay(arr_vay, size_windows_h, grid) + 1;
	int *index_sort_arr_vay_x;
	index_sort_arr_vay_x = new int [n_vay];
	ShakerSort(arr_vay, index_sort_arr_vay_x, n_vay);
	position cursor;
	cursor.x = size_windows_h / 2;
	cursor.y = size_windows_h / 2;
	SDL_Rect me_r;
	me_r.x = cursor.x + grid / 2;
	me_r.y = cursor.y + grid / 2;
	me_r.w = grid;
	me_r.h = grid;
	SDL_Surface *me_bmp = SDL_LoadBMP("me.bmp");
	SDL_Texture *me = SDL_CreateTextureFromSurface(ren, me_bmp);
	SDL_FreeSurface(me_bmp);
	SDL_Surface *background_bmp = SDL_LoadBMP("background.bmp");
	SDL_Texture *background = SDL_CreateTextureFromSurface(ren, background_bmp);
	SDL_FreeSurface(background_bmp);
	SDL_Rect back_r[size_windows_h/grid + 1][size_windows_h/grid + 1];
	position back_cursor;
	for(int i = 0; i*grid <= size_windows_h; i++) {
		for(int j = 0; j*grid <= size_windows_h; j++) {
			back_cursor.x = i*grid;
			back_cursor.y = j*grid;
			back_r[i][j].h = grid;
			back_r[i][j].w = grid;
			if(!found_d(arr_vay, index_sort_arr_vay_x, n_vay, back_cursor)) {
				back_r[i][j].x = i*grid - grid/2;
				back_r[i][j].y = j*grid - grid/2;
			}
			else {
				back_r[i][j].x = size_windows_h;
				back_r[i][j].y = size_windows_h;
			}
		}
	}
	while(quit) {
		SDL_SetRenderDrawColor(ren, 255, 255, 255, 255);
		SDL_RenderClear(ren);
		SDL_PollEvent(&event);
		SDL_SetRenderDrawColor(ren, 50, 50, 50, 255);
		SDL_RenderCopy(ren, me, NULL, &me_r);
		for(int i = 0; i*grid <= size_windows_h; i++) {
			for(int j = 0; j*grid <= size_windows_h; j++) {
				SDL_RenderCopy(ren, background, NULL, &back_r[i][j]);
			}
		}
		if (event.type == SDL_KEYDOWN && me_r.x == (cursor.x - grid / 2) && me_r.y == (cursor.y - grid / 2)) {
			switch (event.button.button) {
				case SDL_SCANCODE_D:
					if (test_move_cursor(arr_vay, index_sort_arr_vay_x, n_vay, 1, cursor, grid)) cursor.x += grid;
					break;
				case SDL_SCANCODE_A:
					if (test_move_cursor(arr_vay, index_sort_arr_vay_x, n_vay, 2, cursor, grid)) cursor.x -= grid;
					break;
				case SDL_SCANCODE_S:
					if (test_move_cursor(arr_vay, index_sort_arr_vay_x, n_vay, 3, cursor, grid)) cursor.y += grid;
					break;
				case SDL_SCANCODE_W:
					if (test_move_cursor(arr_vay, index_sort_arr_vay_x, n_vay, 4, cursor, grid)) cursor.y -= grid;
					break;
			}
		}
		if(me_r.x < (cursor.x - grid / 2)) me_r.x ++;
		if(me_r.x > (cursor.x - grid / 2)) me_r.x --;
		if(me_r.y < (cursor.y - grid / 2)) me_r.y ++;
		if(me_r.y > (cursor.y - grid / 2)) me_r.y --;
		if(event.type == SDL_QUIT) quit = false;
		if(cursor.x < 0 || cursor.x > size_windows_h || cursor.y < 0 || cursor.y > size_windows_h) quit = false;
		SDL_RenderPresent(ren);
	}
	
}

