## Курсовая работа по курсу "Операционные Системы"


[Исходный документ с заданиями](https://github.com/mlkv52git/sibsutis_os/blob/master/%D0%9A%D1%83%D1%80%D1%81%D0%BE%D0%B2%D1%8B%D0%B5.pdf)


### Задание:

Сетевая REST API служба выполняющая роль диспетчера приложений.


### Сборка:

`go build`



//Запустить командную строку от имени администратора

### Запуск:

`5semestrOS.exe install`

`5semestrOS.exe start`



### Завершение:

`5semestrOS.exe stop`



### Удаление:

`5semestrOS.exe remove`



### Дополнительные ключи:

`-port {port}` - установка сервиса на порту {port}, по умолчанию используется порт 8080 (использовать только с параметром start)

пример: `5semestrOS.exe -port 9000 start`



### API:

`"GET" /process` - список процессов в формате json, поле "Processes" массив процессов. (application/json)

`"DELETE" /process/{ProcessID}` - остановка процесса, поле "Errors" ошибки возникшие в результате. (application/json)
