# ADR-006: RabbitMQ a P2 értesítésekhez

## Állapot

deferred to P2

## Kapcsolódó követelmények

- `FR-EMAIL-001`
- `FR-EMAIL-002`

## Kontextus

A foglalási e-mail-megerősítés és az adminértesítés P1 követelmény. A Requirements.pdf a RabbitMQ-integrációt P2 prioritású későbbi fejlesztésként sorolja fel. A foundation nem tartalmaz e-mail-küldést vagy üzenetközvetítőt.

## Döntés

RabbitMQ kizárólag a `support.notification` P2 értesítési folyamatainak későbbi üzenetközvetítője lehet. Nem része a foundationnek, és nem előfeltétele a P1 e-mail-követelmények megvalósításának. A konkrét eseményekről és üzenetszerződésekről az azokat bevezető P2 feature PR dönt.

## Következmények

- A foundation Compose konfigurációja nem tartalmaz RabbitMQ szolgáltatást.
- A P1 e-mail-folyamatok RabbitMQ nélkül is megvalósíthatók.
- A P2 integráció csak a notification felelősségén belül vezethető be.
- Ez az ADR nem rögzít eseményneveket, payloadokat, kézbesítési ígéreteket vagy retry szabályokat.
