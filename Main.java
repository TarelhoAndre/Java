import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    public static void main(String[] args) {
        Path arquivo = args.length > 0 ? Path.of(args[0]) : Path.of("dados", "estoque.txt");
        try {
            Estoque estoque = new Estoque(arquivo);
            System.out.println("ESTOQUE LOCAL | Dados: " + arquivo.toAbsolutePath());
            while (true) {
                System.out.println("\n1 - Cadastrar produto\n2 - Listar produtos\n3 - Buscar produto"
                        + "\n4 - Registrar entrada\n5 - Registrar saída\n6 - Editar produto"
                        + "\n7 - Excluir produto\n8 - Relatório do estoque\n0 - Sair");
                int opcao = inteiro("Opção: ", 0);
                if (opcao == 0) break;
                boolean alterou = false;
                switch (opcao) {
                    case 1 -> {
                        String nome = texto("Nome: ");
                        int quantidade = inteiro("Quantidade inicial: ", 0);
                        int minimo = inteiro("Estoque mínimo: ", 0);
                        BigDecimal preco = preco();
                        Produto p = estoque.cadastrar(nome, quantidade, minimo, preco);
                        System.out.println("Produto cadastrado com código " + p.id + ".");
                        alterou = true;
                    }
                    case 2 -> exibir(estoque.listar());
                    case 3 -> {
                        String termo = texto("Nome ou código: ").toLowerCase(Locale.ROOT);
                        exibir(estoque.listar().stream().filter(p -> p.nome.toLowerCase(Locale.ROOT).contains(termo)
                                || String.valueOf(p.id).equals(termo)).toList());
                    }
                    case 4, 5 -> {
                        Produto p = selecionar(estoque);
                        if (p == null) break;
                        int quantidade = inteiro("Quantidade: ", 1);
                        if (opcao == 5 && quantidade > p.quantidade) {
                            System.out.println("Saldo insuficiente. Disponível: " + p.quantidade);
                        } else if (opcao == 4 && quantidade > Integer.MAX_VALUE - p.quantidade) {
                            System.out.println("A entrada excede o limite de quantidade.");
                        } else {
                            p.quantidade += opcao == 4 ? quantidade : -quantidade;
                            System.out.println("Novo saldo: " + p.quantidade);
                            alterou = true;
                        }
                    }
                    case 6 -> {
                        Produto p = selecionar(estoque);
                        if (p == null) break;
                        String nome = texto("Novo nome: ");
                        int minimo = inteiro("Novo estoque mínimo: ", 0);
                        BigDecimal valor = preco();
                        p.nome = nome;
                        p.minimo = minimo;
                        p.preco = valor;
                        alterou = true;
                    }
                    case 7 -> {
                        Produto p = selecionar(estoque);
                        if (p != null && texto("Excluir " + p.nome + "? Digite SIM: ").equalsIgnoreCase("SIM")) {
                            estoque.remover(p);
                            alterou = true;
                        }
                    }
                    case 8 -> {
                        List<Produto> produtos = estoque.listar();
                        long unidades = produtos.stream().mapToLong(p -> p.quantidade).sum();
                        BigDecimal total = produtos.stream().map(p -> p.preco.multiply(BigDecimal.valueOf(p.quantidade)))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        System.out.println("Produtos: " + produtos.size() + " | Unidades: " + unidades
                                + " | Valor total: " + MOEDA.format(total));
                        System.out.println("Produtos no mínimo ou abaixo:");
                        exibir(produtos.stream().filter(p -> p.quantidade <= p.minimo).toList());
                    }
                    default -> System.out.println("Opção inválida.");
                }
                if (alterou) {
                    estoque.salvar();
                    System.out.println("Alteração salva.");
                }
            }
            System.out.println("Estoque encerrado.");
        } catch (FimDaEntrada e) {
            System.out.println("\nEntrada encerrada.");
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
            System.err.println("O programa foi encerrado. A última alteração pode não ter sido salva.");
            System.exit(1);
        }
    }

    private static Produto selecionar(Estoque estoque) {
        Produto produto = estoque.buscar(inteiro("Código do produto: ", 1));
        if (produto == null) System.out.println("Produto não encontrado.");
        return produto;
    }

    private static void exibir(List<Produto> produtos) {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto encontrado.");
            return;
        }
        for (Produto p : produtos) {
            System.out.printf("#%d | %s | Quantidade: %d | Mínimo: %d | Preço: %s%s%n",
                    p.id, p.nome, p.quantidade, p.minimo, MOEDA.format(p.preco),
                    p.quantidade <= p.minimo ? " | ESTOQUE BAIXO" : "");
        }
    }

    private static String texto(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            if (!ENTRADA.hasNextLine()) throw new FimDaEntrada();
            String valor = ENTRADA.nextLine().strip();
            if (!valor.isEmpty()) return valor;
            System.out.println("Informe um valor.");
        }
    }

    private static int inteiro(String mensagem, int minimo) {
        while (true) {
            try {
                int valor = Integer.parseInt(texto(mensagem));
                if (valor >= minimo) return valor;
            } catch (NumberFormatException ignored) { }
            System.out.println("Informe um inteiro entre " + minimo + " e " + Integer.MAX_VALUE + ".");
        }
    }

    private static BigDecimal preco() {
        while (true) {
            try {
                BigDecimal valor = new BigDecimal(texto("Preço unitário (ex.: 19,90): ").replace(',', '.'));
                if (valor.signum() >= 0) return valor.setScale(2, RoundingMode.UNNECESSARY);
            } catch (NumberFormatException | ArithmeticException ignored) { }
            System.out.println("Informe um preço não negativo com até duas casas decimais, sem separador de milhar.");
        }
    }

    private static final class FimDaEntrada extends RuntimeException { }
}
