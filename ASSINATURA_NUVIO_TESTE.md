# Assinatura persistente — Nuvio Teste

A variante `fullTvTest` usa `com.nuvio.tv.center.test` e o nome de launcher **Nuvio Teste**. A principal `fullDebug` continua `com.nuvio.tv.center` / Nuvio. Ambas usam as mesmas fontes, configuração pública do QR, decoder FFmpeg modificado e funcionalidades completas. As instalações têm armazenamento independente: configurar conta, addons e preferências na variante nova, sem apagar a V1.2.

O workflow existente compila a principal e, pelo finalizador Gradle, a variante separada. O artefato contém quatro APKs: dois principais assinados com a chave debug temporária da CI e dois **Nuvio-Teste-CI-*** sem assinatura. Para testar ao lado da V1.2, instalar somente os dois APKs Nuvio-Teste finais assinados localmente, escolhendo uma arquitetura compatível com a TV. Não instalar APK unsigned nem usar o principal como atualização da V1.2.

## Chave privada criada neste computador

- Diretório: `C:/Users/Robson/.nuvio-signing/nuvio-teste`.
- Keystore PKCS12: `nuvio-teste.p12`; alias: `nuvio-teste`; RSA 3072; validade de 30 anos.
- Senha aleatória guardada em `password.txt`, no mesmo diretório privado; não está no código, logs ou Git. ACL permite somente Robson e SYSTEM.
- Metadados públicos e impressão digital fixada: `key.json`.
- Certificado SHA-256: `4953b9702476ac1820633877f44057ff6a3f81a0ce8b35453844a3f2c605e08c`.

Guardar um backup privado desse diretório completo, incluindo keystore e senha, fora dos ZIPs compartilhados. Esses arquivos permitem continuar assinando em outro computador e após reinstalar o Windows. O ZIP de código e o handoff não contêm a chave nem sua senha.

## Próximas versões

1. Manter package, alias e chave; aumentar `versionCode` acima de 1361 ao publicar uma nova versão da variante.
2. Compilar `:app:assembleFullDebug -PuseLocalFfmpegDecoder=true` no Actions existente, ou diretamente `:app:assembleFullTvTest` em ambiente com SDK/NDK e FFmpeg locais configurados.
3. Baixar os APKs Nuvio-Teste-CI e assinar ambos usando `tools/sign_nuvio_test.py` e a mesma pasta privada.
4. Conferir certificado, arquitetura e versão; atualizar somente a instalação Nuvio Teste. O script recusa pacote/rótulo diferentes, chave diferente e sobrescrita do APK de entrada ou de uma entrega existente.

Exemplo em PowerShell (Python 3, JDK 17 e Android SDK Build Tools 36 já instalados):

```powershell
python tools/sign_nuvio_test.py --jdk "CAMINHO_DO_JDK_17" sign `
  --build-tools "CAMINHO_DOS_BUILD_TOOLS_36" `
  --input "CAMINHO_DO_APK_Nuvio-Teste-CI_ARM64.apk" `
  --output "Nuvio-Teste-V1.4-ARM64.apk"
```

O diretório privado padrão é o indicado acima para o usuário atual. `--private-dir` antes de `sign` permite apontar para o backup restaurado. `init` verifica/reutiliza a chave existente; não substitui uma chave parcial ou diferente. As senhas são passadas às ferramentas por referência ao arquivo privado, nunca na linha de comando. O script aplica zipalign antes de assinar, verifica a assinatura v1/v2/v3 com apksigner e confirma o certificado fixado e a identidade do APK. Referência: https://developer.android.com/tools/apksigner.

Nesta máquina, o Python fica em `C:/Users/Robson/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`; o JDK em `C:/Users/Robson/Documents/Nuvio-Entregas-2026-10-04/jvm/jdk-17.0.20.1+1`; os Build Tools em `C:/Users/Robson/Documents/Nuvio-Entregas-2026-10-04/Nuvio-Teste/android-tools/android-16` (nome do diretório do ZIP oficial; source.properties confirma 36.0.0).
