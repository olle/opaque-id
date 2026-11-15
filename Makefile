ifeq ($(shell uname -s),Darwin)
JAVA_HOME := $(shell unset JAVA_HOME; /usr/libexec/java_home -v 25 2>/dev/null)
endif
export JAVA_HOME

all: verify

verify:
	./mvnw -B formatter:format verify

test:
	./mvnw -B test

tidy:
	./mvnw -B formatter:format

clean:
	./mvnw -B clean

.PHONY: all verify test tidy clean
