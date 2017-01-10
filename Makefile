clean:
	bash gradlew clean
run:
	bash gradlew run
sim: \
	play \
	report \
	archive
play:
	bash scripts/play.sh
report:
	mkdir -p reports
	python scripts/report.py
archive:
	bash scripts/archive.sh
jar:
	bash gradlew jarForUpload
client:
	npm run electron --prefix battlecode-client-17
.PHONY: \
	clean \
	run \
	sim \
	play \
	report \
	archive \
	jar \
	client
