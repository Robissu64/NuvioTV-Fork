# Nuvio Center Test V1.1 — dinâmica sem compressão no decodificador Dolby

Atualização incremental para a V1 que já foi compilada e instalada.

## Mudanças

- Central mantém ganho linear de +4 dB, agora sem a proteção suave de picos.
- Demais canais não recebem atenuação compensatória pelo controle da central.
- Na conversão de AC-3/E-AC-3 para AC-3, drc_scale=0 desativa a compressão dinâmica do decodificador FFmpeg. Na V1 ficava no padrão 1. Essa é uma possível causa da redução nas cenas de ação, ainda não confirmada por teste no aparelho.
- Não altera controles de volume ou modos noturnos da TV/soundbar.
- Sem limitador, a central pode ultrapassar o nível digital máximo e distorcer nas etapas seguintes. Não há promessa de ausência de clipping com +4 dB.

Referência: https://ffmpeg.org/ffmpeg-codecs.html#AC_002d3-Decoder-Options

## Aplicar no Windows/GitHub

1. Guarde os APKs da V1 para poder voltar a ela.
2. Extraia este ZIP. Ele contém código, não um APK.
3. Na raiz do seu repositório Robissu64/NuvioTV-Fork, branch nuvio-test, use Add file > Upload files.
4. Arraste as pastas app, ffmpeg-decoder-downmix e tests, e este LEIA-ME. Preserve a estrutura de pastas; não envie a pasta externa do ZIP.
5. Clique Commit changes.
6. Abra Actions > Build Center Test V1 APK > Run workflow, na branch nuvio-test. Inicie uma execução nova; não use Re-run no build antigo.
7. O nome do workflow e do artefato continua V1, mas o app compilado terá versão 0.9.0-center-v1.1.
8. Quando concluir, baixe o artefato, extraia e instale o APK da mesma arquitetura que funcionou na V1.

O pacote não contém .github nem substitui a correção de packages: platform-tools que você já aplicou no workflow.

## Instalação

Tente instalar por cima do Nuvio Center Test. A assinatura de debug pode mudar entre execuções do GitHub: se aparecer conflito de assinatura, guarde suas configurações e integrações antes de desinstalar somente o Center Test. A desinstalação remove seus dados locais. Não desinstale o Nuvio original.

## Configurar e testar

Mantenha Preferir decodificadores do app, Teste da central ligado, AC-3 capable receiver ligado, downmix desligado e tunneled desligado. Reabra o mesmo filme, selecione a mesma faixa Português AC-3 5.1 e compare o mesmo trecho de ação no mesmo volume. Observe impacto das explosões, separação traseira, distorção e sincronismo.

## Validação realizada

Teste C++ local passou para ganho linear inclusive em picos, preservação dos outros canais, diferentes posições da central, bypass e entradas inválidas. Recursos XML foram validados.

O APK V1.1 ainda precisa ser compilado no GitHub Actions e testado na TV. Não foi feita compilação Android completa neste ambiente.

Log esperado: CENTER_TEST_V1_1: AC3/EAC3 decoder DRC disabled; e FC +4 dB, linear gain, no peak protection.
