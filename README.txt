CsvAdder

Aplikacja sumująca wiersze w podanym pliku csv pogrupowanych po operacji modulo X.
Aplikacja wysyła zsumowane liczby na kafkę oraz wystawia websocket, gdzie można zasubskrybować się na wiadomości.

Przykład wywołania websocketa:
ws://localhost:8080/ws?number=1

--------
Przygotowanie prostej aplikacji:
- załadowanie pliku csv, dowolnej długości (liczby w kolejnych wierszach)
- podział streamu na x mniejszych -> liczba z pliku % (operacja modulo) x
- dodajemy liczby w każdym streamie do siebie i wrzucamy na kolejkę (Kafka, RabbitMq)
- dodatkowo websocket gdzie możemy się zasubskrybować na dany dzielnik [0,1 ... x-1]

Wymagania:
- Zadanie udostępnione na Github
- yaml asyncApi (tapir)
- Docker compose
- Scala 2, fs2 + cats-effect
- http4s - websocket api

--------
Do dokończenia:
- testy
- brak error handlingu
- keep alive dla websocketa
