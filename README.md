# CF Prova

App Android para corrigir cartões de respostas com a **câmera ao vivo**. Cadastre o gabarito, abra a câmera e aponte para a folha: após uma leitura estável, a nota aparece automaticamente, sem fotografar, salvar imagens, tocar em marcadores ou confirmar respostas. Funciona offline.

## Como usar

1. Defina nome e quantidade de questões (5 a 40).
2. Edite e salve o gabarito do professor (A–E). As respostas começam em A: confira todas antes de corrigir.
3. Gere o **Word editável (.docx)** ou o **PDF**, com **um único cartão no canto superior esquerdo da folha A4**, ocupando menos de 1/4 da página. No Word, personalize instituição, professor e avaliação. Imprima em A4, a 100%, e se desejar recortar, preserve os quatro marcadores e o código do cartão.
4. O aluno preenche uma bolha inteira por questão, com caneta escura.
5. Toque em **Abrir câmera e corrigir** e permita acesso à câmera.
6. Aponte para **um cartão por vez** (aproxime ou recorte a folha). Mantenha o cartão plano, inteiro e na vertical, com o **quadrado maior no alto à esquerda**. Use boa iluminação, sem sombras. Toque na prévia se precisar ajustar o foco.
7. Aguarde o foco e mantenha o celular parado. A quantidade de acertos e a nota aparecem automaticamente assim que pelo menos quatro leituras concordarem por cerca de um segundo (o tempo total depende do aparelho e do enquadramento).
8. O resultado mostra primeiro **“X acertos de N questões”**, seguido da nota e dos acertos por questão e toque em **Corrigir outro aluno** para uma nova leitura.

Respostas em branco valem zero. Marcas fracas ou duplas impedem a nota automática e geram uma orientação na tela: corrija o preenchimento ou melhore o enquadramento. Questões têm o mesmo peso; a nota vai de 0 a 10.

## Desenvolvimento

Requisitos: JDK 17 ou 21, Android SDK Platform 35 e Build Tools 35.0.0. Configure `ANDROID_HOME` ou `sdk.dir` no arquivo local ignorado `local.properties`.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Na nuvem preparada: `/workspace/tooling/cf-gradle testDebugUnitTest lintDebug assembleDebug`.

APK: `app/build/outputs/apk/debug/app-debug.apk`. Android 8.0 ou posterior, com câmera traseira. APK de desenvolvimento, sem assinatura de publicação.

## Testes e APK pelo GitHub

O GitHub Actions executa os testes, a análise estática e gera o APK em cada envio à `main` e em pull requests. Para iniciar manualmente, abra **Actions → Android — testes e APK → Run workflow**.

Após uma execução bem-sucedida, baixe **CF-Prova-Android-debug** em **Artifacts** (disponível por 14 dias), extraia o ZIP e abra `app-debug.apk` no celular. Autorize a instalação dessa origem quando solicitado. É necessário estar conectado ao GitHub para baixar artefatos.

Se houver outra versão assinada com uma chave diferente, o Android pode recusar a atualização. Desinstalar a anterior apaga o gabarito salvo. Evite desinstalar se a atualização funcionar.

## Validação no celular

Gere um cartão v2 de cinco questões, preencha quatro respostas corretas e uma errada e aponte a câmera: o resultado esperado é **4 acertos de 5 questões**, com nota **8,0**, sem disparar foto nem tocar nos cantos. Teste também respostas em branco, marcações duplas, folha ausente, cartão invertido, cartão com quantidade de questões diferente, sombras, foco, recusa da permissão e saída/retorno do aplicativo. Ao trocar de aluno, toque em **Corrigir outro aluno**.

Os testes automatizados incluem imagens sintéticas do layout, distorção, códigos de identificação, bloqueios de leitura e estabilidade entre quadros. Não substituem testes de câmera e cartões físicos. A precisão óptica em aparelhos reais ainda precisa ser validada; confira os detalhes antes de registrar uma nota oficial.

## Como funciona e limites

CameraX fornece a prévia e os quadros de luminância, descartando quadros antigos para evitar fila. O leitor localiza os quadrados pretos, corrige a perspectiva matematicamente, valida o código do cartão e a quantidade de questões, confere as bordas das bolhas e classifica o preenchimento. A nota só é liberada após consenso temporal; qualquer leitura inválida reinicia o consenso. Quadros ficam apenas na memória, sem arquivo de foto ou envio para servidores.

Use somente o cartão v2 do app, com a orientação indicada. Cartões arbitrários, papel curvado, rasuras, sombras e desfoque podem impedir a leitura ou causar erros. O código identifica o formato e a quantidade de questões, não o nome da prova: escolha e confira o gabarito correto no aparelho.

Mantém um gabarito por aparelho. Não há contas, histórico de alunos, pesos individuais ou exportação de notas. Os arquivos Word/PDF temporários ficam no cache privado.

## Modelo compacto para qualquer instituição

A versão 0.3.1 exporta uma folha A4 com **apenas um cartão**, no canto superior esquerdo; o restante fica em branco. A área óptica mede **87 × 130,5 mm**, com cabeçalho editável acima, dentro de uma célula de aproximadamente **100 × 142,9 mm**. Isso deixa margens de impressão e cabe em um quarto da folha A4 (105 × 148,5 mm). O modelo não tem logotipo nem nome de instituição fixo.

[Baixar modelo Word de 20 questões](modelos/cartao-20-questoes.docx). Para outra quantidade, use a exportação do aplicativo (5 a 40 questões). É um arquivo Word moderno `.docx`, não o formato binário antigo `.doc`.

Edite os três campos do cabeçalho do cartão, mantendo as três linhas. A grade óptica é uma imagem fixa para não deslocar bolhas, código ou marcadores durante a edição; não a recorte, distorça ou reprojete. O aluno preenche nome, turma e data à mão. A quantidade de questões está codificada na imagem: para alterá-la, gere novamente pelo app.

Os PDFs também contêm apenas um cartão de 1/4 de página em uma folha A4. Ao imprimir no Word, mantenha A4 e escala 100%; confira a prévia de uma página. A edição do cabeçalho e a impressão devem preservar as proporções. Para a câmera, enquadre somente um cartão, perto o suficiente para ler as bolhas pequenas. Especialmente com 40 questões, valide primeiro a impressão e o foco no aparelho. A leitura ao vivo continua usando a geometria v2.
