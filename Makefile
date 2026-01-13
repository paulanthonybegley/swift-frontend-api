build:
	./mvnw clean install

run:
	java -jar rest-mock/target/rest-mock-1.0.0-SNAPSHOT.jar

run-job:
	java -jar job/target/job-1.0.0-SNAPSHOT.jar

run-all:
	@echo "Starting Mock Server in background..."
	@java -jar rest-mock/target/rest-mock-1.0.0-SNAPSHOT.jar > mock.log 2>&1 &
	@echo "Waiting for Mock Server to start..."
	@sleep 10
	@echo "Starting Job Processor..."
	@java -jar job/target/job-1.0.0-SNAPSHOT.jar
	@echo "Stopping Mock Server..."
	@pkill -f "rest-mock"

test:
	./mvnw test

test-debug:
	./mvnw test -Dorg.apache.cxf.logging.enabled=true

stop:
	pkill -f "rest-mock"

clean-db:
	@echo "Cleaning databases..."
	@rm -f uetrs.db service_uetrs.db test_*.db mock.log
	@echo "Databases cleaned"
