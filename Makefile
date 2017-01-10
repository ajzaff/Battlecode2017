clean:
	bash gradlew clean
run:
	bash gradlew run
play:
	bash scripts/play.sh
jar:
	bash gradlew jarForUpload
client:
	npm run electron --prefix battlecode-client-17
.PHONY: \
	clean \
	run \
	play \
	jar \
	client
