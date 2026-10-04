# Handoff Nuvio — 04/10/2026

## Etapa 1: auditoria concluída

- Fork: https://github.com/Robissu64/NuvioTV-Fork, branch `nuvio-test`.
- Base local e remota verificada: `ee327e3ef694020554f91239ad4ce5d337233def`.
- Checkout inicialmente limpo; nenhum AGENTS.md encontrado no projeto.
- ZIP antigo e APK oficial não estavam em Downloads. Implementação pendente será recriada.
- Login QR e áudio aprovados na TV pelo usuário; não são validações feitas neste ambiente.
- Fallback público do login presente em app/build.gradle.kts; preservá-lo.
- applicationId `com.nuvio.tv.center`; versão base `1360 / 0.9.0-center-v1.2`.
- Decoder FFmpeg local obrigatório, central 0–6 dB, migração de preferência legada para +4 dB presentes.
- Abertura externa content/file/http/https e DefaultDataSource presentes. CX Explorer ainda exige teste na TV; não pressupor FTP direto.
- `.github/workflows/build-center-test.yml` deve permanecer byte a byte intacto.

## Trabalho em andamento

Restaurar nome e sete recursos oficiais de launcher; implementar importação SRT/VTT/ASS/SSA com limite, charset, cancelamento e alternativa para Android TV sem seletor. Investigar busca online existente e requisitos atuais de provedores. Atualizar este arquivo a cada etapa.

## Compilação

JDK, Android SDK e gh não encontrados no PATH. Verificando credencial Git existente e possibilidade de executar o workflow autorizado. Não substituir o decoder por AAR original para contornar o build.


## Etapa 2: nome, recursos e implementação local concluídos; validação Android em andamento

- Nome Nuvio no launcher e em todas as strings app_name, incluindo debug; applicationId preservado.
- Versão experimental: 1361 / 0.9.0-center-v1.3-local-subtitles.
- APK oficial 1.0.0 baixado da release; SHA-256 conferido: b682c455ce3766bce9af250c7a408fe2b1e5b8e207db299854bcce003b9db9b1.
- Sete PNGs extraídos com IDs da tabela resources.arsc. Proveniência e hashes em NUVIO_RECURSOS_OFICIAIS.json. Ícone e banner inspecionados visualmente. Atividades de tema alternativas preservadas; selecionar tema padrão para ver o recurso oficial.
- Importação OpenDocument, fallback GetContent e envio pelo celular na mesma rede com QR temporário (10 minutos). Servidor inicia somente com o painel aberto e para ao fechar/sair/trocar mídia. Upload exige token aleatório e valida tamanho/formato antes de aplicar.
- SRT/VTT/ASS/SSA: limite de 8 MiB durante leitura; detector de charset existente; cache UUID em UTF-8; idioma desconhecido (und), sem atribuir português indevidamente. Cache limitado e limpo ao finalizar o controller; resíduos antigos removidos após 24 horas na próxima sessão.
- Legenda local permanece visível com filtro de idiomas e chegada posterior de addons. Caminhos sidecar/ExoPlayer/libass/MPV existentes reutilizados; extensão ASS/SSA mantida no cache MPV. Não se persiste URL temporária da legenda local; é necessário selecionar novamente após reabrir.
- Seletor preserva pausa/reprodução anterior; cancelamento não troca legenda. Erro de importação não muda fonte de vídeo nem faixa ativa. Ganho, QR e identidade não foram modificados.
- 42 testes JVM passaram: 8 novos de formatos/tamanho/encoding/cancelamento, 2 novos de HTTP/token/upload e 32 existentes do detector de charset. Execução isolada usa Kotlin/JDK17 + JUnit e as fontes reais, sem Android SDK. Compilação Android ainda precisa confirmar integração da interface.
- Credencial do Git autenticada como Robissu64; autorizado enviar para nuvio-test e executar workflow existente. Não há JDK/SDK Android previamente configurados neste computador.
- Diff de .github vazio até esta etapa.

## Etapa 3: pesquisa online concluída; integração direta pendente de configuração

O SubtitleRepositoryImpl já consulta addons habilitados que anunciem subtitles, usando tipo/ID e opcionalmente vídeo hash, tamanho e filename. Há timeout de 20 segundos por addon e resultados progressivos. O controller calcula hash com OpenSubtitlesHasher, porém a busca exige contentId/contentType: arquivo externo sem metadados não tem caminho de busca por consulta livre. Não reutilizar URL externa como se fosse um ID IMDb.

OpenSubtitles.com é viável, mas sua documentação oficial requer API key de consumidor (criada em conta OpenSubtitles.com) e User-Agent de aplicação. Downloads têm cotas por IP/conta e podem exigir autenticação para ampliar o limite. Nenhuma chave própria foi fornecida; não se emprestou chave de terceiros e não se adicionou botão sem funcionalidade. A conta OpenSubtitles é independente do login Nuvio; não exige conta Supabase.

Fontes verificadas em 04/10/2026:
- https://opensubtitles.tawk.help/article/getting-started
- https://opensubtitles.tawk.help/article/about-the-api
- https://github.com/opensubtitles/vlsub-opensubtitles-com/blob/main/docs/languages.md (pt-br e pt-pt distintos; confirmar catálogo do endpoint da versão escolhida)
- https://developer.android.com/training/data-storage/shared/documents-files

Próxima implementação online: configuração de API key em armazenamento privado da instalação (sem logs/commits), autenticação opcional e quotas reportadas pelo serviço; busca editável por título/filename, parsing conservador de SxxExx e tags de release, PT-BR prioritário e seleção manual de resultados com fonte/release/idioma/hash. Usar ranges de início/fim para hash HTTP apenas quando o servidor realmente honrar Range; leitura seekable local quando viável. Validar e baixar pelo mesmo limite/pipeline local. Não baixar o filme inteiro para obter hash. Avaliar addons instalados para resolver metadados antes de duplicar a integração. Não há serviço online novo concluído nesta versão.

## Revisão final da implementação

- Commit inicial enviado: `7c9cf18053d8208b60dada783a5ddbd71c47ac68`.
- Build inicial: https://github.com/Robissu64/NuvioTV-Fork/actions/runs/37201015451 (não usar seus APKs como entrega final; revisão adicional em andamento).
- Teste C++ de ganho da central e cross-compilação FFmpeg ARM32/ARM64 passaram nesse build.
- Revisão adicional elimina cópia duplicada do cache local no MPV, conserva ASS/SSA e atrasa o fechamento do QR para permitir resposta HTTP ao celular.
- 46 testes JVM passaram após acrescentar quatro casos de visibilidade: PT-BR vs PT-PT, idioma desconhecido local, filtro vazio, snapshots progressivos e deduplicação.
- APK anterior da correção QR recuperado do run 35792050378 para conferir a assinatura antes de recomendar atualização. Certificado SHA-256: `dbbaf04c198f64d3114a2581af29186c039361fd10a7a8c0bde7c24f7f15dfec`. A identidade Android sozinha não garante atualização: a assinatura nova também precisa corresponder. Não desinstalar o app aprovado para contornar incompatibilidade.
- Revisão de concorrência: selecionar outra faixa/desativar cancela importação local pendente; uma leitura antiga não pode aplicar depois da escolha mais recente nem apagar o indicador de uma nova importação. Roteiro de teste incluído em TESTE_TV_NUVIO.md.


## Etapa 4: compilação, inspeção dos APKs e entrega concluídas

- Build final bem-sucedido: https://github.com/Robissu64/NuvioTV-Fork/actions/runs/37201800191.
- Código compilado: `948267790a85dcaf1530e55ee3a27c9c8f046b1a`; Gradle assembleFullDebug + decoder local, BUILD SUCCESSFUL em 9m59s.
- Commits de implementação enviados à nuvio-test:
  - 7c9cf18053d8208b60dada783a5ddbd71c47ac68 — nome, PNGs oficiais, importação local e alternativa por QR.
  - 82b629770be364a24cc50f1c93b8db04de3d0e25 — visibilidade, cache MPV e confirmação de upload.
  - 948267790a85dcaf1530e55ee3a27c9c8f046b1a — cancelamento de importação quando chega escolha mais recente e roteiro TV.
- APKs full/debug ARM32 (armeabi-v7a) e ARM64 (arm64-v8a), versão 1361 / 0.9.0-center-v1.3-local-subtitles, applicationId com.nuvio.tv.center.
- Nome Nuvio, sete recursos oficiais, marcador CENTER_TEST_V1_2 no libffmpegJNI.so e fallback público de login (backend e chave anon existentes, sem imprimir a chave) conferidos dentro dos APKs. SHA-256 dos APKs e certificados em NUVIO_APK_VALIDACAO.json.
- 46 testes JVM passaram na revisão final; teste C++ da central passou no workflow final. Não houve teste da nova etapa na TCL C835.
- `.github` permanece intacta: árvore Git da base e do final é 553e4971b321a530396a538ee7390cb4ba49c2bd; diff vazio. MainActivity, Manifest, PlayerMediaSourceFactory, PlayerSettingsDataStore e todo ffmpeg-decoder-downmix também sem mudanças contra a base.
- Fonte modificada, patch binário, metadados, logs, APKs e legendas de teste preservados localmente em C:/Users/Robson/Documents/Nuvio-Entregas-2026-10-04. ZIP do código contém somente arquivos modificados e material de continuação; reaplicar sobre a base indicada, não sobre o upstream oficial.

### Assinatura e preservação dos dados da TV

- Em resposta à pergunta, o usuário informou que não guardou keystore e pediu investigação sem alterar .github. Não autorizou desinstalação ou perda de dados.
- Nenhuma keystore/JKS encontrada no projeto, no histórico completo da branch (clone não raso), em ~/.android ou em Downloads. O artefato aprovado do run 35792050378 contém apenas os dois APKs. A API do fork listou somente artefatos APK desse workflow e nenhum cache.
- Certificado anterior: dbbaf04c198f64d3114a2581af29186c039361fd10a7a8c0bde7c24f7f15dfec, Android Debug, criado em 22/09/2026 22:46:30 UTC no build anterior.
- Certificado final: 3300a5119494072572487b9497cdfb8c37b8320a6d0fcdc368a99bbce5497d38.
- Assinatura compatível com o APK do build aprovado: false.
- A chave privada não está no certificado do APK. A configuração atual usa a keystore debug temporária do runner. Manter package/versionCode não resolve incompatibilidade de assinatura. Fonte oficial: https://developer.android.com/studio/publish/app-signing.
- Nenhum comando de instalação, desinstalação, limpeza de dados ou mudança de applicationId foi executado. Manter a V1.2 instalada até escolher uma alternativa.
- Opções a apresentar: testar os APKs finais em outro aparelho sem a instalação antiga; ou, com autorização explícita para excepcionar a identidade somente do pacote de testes, preparar um app separado para coexistir com a V1.2. Uma migração para nova assinatura só pode ocorrer após revisar exportação/backup viável e obter autorização expressa; não prometer recuperação completa de dados (Manifest atual tem allowBackup=false).
- Para futuras atualizações, guardar uma chave estável privadamente e assinar os APKs localmente após o Actions permite manter .github intacta. Isso não recupera a chave anterior nem autoriza uma migração agora.

### Arquivos e continuação

Novos: LocalSubtitleFiles.kt, SubtitleVisibility.kt, LocalSubtitleTransferServer.kt, LocalSubtitleTransferOverlay.kt, PlayerRuntimeControllerLocalSubtitles.kt; testes LocalSubtitleFilesTest.kt, SubtitleVisibilityTest.kt e LocalSubtitleTransferServerTest.kt; documentos HANDOFF_NUVIO.md, TESTE_TV_NUVIO.md, NUVIO_RECURSOS_OFICIAIS.json e NUVIO_APK_VALIDACAO.json.

Alterados: app/build.gradle.kts (somente nome e versão); domain/model/Subtitle.kt; PlayerRuntimeController.kt, Initialization, Lifecycle, Observers, PlaybackEvents, SubtitleTiming e TrackSelection; PlayerScreen.kt, PlayerUiState.kt e SubtitleSelectionOverlay.kt; sete PNGs e strings app_name das traduções/debug, com strings locais novas em EN/PT-BR. Relação exata em ENTREGA.json no ZIP.

Pendências: decisão segura para testar na TV devido à assinatura; validação real de CX Explorer e legendas; configuração própria para provedor online. Na integração futura, corrigir a verificação de Range do OpenSubtitlesHasher: ele atualmente aceita resposta 200 ao pedir o fim do vídeo e pode gerar hash errado quando o servidor ignora o range. Não tratar esse hash como correspondência certa. Também falta leitura seekable de URI local e caminho de consulta editável para arquivo sem ID.
