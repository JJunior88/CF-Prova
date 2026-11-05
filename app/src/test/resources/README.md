# JPEG de regressão do cartão-resposta

`quarter-card-20-jpeg.jpg` é uma imagem de teste gerada, não uma foto de aluno.

Origem: `CardDrawing.draw` de produção, exportação real de `CardDocx.write` com um cartão de 1/4 de A4, renderização do Word em uma página A4 (1191 × 1684 pixels), preenchimento simulado e compressão JPEG qualidade 70. A imagem recebeu um gradiente de luz de aproximadamente 71% a 100% e desfoque gaussiano de raio 0,3 pixel. Contém cabeçalho e textos do modelo, não apenas bolhas sintéticas isoladas.

Respostas: questões 1, 5, 9, 13 e 17 em branco; nas demais, alternativa de índice `(questão - 1) % 5`, com A = 0. Contra esse mesmo gabarito, o resultado esperado é 15 acertos de 20 e nota 7,5. `CapturedCardTest` decodifica o JPEG no JDK do runner e verifica todas as respostas. A decodificação em Android e o flash físico ainda precisam ser conferidos em aparelho real.

`lower-right-card-20-jpeg.jpg` usa a mesma preparação e respostas esperadas, mas foi gerado com a exportação 0.6.1: somente a grade, sem identificação, posicionada no canto inferior direito da A4. O teste mantém também o JPEG antigo para verificar compatibilidade dos cartões anteriores.

## Tabela A–E v3

`table-v3-blank.jpg`, `table-v3-filled.jpg` e `table-v3-double.jpg` vêm da renderização real de `CardDrawing` no Android simulado (1200 × 1800), com letras dentro dos círculos. O cartão vazio deve retornar vinte respostas em branco. No preenchido, a alternativa da questão i (base zero) é i % 5, exceto i % 4 == 0, que fica em branco: 15 acertos, nota 7,5. O terceiro acrescenta A e B na primeira questão, devendo ser recusado. Os preenchimentos são simulados para testar o algoritmo, não fotos de um dispositivo real.

## Leitura com perda de nitidez (0.8.1)

As imagens derivam dos JPEGs da tabela v3: `table-v3-soft-filled.jpg` foi reduzida para 600 × 900, desfocada com GaussianBlur(1.5) e recomprimida em qualidade 70. A versão 0.8.0 rejeitava essa imagem pela validação das bordas; a 0.8.1 deve retornar exatamente as mesmas respostas (15 acertos em 20). Os casos `soft-blank` e `soft-double` usam GaussianBlur(2) e verificam branco e rejeição da marca dupla. `no-rings` apaga os círculos, preservando letras e marcadores, e deve ser rejeitado. `shadow` reduz o cartão para 840 × 1260 e aplica iluminação horizontal de 45% a 100%; deve preservar todas as respostas. `unreadable` usa GaussianBlur(5) em 600 × 900 e deve ser rejeitado. São simulações, não capturas físicas.
