build:
	./mvnw clean install

run:
	java -jar rest-mock/target/*-shaded.jar

test:
	./mvnw test

test-debug:
	./mvnw test -Dorg.apache.cxf.logging.enabled=true

stop:
	pkill -f "rest-mock"
