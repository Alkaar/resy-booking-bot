run: build
	docker run -it --rm --volume=${CURDIR}/src/main/resources:/app/src/main/resources:Z resy-booking-bot

build:
	docker build -t resy-booking-bot .
