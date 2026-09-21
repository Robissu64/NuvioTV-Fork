# Nuvio Center Test V1 — projeto experimental

## Estado real da entrega

Código modificado, com testes isolados do ganho aprovados. **Ainda não há APK
compilado nem teste na TCL C835/Sony HT-S700RF.** A compilação local foi impedida
pela indisponibilidade de acesso ao servidor do Gradle, antes da configuração
do projeto. O workflow fornecido também ainda não foi executado.

Base: `ysosrs123/NuvioTV-Fork`, branch `nuvio-test`, commit
`45e0984c18460d2a65c5d745999011b4314328eb`, identificado no comentário do ZIP
enviado por Rob. Não aplicar cegamente sobre outra versão.

Este pacote contém **somente arquivos novos ou modificados**, mantendo os
caminhos do projeto. Não é um APK e não é um projeto completo independente.
O ZIP original enviado por você permanece intacto.

## O que mudou

- Aplicativo separado: `com.nuvio.tv.center`, nome `Nuvio Center Test`.
- A opção de forçar AC-3 foi renomeada em português brasileiro/inglês para
  `Teste da central: +4 dB / AC-3 5.1`. Não adicionamos um slider nesta V1.
- Com essa opção ligada, o renderer FFmpeg também aceita AC-3 multicanal
  para decodificação, em vez de deixá-lo escapar pelo passthrough.
- O ganho é aplicado no PCM float planar após a conversão para 5.1 e antes
  do codificador AC-3. O canal FC é encontrado pelo layout, sem assumir índice fixo.
- O ganho não depende do controle antigo de mixagem. O coeficiente antigo
  é ignorado na transcodificação para não contaminar este teste.
- Fontes sem canal central identificado não recebem o ganho. Estéreo conhecido
  não é selecionado para transcodificação por esta mudança.
- Com o botão desligado, o ganho é zero. Outros motivos preexistentes do fork
  ainda podem provocar transcodificação, mas não ativam o ganho experimental.
- O novo método `setDeniedTranscodeMimes` foi portado para o módulo fonte:
  o app já o chamava, mas ele estava apenas na versão binária do renderer.
- O diagnóstico reconhece transcodificação inclusive de AC-3 para AC-3.
- A compilação exige o módulo fonte e as bibliotecas FFmpeg recompiladas.
  Isso evita produzir um APK com as mudanças Java e a biblioteca JNI antiga.
- Adicionadas verificações de alocação/FIFO e de frame gravável no trecho alterado.

### Proteção de picos e limitações

Em níveis moderados, o ganho é +4 dB (amplitude multiplicada por aproximadamente
1,5849). Acima de 0,90 de amplitude após ganho, há compressão suave de picos
até o limite de 0,98, somente na central. Isso **não é um limiter transparente**:
pode alterar o timbre em picos; por isso deve ser ouvido e avaliado.

Não há atenuação global. Os outros canais não são alterados pela função de ganho.
Contudo, a conversão de 7.1 para 5.1 e a recodificação AC-3 com perdas afetam
naturalmente o sinal; não prometemos saída bit a bit idêntica ao original.

O encoder existente usa AC-3 5.1 a 640 kbps e frequências compatíveis. Não preserva
Atmos, TrueHD sem perdas ou 7.1 discreto. Não controla Netflix, outros apps ou
o volume físico da soundbar: funciona somente no caminho FFmpeg/ExoPlayer deste app.
O desempenho, sincronismo, troca de faixa e comportamento ao avançar o filme
ainda precisam de testes reais. MPV e players externos não usam esta alteração.

## Caminho recomendado no Windows: compilar pelo GitHub

O trabalho pesado é feito por uma máquina Linux do GitHub. Não é necessário
instalar Android Studio para este caminho. Nenhum repositório foi criado,
nenhum arquivo foi publicado e nenhuma execução foi iniciada por mim.

1. Entre na sua conta GitHub e abra https://github.com/ysosrs123/NuvioTV-Fork.
2. Use **Fork** para criar uma cópia na sua conta. Mantenha `nuvio-test` como
   branch padrão e verifique a base acima. Não envie alterações ao projeto do autor.
3. Baixe e extraia `Nuvio-Center-Test-V1-Alteracoes.zip` no Windows.
4. No SEU fork, na raiz e na branch `nuvio-test`, use **Add file → Upload files**.
5. Arraste o CONTEÚDO da pasta extraída: `.github`, `app`,
   `ffmpeg-decoder-downmix`, `tests` e este arquivo de instruções.
   Não envie o ZIP nem a pasta externa que o envolve. Se necessário, habilite
   a visualização de itens ocultos no Explorador para ver `.github`.
6. Confira os caminhos: deve aparecer `.github/workflows/build-center-test.yml`
   e `app/build.gradle.kts`, sem uma pasta extra antes deles. Confirme o commit
   no seu fork. O upload contém apenas as mudanças e preserva os demais arquivos.
7. Abra **Actions** e habilite workflows no seu fork se o GitHub solicitar.
8. Selecione **Build Center Test V1 APK → Run workflow → nuvio-test → Run workflow**.
   O arquivo do workflow deve estar na branch padrão para aparecer o botão.
9. Espere o resultado. Não tente adivinhar correções se aparecer vermelho:
   abra a etapa com erro e envie o trecho final do log, ocultando dados privados.
10. Se ficar verde, abra a execução e baixe **Nuvio-Center-Test-V1-APKs** em
    **Artifacts**. O pacote terá APKs separados para ARM 32 e 64 bits.

Não envie senhas, tokens, chaves de API ou arquivos `local.properties` para o
repositório. O workflow não precisa de segredos de publicação nem publica uma
release. A compilação pode consumir a cota de GitHub Actions da conta: confira
os limites e não ative cobrança adicional apenas para este teste.

A compilação usa assinatura de depuração gerada no runner. Recompilações podem
ter outra chave e exigir desinstalar APENAS o Center Test, perdendo os dados
dele. A assinatura estável será tratada antes de uso diário. O Nuvio original
permanece independente. Não desinstale o original para resolver assinatura.

### Alternativa local

Para editar no Windows, extraia o ZIP original em uma pasta nova e copie os
arquivos deste pacote por cima, preservando caminhos. A geração do FFmpeg
fornecida foi feita para Linux (GitHub Actions ou WSL). Abrir no Android Studio
e clicar Build sem essas bibliotecas não basta. Podemos orientar WSL se o
GitHub não for uma opção; não é necessário preparar ambos os ambientes.

## Configuração e teste na TV, após o APK compilar

1. Escolha a ABI compatível com o Android da TV, não só com o processador.
   Se tiver ADB: `adb shell getprop ro.product.cpu.abilist`.
   Se aparecer somente `armeabi-v7a`, use o APK ARM 32. Não force instalação
   de um APK incompatível. Se estiver em dúvida, envie a mensagem/print.
2. Instale **Nuvio Center Test**, mantendo o Nuvio original.
3. Configure o app separadamente. Login, catálogos e integrações do projeto
   podem exigir configurações/chaves que não acompanham o código público;
   não estamos prometendo que todos os serviços online funcionem nesta build.
4. Use o player interno **ExoPlayer**, decodificadores do app/FFmpeg preferidos,
   reprodução tunneled desligada, downmix manual desligado e velocidade 1x.
   Não use Bluetooth, MPV ou player externo durante o teste.
5. Deixe a saída digital da TV compatível com Dolby Digital/passthrough,
   não forçada a PCM estéreo. Não altere firmware ou menu de serviço.
6. Comece em volume moderado e com um arquivo conhecido AC-3 5.1. Use primeiro
   o botão `Teste da central` DESLIGADO; ouça um trecho e anote o volume.
7. Saia do filme, ligue o botão e reabra o MESMO trecho, MESMA faixa e MESMO
   volume. A central deve ficar mais presente sem espalhar a voz para as traseiras.
8. O ajuste antigo `Nível do canal central` pode continuar cinza: é esperado.
   Ele NÃO é o controle deste protótipo.
9. Confira a saída efetiva no diagnóstico do player/TV, se disponível. O nome
   `AC-3 5.1` na lista de faixas descreve a FONTE e sozinho não prova a saída.
   Se possível, teste um arquivo que identifique cada canal separadamente.
10. Só depois teste 7.1 → 5.1, pausa/retomada, avanço, troca de faixa e um trecho
    longo. Anote estéreo inesperado, atraso, cortes, travamentos ou distorção.

Para desfazer: desligue o botão, saia e reabra o filme, ou volte ao app original.
Não é preciso mudar nada na soundbar nem remover seu aplicativo principal.

Diagnóstico opcional com ADB no PowerShell, se já estiver configurado:

```powershell
adb logcat -d -s ffmpeg_jni:D | Select-String 'CENTER_TEST_V1'
```

O marcador `FC +4 dB, soft peak protection, AC3 5.1` confirma que a função
executou. Ele não prova sozinho que a TV transportou todos os canais.

## Verificação realizada nesta entrega

- Teste C++ real compilado com `g++`, C++11, avisos tratados como erro.
- Ganho em canais FC em posições diferentes (layouts sintéticos de 3, 6 e 8 canais).
- Igualdade dos demais canais antes/depois da função de ganho.
- Bypass com 0 dB, limites de ganho, zero, sinais positivos/negativos e entradas inválidas.
- Continuidade/monotonicidade da proteção em varredura de amplitudes.
- Teste com AddressSanitizer e UndefinedBehaviorSanitizer aprovado;
  detecção de vazamentos desabilitada porque este ambiente usa ptrace.
- Pendente: compilação Java/Kotlin/JNI Android completa, codificação/decodificação
  real de teste e validação de 5.1 no conjunto TCL/Sony.

Não confundir teste matemático aprovado com APK pronto.

## Referências de operação

- Upload de arquivos/pastas no GitHub:
  https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository
- Execução manual de workflows:
  https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow

Preservar as licenças e avisos do projeto base ao distribuir alterações.
Este pacote não altera a licença GPL-3.0 do módulo modificado.
