JAVA_HOME=$(shell unset JAVA_HOME; /usr/libexec/java_home -v 21)

all:
	./mvnw formatter:format verify

.PHONY: tidy
tidy:
	./mvnw formatter:format

.PHONY: clean
clean:
	./mvnw clean
