JAR := runelite-client/build/libs/client-*-shaded.jar

install:
	./gradlew build

build:
	./gradlew :client:shadowJar

run: build
	java -ea -jar $(wildcard $(JAR)) --developer-mode

.PHONY: install build run
