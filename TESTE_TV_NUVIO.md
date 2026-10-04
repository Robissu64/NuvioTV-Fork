# Teste experimental — Nuvio V1.3

Não há confirmação de funcionamento desta etapa na TV. O usuário aprovou áudio V1.2 e QR na versão anterior; esses caminhos foram preservados no código.

1. **Atualização:** manter o app aprovado instalado. Conferir arquitetura e assinatura no relatório de entrega. Tentar atualizar normalmente; verificar conta, addons, tema e preferências existentes. Se o Android rejeitar assinatura, parar: desinstalar apaga dados e não é uma solução autorizada.
2. **Nome e launcher:** conferir “Nuvio”. O tema padrão usa ícone/banner da release oficial 1.0.0; temas alternativos conservam os recursos intencionais anteriores. O launcher da TV pode demorar para atualizar seu cache.
3. **Central:** usar o mesmo trecho e configuração de saída 5.1 já aprovados. Conferir 0, +4 e +6 dB, traseiras discretas, faixa de áudio e persistência. Preferência V1.1 deve continuar em +4 dB ao migrar; não limpar dados para testar isso.
4. **Login:** verificar conta já conectada e, quando apropriado, geração/leitura do QR. O teste de legendas não deve derrubar a sessão.
5. **CX Explorer:** no celular, ativar o acesso de rede habitual; na TV abrir um MKV e escolher Nuvio. Testar início, seek e áudio. Anotar se Nuvio aparece e a mensagem exata em caso de falha. A URI entregue pode ser content/file/http/https; não presumir ftp. Não enviar URLs com credenciais em relatório público.
6. **Legenda local:** durante o filme, abrir Legendas → “Escolher arquivo de legenda”; selecionar SRT/VTT/ASS/SSA. Validar os acentos e, com libass/MPV, o estilo de ASS/SSA. Os exemplos entregues exibem linhas entre 1 e 12 segundos; buscar esse trecho. Cancelar o seletor e confirmar a legenda/faixa anteriores. Repetir com o filme pausado e em reprodução.
7. **Envio pelo celular:** Legendas → “Enviar legenda pelo celular”; ler o QR com o celular na mesma rede. Escolher e enviar a legenda no navegador. Esse é também o fallback automático quando nenhum seletor Android está disponível. Testar fechar com Voltar e reabrir: o QR anterior deixa de funcionar. O QR expira em 10 minutos. Não exige internet nem conta de legendas.
8. **Erros e estado:** usar arquivo inválido e maior que 8 MiB; o filme e a faixa atual devem continuar. Com “somente idiomas preferidos”, confirmar que a legenda local aparece. Selecionar local enquanto chegam addons; ela deve permanecer selecionada. Trocar local/embutida/addon/desativada preservando posição, pausa, velocidade, faixa de áudio, ganho e atraso de legenda. Trocar filme e reabrir o app: legenda local é uma seleção temporária, não deve reaparecer em outro título; selecionar novamente se necessário.

Busca direta online por nome de arquivo ainda não foi implementada: depende da configuração de API key do provedor. Busca pelos addons existentes continua disponível para títulos com metadados/ID e addons de legenda habilitados.

Para relatar: modelo da TV, APK/arquitetura, player ExoPlayer/libass/MPV usado, formato da legenda, etapa acima, resultado esperado/observado e mensagem exibida. Marcar separadamente o que funcionou no seletor e pelo celular.

Teste adicional de importação lenta: ao ler uma legenda de provedor remoto, selecionar outra faixa ou desativar legendas antes da leitura terminar. A importação anterior deve ser cancelada e não pode substituir a escolha mais recente.

**Resultado da conferência de assinatura:** incompatível com o APK aprovado do run 35792050378. Não instalar como atualização da V1.2 nem desinstalar para contornar o bloqueio. Testar em outro aparelho ou decidir previamente sobre pacote de testes separado.
