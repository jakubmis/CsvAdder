FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1
COPY . /CsvAdder
WORKDIR /CsvAdder

RUN sbt clean compile
EXPOSE 9092 8080
CMD sbt run