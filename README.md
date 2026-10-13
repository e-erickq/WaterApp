# WaterApp

Aplicativo Android para monitoramento do consumo diário de água, com metas personalizadas, registro de consumo e acompanhamento visual do progresso.

## Funcionalidades

- Definição e alteração da meta diária.
- Registro dos volumes consumidos.
- Acompanhamento do progresso por meio de um gráfico personalizado.
- Alerta visual ao ultrapassar a meta.
- Persistência dos dados de consumo.

## Tecnologias

- Java
- Android SDK
- XML
- Canvas
## Persistência

Foi usado SharedPreferences porque os dados são poucos e simples (meta, data,
total e registros do dia), é nativo do Android, não exige biblioteca nem banco,
e os registros de dias anteriores são descartados, o que torna um banco de
dados desnecessário.