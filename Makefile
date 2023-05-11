JAVA_HOME=$(shell unset JAVA_HOME; /usr/libexec/java_home -v 17)

all:
	./mvnw formatter:format verify

.PHONY: tidy
tidy:
	./mvnw formatter:format
