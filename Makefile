JAVA_HOME=$(shell unset JAVA_HOME; /usr/libexec/java_home -v 17)

all:
	./mvnw verify
