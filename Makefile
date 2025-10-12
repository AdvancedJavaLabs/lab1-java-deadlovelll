jcstress:
	javac -cp jcstress-latest.jar -d out \
	app/src/main/java/org/graph/Graph.java \
	app/src/test/java/org/graph/ConcurrentStartsTest.java \
	app/src/test/java/org/graph/SameStartConcurrentTest.java
	java -cp out:jcstress-latest.jar org.openjdk.jcstress.Main
	rm jcstress-results-*.bin.gz