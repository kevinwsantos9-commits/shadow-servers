# Shadow Servants — Minecraft 1.21.1 / NeoForge 21.1.172

Mod de servos das sombras baseado no projeto enviado.

## Funciona
- 10 derrotas de uma mesma criatura desbloqueiam 1 servo.
- O progresso e salvo no mundo.
- Servos invocados acompanham o jogador e ajudam contra o ultimo inimigo atacado/que atacou o jogador.
- Guardar um servo nao o consome.
- Se um servo morrer, ele e removido permanentemente do estoque daquele tipo.
- Servos recebem marcador persistente e brilho para diferenciar a criatura normal.
- Tecla **G** abre o menu simples do mod.
- O menu nao usa blur: o fundo e desenhado diretamente para manter texto e botoes nitidos.

## Comandos
- `/shadowservants status`
- `/shadowservants summon minecraft:zombie`
- `/shadowservants summon minecraft:skeleton`
- `/shadowservants summon minecraft:spider`
- `/shadowservants dismiss minecraft:zombie`
- `/shadowservants dismiss_all`

## Requisitos
- Minecraft 1.21.1
- NeoForge 21.1.172
- Java 21

**Importante:** este projeto usa NeoForge, nao Forge. Nao misture os dois loaders na mesma instancia.

## Build pelo GitHub
O workflow instala Gradle 8.10.2 automaticamente, compila e publica o JAR em **Actions > Artifacts**.

## Fonte da implementacao
O projeto original enviado tinha a base de NeoForge, mas o `gradlew` era apenas um placeholder e os sistemas de gameplay eram apenas stubs. Esta versao transforma esses pontos em uma implementacao jogavel, mantendo a regra central de 10 derrotas por servo.
