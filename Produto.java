import java.math.BigDecimal;

final class Produto {
    final int id;
    String nome;
    int quantidade;
    int minimo;
    BigDecimal preco;

    Produto(int id, String nome, int quantidade, int minimo, BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.minimo = minimo;
        this.preco = preco;
    }
}
