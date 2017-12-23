all: clear
	mkdir -p bin
	go build -o bin/main
	GOOS=windows GOARCH=amd64 go build -o bin/main.exe main.go
	GOOS=windows GOARCH=386 go build -o bin/main.exe main.go
	mkdir -p bin/html
	mkdir -p bin/resourse
	cp html/* bin/html
	cp resourse/* bin/resourse
	cp READMY.txt bin/READMY.txt
	cp testBase1.dat bin/testBase1.dat
	zip -r course-work.zip bin

clear:
	rm -r -f bin
	rm -f course-work.zip