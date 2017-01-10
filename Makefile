clean:
	bash gradlew clean
run:
	bash gradlew run
play:
	bash scripts/play.sh
report:
	mkdir -p reports
	python scripts/report.py
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
