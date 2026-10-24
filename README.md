# CF Prova

Aplicativo Android nativo em Java para corrigir provas de múltipla escolha usando a câmera. Funciona offline e não envia imagens ou respostas para servidores.

## Como usar

1. Defina o nome e a quantidade de questões (5 a 40).
2. Edite e salve o gabarito do professor (alternativas A–E). As respostas começam em A: confira todas.
3. Gere o PDF do cartão e compartilhe com um aplicativo de impressão. Use o cartão desta versão, sem cortar os quatro quadrados pretos.
4. O aluno preenche completamente uma bolha por questão com caneta escura.
5. Fotografe a folha inteira na vertical, com boa iluminação.
6. Toque no centro dos quatro quadrados na ordem indicada: superior esquerdo, superior direito, inferior direito, inferior esquerdo, conforme o texto impresso.
7. Revise as respostas detectadas comparando com a foto. Resolva as marcações “Revisar”; respostas em branco valem zero.
8. Confirme para calcular a nota de 0 a 10, com questões de mesmo peso e detalhes dos acertos e erros.

## Desenvolvimento

Requisitos: JDK 17 ou 21, Android SDK Platform 35 e Build Tools 35.0.0. Configure `ANDROID_HOME` ou `sdk.dir` no arquivo local (ignorado) `local.properties`.

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Instale pelo Android Studio ou `adb install -r app/build/outputs/apk/debug/app-debug.apk` em Android 8.0 ou posterior. É uma compilação de desenvolvimento, sem assinatura de publicação.

No ambiente de nuvem preparado, execute `/workspace/tooling/cf-gradle testDebugUnitTest assembleDebug lintDebug`. Esse comando usa o JDK, SDK e cache instalados em `/workspace/tooling`, com as configurações locais de rede e diretórios graváveis.

## Leitura óptica e limitações

A imagem é corrigida por perspectiva a partir dos quatro pontos selecionados; a região interna de cada bolha é amostrada para estimar preenchimento. Marcações múltiplas ou fracas são sinalizadas. Essa técnica exige o cartão padronizado gerado pelo app, e não interpreta gabaritos arbitrários ou respostas manuscritas.

A revisão humana é obrigatória: sombras, desfoque, papel curvado e alinhamento impreciso podem causar erros inclusive em respostas aparentemente válidas. A precisão em cartões físicos precisa ser validada em aparelhos reais antes do uso em avaliações. Os testes unitários cobrem classificação de marcações e cálculo da nota, não a câmera ou precisão óptica real.

Esta versão mantém somente um gabarito no aparelho. Não há contas, histórico de alunos, exportação de notas ou pesos por questão. Fotos temporárias e PDF ficam no cache privado e podem ser substituídos pela próxima captura/geração. A câmera usa o aplicativo de câmera instalado no dispositivo.

## Testes e APK pelo GitHub

Em cada envio à branch `main` e em pull requests, o GitHub Actions executa os testes unitários, a análise estática e a compilação do APK. Também é possível iniciar manualmente:

1. Abra a aba **Actions** do repositório.
2. Selecione **Android — testes e APK** e clique em **Run workflow**, escolhendo `main`.
3. Abra a execução e espere o trabalho **Testar e compilar** terminar com sucesso.
4. Na seção **Artifacts**, baixe **CF-Prova-Android-debug** e extraia o ZIP para obter `app-debug.apk`. É necessário estar conectado ao GitHub. Os artefatos ficam disponíveis por 14 dias.
5. Transfira o APK para um celular Android 8.0 ou posterior. Abra pelo aplicativo Arquivos, autorize a instalação dessa origem quando solicitado e toque em **Instalar**. Depois, pode desativar a autorização dessa origem.

O APK usa assinatura de desenvolvimento e serve para testes. Se existir outra versão assinada com uma chave diferente, a atualização pode ser recusada: desinstalar a versão anterior apaga o gabarito local. Não é necessário desinstalar se a atualização funcionar.

Para conferir o resultado no celular, cadastre cinco questões, preencha quatro corretamente e uma incorretamente no cartão impresso e confirme a leitura: a nota deve ser **8,0**. Teste também questões em branco e marcações duplas. O GitHub Actions não testa a câmera nem substitui a validação física da leitura óptica.
