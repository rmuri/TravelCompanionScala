@echo off
(cd.. && cd db && copy TravelCompanion.h2.db "%HOME%/TravelCompanion.h2.db")
(cd.. && java -XX:MaxPermSize=256m -XX:PermSize=128m -Xms128m -Xmx512m -jar lib/start-6.1.23.jar)