# JPEG de regressão do cartão-resposta

`quarter-card-20-jpeg.jpg` é uma imagem de teste gerada, não uma foto de aluno.

Origem: `CardDrawing.draw` de produção, exportação real de `CardDocx.write` com um cartão de 1/4 de A4, renderização do Word em uma página A4 (1191 × 1684 pixels), preenchimento simulado e compressão JPEG qualidade 70. A imagem recebeu um gradiente de luz de aproximadamente 71% a 100% e desfoque gaussiano de raio 0,3 pixel. Contém cabeçalho e textos do modelo, não apenas bolhas sintéticas isoladas.

Respostas: questões 1, 5, 9, 13 e 17 em branco; nas demais, alternativa de índice `(questão - 1) % 5`, com A = 0. Contra esse mesmo gabarito, o resultado esperado é 15 acertos de 20 e nota 7,5. `CapturedCardTest` decodifica o JPEG no JDK do runner e verifica todas as respostas. A decodificação em Android e o flash físico ainda precisam ser conferidos em aparelho real.
