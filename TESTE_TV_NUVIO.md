# Teste experimental — Nuvio V1.3

Não há confirmação de funcionamento desta etapa na TV. O usuário aprovou áudio V1.2 e QR na versão anterior; esses caminhos foram preservados no código.

1. **Instalação separada:** manter a V1.2 aprovada instalada. Instalar o APK final assinado **Nuvio-Teste-V1.3-ARM32.apk** ou **ARM64.apk**, conforme a arquitetura aceita pelo Android da TV. Ele usa com.nuvio.tv.center.test e deve coexistir com a V1.2; não atualizar/desinstalar a principal. A variante tem configurações independentes: entrar na conta, configurar addons e conferir preferências nela. Não escolher os APKs principais ou unsigned do Actions.
2. **Nome e launcher:** conferir “Nuvio Teste” ao lado do aplicativo V1.2 já instalado. O tema padrão usa ícone/banner da release oficial 1.0.0; temas alternativos conservam os recursos intencionais anteriores. O launcher da TV pode demorar para atualizar seu cache.
3. **Central:** usar o mesmo trecho e configuração de saída 5.1 já aprovados. Conferir 0, +4 e +6 dB, traseiras discretas, faixa de áudio e persistência. Preferência V1.1 deve continuar em +4 dB ao migrar; não limpar dados para testar isso.
4. **Login:** entrar pelo QR na variante nova e verificar persistência ao reabrir. O teste de legendas não deve derrubar a sessão. A sessão Android local da V1.2 não é copiada para a variante.
5. **CX Explorer:** no celular, ativar o acesso de rede habitual; na TV abrir um MKV e escolher Nuvio Teste. Testar início, seek e áudio. Anotar se Nuvio Teste aparece e a mensagem exata em caso de falha. A URI entregue pode ser content/file/http/https; não presumir ftp. Não enviar URLs com credenciais em relatório público.
6. **Legenda local:** durante o filme, abrir Legendas → “Escolher arquivo de legenda”; selecionar SRT/VTT/ASS/SSA. Validar os acentos e, com libass/MPV, o estilo de ASS/SSA. Os exemplos entregues exibem linhas entre 1 e 12 segundos; buscar esse trecho. Cancelar o seletor e confirmar a legenda/faixa anteriores. Repetir com o filme pausado e em reprodução.
7. **Envio pelo celular:** Legendas → “Enviar legenda pelo celular”; ler o QR com o celular na mesma rede. Escolher e enviar a legenda no navegador. Esse é também o fallback automático quando nenhum seletor Android está disponível. Testar fechar com Voltar e reabrir: o QR anterior deixa de funcionar. O QR expira em 10 minutos. Não exige internet nem conta de legendas.
8. **Erros e estado:** usar arquivo inválido e maior que 8 MiB; o filme e a faixa atual devem continuar. Com “somente idiomas preferidos”, confirmar que a legenda local aparece. Selecionar local enquanto chegam addons; ela deve permanecer selecionada. Trocar local/embutida/addon/desativada preservando posição, pausa, velocidade, faixa de áudio, ganho e atraso de legenda. Trocar filme e reabrir o app: legenda local é uma seleção temporária, não deve reaparecer em outro título; selecionar novamente se necessário.

Busca direta online por nome de arquivo ainda não foi implementada: depende da configuração de API key do provedor. Busca pelos addons existentes continua disponível para títulos com metadados/ID e addons de legenda habilitados.

Para relatar: modelo da TV, APK/arquitetura, player ExoPlayer/libass/MPV usado, formato da legenda, etapa acima, resultado esperado/observado e mensagem exibida. Marcar separadamente o que funcionou no seletor e pelo celular.

Teste adicional de importação lenta: ao ler uma legenda de provedor remoto, selecionar outra faixa ou desativar legendas antes da leitura terminar. A importação anterior deve ser cancelada e não pode substituir a escolha mais recente.

**Identidade e assinatura:** o usuário autorizou a variante separada. Os APKs finais Nuvio Teste foram assinados com a mesma chave persistente; futuras versões dessa variante poderão atualizá-la sem desinstalar quando tiverem a mesma assinatura e versionCode maior. Isso não recupera a assinatura da V1.2 nem migra seus dados.
