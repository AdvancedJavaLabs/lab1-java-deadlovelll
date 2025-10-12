Распараллеливание

В реализации параллельного BFS параллелизм достигается за счёт одновременной обработки вершин одного и того же уровня.
Каждая вершина текущего "фронта" обрабатывается независимо, а найденные соседние вершины формируют следующий фронт.
Таким образом, синхронизация требуется только при добавлении новых вершин во множество visited.

Реализация

В пакете org.graph реализован метод для класса Graph, 
выполняющий распараллеленный BFS с использованием пула потоков (не fjp).

Ключевые детали реализации:

Для отметки посещённых вершин используется AtomicIntegerArray visited, что обеспечивает потокобезопасность без глобальной блокировки.

Для параллельной обработки фронта создаётся ExecutorService c фиксированным пулом потоков (Executors.newFixedThreadPool), размером в число доступных CPU.

Вершины текущего уровня (frontier) разбиваются на чанки (по числу потоков), и каждый поток обрабатывает свой подсписок.

Для синхронизации добавления новых вершин используется compareAndSet — только первый поток, отметивший вершину, добавит её в следующий фронт.

После завершения итерации фронт обновляется, и алгоритм продолжается, пока не будут посещены все достижимые вершины.
```java
if (visited.compareAndSet(u, 0, 1)) {
    nextFrontier.add(u);
}
```

Тестирование корректности (JCStress)

Для проверки отсутствия гонок и корректности параллельного доступа использовался JCStress.

Созданы тесты, проверяющие что нет гонок по ресурам

Запуск тестов

Для выполнения JCStress-тестов используется Makefile:

jcstress:
javac -cp jcstress-latest.jar -d out \
app/src/main/java/org/graph/Graph.java \
app/src/test/java/org/graph/ConcurrentStartsTest.java \
app/src/test/java/org/graph/SameStartConcurrentTest.java \
app/src/test/java/org/graph/StaticAdjListInterferenceTest.java
java -cp out:jcstress-latest.jar org.openjdk.jcstress.Main -tb 1s
rm jcstress-results-*.bin.gz

Результаты
RUN RESULTS:
Interesting tests: No matches.
Failed tests: No matches.
Error tests: No matches.


Все тесты прошли успешно — гонок данных при параллельной работе parallelBFS не выявлено.
Атомарные операции и структура итераций по фронтам обеспечивают 
корректную синхронизацию и отсутствие конфликтов между потоками.

Если протестировать сломанную реализацию (GraphBroken),
получаем следующие реузльтаты:

RESULT  SAMPLES     FREQ      EXPECT  DESCRIPTION
1      134    0.01%   Forbidden  
15    2,285    0.23%  Acceptable  All 4 vertices visited (bitmask 1111b == 15)
3  976,562   97.82%   Forbidden  
7   19,357    1.94%   Forbidden  