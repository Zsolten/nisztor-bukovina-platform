# ADR-001: Moduláris monolit

## Állapot

accepted

## Kapcsolódó követelmények

- `NFR-TECH-002`
- `NFR-TECH-004`

## Kontextus

A backendnek a panziókezelés, a foglalás, a turisztikai tartalom és a támogató képességek eltérő felelősségeit kell kezelnie. A követelményspecifikáció Java, Spring Boot és moduláris rétegezett architektúra használatát írja elő. A foundation célja egyetlen futtatható és tesztelhető backend létrehozása üzleti funkciók nélkül.

## Döntés

A backend egyetlen Gradle projektként épülő, Spring Boot alapú moduláris monolit. Az alapcsomag `com.bukovina.platform`, ezen belül az üzleti és támogató képességeket package-by-feature modulok választják el. A modulhatárokat és a megengedett függőségi irányokat ArchUnit tesztek teszik ellenőrizhetővé.

## Következmények

- Egy alkalmazás és egy telepítési egység szolgálja ki a backend képességeit.
- A modulok felelőssége és belső rétegezése kódszinten elkülönül.
- Körkörös csomagfüggőség nem megengedett.
- A foundation nem vezet be elosztott rendszerhez tartozó szolgáltatáshatárokat vagy kommunikációt.
