# Estoque local em Java

Sistema de terminal sem bibliotecas externas. Requer Java 17 ou superior.

## Executar

Abra o terminal nesta pasta:

```powershell
javac -encoding UTF-8 Main.java Estoque.java Produto.java
java Main
```

O menu permite cadastrar, listar, buscar por nome ou código, registrar entradas
e saídas, editar nome/preço/estoque mínimo, excluir com confirmação e consultar
o valor total e os produtos com estoque baixo. Os códigos são gerados automaticamente.
Para ajustar o saldo, use entradas e saídas.

Cada alteração é salva em `dados/estoque.txt`, relativo à pasta de execução.
Os dados são recuperados na próxima execução. Os preços aceitam vírgula ou ponto
decimal e até duas casas decimais; quantidades são inteiras e não negativas.

Para usar outro arquivo:

```powershell
java Main "dados/meu-estoque.txt"
```

Faça backup do arquivo de dados com o programa fechado. Use apenas uma instância
por arquivo de estoque por vez. Não edite o arquivo manualmente: os nomes são
codificados para preservar caracteres especiais. Um arquivo inválido interrompe
a inicialização, sem sobrescrever seus dados.
