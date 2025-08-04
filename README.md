--spring.datasource.url - урл для подключения к бд
  --spring.datasource.driver-class-name - драйвер для подключения (postgres=org.postgresql.Driver, SQLite=org.sqlite.JDBC)
  --spring.datasource.username - username бд
  --spring.datasource.password - password бд
  --server.port - порт на котором работает приложение


Запуск из папки с jar-ником командой в отдельном терминале

java -jar db-viewer-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/db_name \
  --spring.datasource.driver-class-name=org.postgresql.Driver
  --spring.datasource.username=username \
  --spring.datasource.password=password \
  --server.port=8088


для запуска нужна 21 java

1. Установить homebrew, если не стоит
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

2. Проверить, что установилась
brew -v

Вывод:
Homebrew 4.5.13

3. brew install openjdk@21

4. После установки Brew покажет, где находится Java, например:
==> Caveats
openjdk@21 is keg-only, which means it was not symlinked into /opt/homebrew/bin.

If you need to have openjdk@21 first in your PATH, run:
  echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zprofile
5. Добавить Java 21 в PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zprofile
echo 'export CPPFLAGS="-I/opt/homebrew/opt/openjdk@21/include"' >> ~/.zprofile
source ~/.zprofile
6. Проверить установленную версию Java
```
java --version
```
Вывод что-то типа
```
openjdk 24.0.1 2025-04-15
OpenJDK Runtime Environment Homebrew (build 24.0.1)
OpenJDK 64-Bit Server VM Homebrew (build 24.0.1, mixed mode, sharing)
```

После поднятия приложения оно будет доступно по 
localhost:8088/viewer

Если меняли порт, то указать его, вместо 8088
