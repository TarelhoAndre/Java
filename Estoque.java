import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

final class Estoque {
    private final Path arquivo;
    private final List<Produto> produtos = new ArrayList<>();
    private int proximoId = 1;

    Estoque(Path arquivo) throws IOException {
        this.arquivo = arquivo;
        if (!Files.exists(arquivo)) return;
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        try {
            if (linhas.isEmpty() || !linhas.get(0).startsWith("ESTOQUE_V1;")) {
                throw new IllegalArgumentException("Cabeçalho inválido");
            }
            proximoId = Integer.parseInt(linhas.get(0).split(";", -1)[1]);
            if (proximoId < 1) throw new IllegalArgumentException("Sequência inválida");
            for (int i = 1; i < linhas.size(); i++) {
                String[] campos = linhas.get(i).split(";", -1);
                if (campos.length != 5) throw new IllegalArgumentException("Registro inválido");
                int id = Integer.parseInt(campos[0]);
                String nome = new String(Base64.getDecoder().decode(campos[1]), StandardCharsets.UTF_8);
                int quantidade = Integer.parseInt(campos[2]);
                int minimo = Integer.parseInt(campos[3]);
                BigDecimal preco = new BigDecimal(campos[4]);
                if (id < 1 || id >= proximoId || buscar(id) != null || nome.isBlank()
                        || quantidade < 0 || minimo < 0 || preco.signum() < 0
                        || preco.scale() > 2) throw new IllegalArgumentException("Valores inválidos");
                produtos.add(new Produto(id, nome, quantidade, minimo, preco));
            }
        } catch (RuntimeException e) {
            throw new IOException("Arquivo de estoque inválido. Preserve o arquivo para recuperação: " + arquivo, e);
        }
    }

    List<Produto> listar() { return List.copyOf(produtos); }

    Produto buscar(int id) {
        return produtos.stream().filter(p -> p.id == id).findFirst().orElse(null);
    }

    Produto cadastrar(String nome, int quantidade, int minimo, BigDecimal preco) {
        if (proximoId == Integer.MAX_VALUE) throw new IllegalArgumentException("Limite de códigos atingido.");
        Produto produto = new Produto(proximoId++, nome, quantidade, minimo, preco);
        produtos.add(produto);
        return produto;
    }

    void remover(Produto produto) { produtos.remove(produto); }

    void salvar() throws IOException {
        Path destino = arquivo.toAbsolutePath();
        Files.createDirectories(destino.getParent());
        StringBuilder dados = new StringBuilder("ESTOQUE_V1;" + proximoId + "\n");
        for (Produto p : produtos) {
            String nome = Base64.getEncoder().encodeToString(p.nome.getBytes(StandardCharsets.UTF_8));
            dados.append(p.id).append(';').append(nome).append(';').append(p.quantidade)
                    .append(';').append(p.minimo).append(';').append(p.preco.toPlainString()).append('\n');
        }
        Path temporario = Files.createTempFile(destino.getParent(), "estoque-", ".tmp");
        try {
            Files.writeString(temporario, dados, StandardCharsets.UTF_8);
            try {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }
}
