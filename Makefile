build:
	./mvnw clean install

run:
	java -jar rest-mock/target/*-shaded.jar

test:
	./mvnw test

stop:
	pkill -f "rest-mock"
