# CF Prova

App Android para corrigir cartões de respostas a partir de uma **foto tirada dentro do app**, disponível somente para conferência. A foto fica apenas na memória: não é salva na galeria, em arquivo temporário ou enviada para servidores. O app funciona offline.

## Visual e navegação

A interface mantém cartões de etapas, botões de ação destacados e acesso fixo à câmera na tela inicial. O gabarito usa botões A–E com seleção visível; salvar e concluir ficam sempre acessíveis na parte inferior. Na versão 0.6.0, a câmera inclui conferência imediata, controle da luz/flash e uma guia visual para enquadrar o cartão-resposta, e a conferência reúne foto ampliável, acertos, nota e indicação de acerto/erro por questão. O resultado final inclui uma nota circular e a divisão entre acertos, erros e questões em branco.

<img src="docs/visual/inicio.png" width="240" alt="Tela inicial do CF Prova"/> <img src="docs/visual/gabarito.png" width="240" alt="Seleção de alternativas do gabarito"/> <img src="docs/visual/resultado.png" width="240" alt="Nota e acertos da avaliação"/>

[Prévia da câmera](docs/visual/camera.png) · [Prévia da conferência](docs/visual/conferencia.png). As imagens são renderizações de interface em ambiente Android simulado; a prévia de câmera está sem câmera conectada e a conferência usa um cartão ilustrativo. Foram conferidas larguras de 320 e 360 dp e fonte ampliada a 140%. Isso não substitui os testes físicos da câmera.

## Como usar

1. Defina nome e quantidade de questões (5 a 40) e confira todas as alternativas do gabarito do professor.
2. Gere o **Word editável (.docx)** ou **PDF** do **cartão-resposta v2**: um cartão no canto superior esquerdo de uma A4, ocupando 1/4 da página. Personalize o cabeçalho sem alterar os marcadores, o código ou as bolhas. Cartões v2 anteriores continuam compatíveis.
3. Imprima em A4, a 100%. O aluno deve preencher uma bolha inteira por questão com caneta escura.
4. Toque em **Ler cartão-resposta**, permita a câmera e enquadre um cartão com os quatro marcadores visíveis. O leitor procura o cartão na imagem, sem exigir que ele fique no centro ou alinhado aos cantos da foto. Pode reconhecer o cartão girado; fotografar de frente, sem dobrar a folha, continua dando mais nitidez.
5. Se precisar, toque em **Flash desligado** para ligar a luz contínua do flash. Ela ilumina a prévia e a foto. Toque em **Flash ligado** para desligar, inclusive se houver reflexos. Aparelhos sem esse recurso mostram **Sem flash**.
6. A opção **Conferência imediata** começa ligada. Mantenha o celular firme: depois de pelo menos três quadros concordarem por cerca de 0,7 segundo, o app tira a foto temporária e abre a conferência automaticamente. A prévia já informa acertos e nota. O tempo total depende de foco, luz e aparelho.
7. Para captura manual, desligue **Conferência imediata** e toque em **Tirar foto**. Em aparelhos que não suportam os três fluxos simultâneos da câmera, o app explica a limitação e mantém a captura por botão.
8. A foto capturada é lida novamente, independentemente da prévia. Se não houver confiança, o app explica o motivo e não atribui nota usando respostas antigas. Verifique o modelo, aproxime, ajuste foco/luz e refaça.
9. Confira a imagem ampliável e as respostas. Ajustes manuais atualizam acertos e nota imediatamente. **Refazer foto** descarta a imagem anterior; **Concluir conferência** descarta a imagem e abre o resultado final; **Sair e descartar** cancela.

Respostas em branco valem zero. Marcas fracas ou duplas impedem a leitura automática até serem corrigidas ou fotografadas com mais nitidez. Questões têm o mesmo peso e a nota vai de 0 a 10.

O fluxo de apontar, reconhecer e conferir atende à referência de uso solicitada. O layout reconhecido é o cartão-resposta v2 deste app; não há integração com CAEd/SEAMA nem suporte implícito a cartões externos com outros layouts.

## Foto e privacidade

A captura automática e a manual usam o callback em memória do CameraX (`OnImageCapturedCallback`), sem `OutputFileOptions`, URI de arquivo ou MediaStore. A foto não é compartilhada entre atividades, serializada em estado salvo nem mantida em variável estática. A tela de câmera/conferência bloqueia capturas de tela e miniaturas no seletor de aplicativos com `FLAG_SECURE`.

A imagem é liberada ao refazer a captura, finalizar, cancelar, sair da tela ou colocar o aplicativo em segundo plano. Uma captura atrasada que chega depois de sair é rejeitada e liberada. Se a tela de conferência for interrompida ou recriada, será necessário tirar outra foto; a imagem não é restaurada de disco. A câmera e a luz são liberadas durante a conferência. Quadros da prévia são processados em memória e fechados após a análise; não há gravação de vídeo. Só as respostas conferidas voltam para a tela de resultado.

O gabarito do professor continua salvo no aparelho. Os arquivos Word/PDF exportados também podem existir no cache; eles são modelos em branco e não contêm a foto do aluno.

## Desenvolvimento

Requisitos: JDK 17 ou 21, Android SDK Platform 35 e Build Tools 35.0.0. Configure `ANDROID_HOME` ou `sdk.dir` no arquivo local ignorado `local.properties`.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Na nuvem preparada: `/workspace/tooling/cf-gradle testDebugUnitTest lintDebug assembleDebug`.

APK: `app/build/outputs/apk/debug/app-debug.apk`. Android 8.0 ou posterior, com câmera traseira. Compilação de desenvolvimento, sem assinatura de publicação.

## Testes e APK pelo GitHub

O GitHub Actions executa os testes, a análise estática e gera o APK em cada envio à `main` e em pull requests. Para iniciar manualmente, abra **Actions → Android — testes e APK → Run workflow**.

Após uma execução bem-sucedida, baixe **CF-Prova-Android-debug** em **Artifacts** (disponível por 14 dias), extraia o ZIP e abra `app-debug.apk` no celular. Autorize a instalação dessa origem quando solicitado. É necessário estar conectado ao GitHub para baixar artefatos.

Se houver outra versão assinada com uma chave diferente, o Android pode recusar a atualização. Desinstalar a anterior apaga o gabarito salvo. Evite desinstalar se a atualização funcionar.

## Validação e correções do leitor (0.6.0)

A versão anterior dividia a foto em quatro quadrantes e escolhia um marcador em cada quadrante. Um cartão de 1/4 de A4 podia ficar inteiro no mesmo canto da foto, produzindo rejeição ou a mensagem incorreta sobre orientação. Há testes que reproduziram a falha antes da correção e passaram depois.

Agora o leitor agrupa os quatro marcadores pela geometria do cartão em qualquer posição e orientação, corrige a perspectiva e valida assinatura e quantidade de questões. A seleção não depende do centro da foto. Dois cartões válidos visíveis geram orientação para enquadrar apenas um. O limiar de imagem considera luz local, o preenchimento considera contraste local e as bordas finas das bolhas toleram o deslocamento de pixels causado por JPEG/redimensionamento. Assinatura, quantidade de questões, nitidez, marcas ambíguas e ausência de marcadores continuam bloqueando resultados indevidos.

Os testes incluem cartões deslocados, invertidos, em paisagem, perspectiva, sombras graduais, múltiplos cartões, marcadores ausentes, dados Y com padding/stride/rotação, consenso da prévia e o [JPEG de regressão da exportação real](app/src/test/resources/README.md). Testes de imagem não comprovam funcionamento físico do flash ou precisão em todos os celulares.

No aparelho: use cinco questões com quatro respostas corretas e uma errada. Com **Conferência imediata** ligada, aponte, aguarde a captura e confira **4 acertos de 5**, nota **8,0**. Repita com captura manual, cartão no canto da foto, girado e iluminação reduzida com a luz ligada/desligada. Confira que a luz apaga ao sair ou abrir a conferência e que nenhuma foto aparece na galeria. Teste também recusa de permissão, segundo plano durante captura, ampliação, refazer, ajustes de resposta, cartão incompatível e quantidade de questões divergente. A validação física no Android ainda precisa ser feita.

## Modelo compacto para qualquer instituição

A exportação mantém **um cartão de 1/4 de página** em uma A4, no canto superior esquerdo; o restante fica em branco. A área óptica mede **87 × 130,5 mm**, com cabeçalho editável acima, dentro de aproximadamente **100 × 142,9 mm**. Cabe em um quarto da A4 (105 × 148,5 mm), com margens de impressão. O modelo não tem instituição fixa.

[Baixar modelo Word de 20 questões](modelos/cartao-20-questoes.docx). Para outra quantidade, use a exportação do aplicativo (5 a 40). É Word moderno `.docx`.

Edite os três campos do cabeçalho, mantendo as três linhas. A grade óptica é uma imagem fixa; preserve bolhas, marcadores, código e proporções. O aluno preenche nome, turma e data à mão. Para alterar a quantidade de questões, gere o cartão novamente. O código identifica formato e quantidade, não o nome da prova: escolha o gabarito correto no app.

Imprima em A4, a 100%, e aproxime a câmera de um cartão por vez. Especialmente com 40 questões, valide a impressão e o foco. O leitor continua usando a geometria v2; a mudança para captura por botão não exige trocar cartões v2 existentes. Não há contas, histórico de alunos, pesos individuais ou exportação de notas.
