package br.com.arlei.screenmatch.principal;

import br.com.arlei.screenmatch.model.DadosEpisodio;
import br.com.arlei.screenmatch.model.DadosSerie;
import br.com.arlei.screenmatch.model.DadosTemporada;
import br.com.arlei.screenmatch.model.Episodio;
import br.com.arlei.screenmatch.service.ConsumoAPI;
import br.com.arlei.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu(){

        System.out.println("Digite o nome da série para a busca");

        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

		for(int i = 1; i<=dados.totalTemporadas(); i++) {

            System.out.println(ENDERECO + nomeSerie.replace(" ","+") + "&season=" + i + API_KEY);
            json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ","+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);

		}
		temporadas.forEach(System.out::println);

        for(int i=0;i < dados.totalTemporadas();i++){
            List<DadosEpisodio> episodios = temporadas.get(i).episodios();
            for(int j=0;j<episodios.size();j++){
                System.out.println(episodios.get(j).titulo());
            }

        }

        temporadas.forEach(
            temporada -> temporada.episodios().forEach(
                episodio -> System.out.println(episodio.titulo())
            )
        );

        
        List<DadosEpisodio> episodios = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream())
                .collect(Collectors.toList());
        episodios.forEach(episodio -> System.out.println(episodio.titulo()));

        // poderia retornar pelo metodo .toList porem seria uma lista imutavel,c aso fosse acrescentar um novo episodio
         // p flatMap recupera uma lista dentro de uma outro lista...

// Usar o peek para debugar o código
        System.out.println("\nOs 10 melhores episódios são: ");
        episodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .peek(e -> System.out.println("Segundo filtro(ordenado) " + e))
                .limit(5)
                .peek(e -> System.out.println("Terceiro filtro(limit) " + e))
                .map(e -> e.titulo().toUpperCase())
                .forEach(System.out::println);

        List<Episodio> episodiosClasse = temporadas.stream()
                .flatMap(temporada -> temporada.episodios().stream())
                .map(d -> new Episodio(d.numero(), d))
                .collect(Collectors.toList());


        System.out.println("A partir de que ano você deseja ver os episódios? ");

        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodiosClasse.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada() +
                                " Episódio: " + e.getTitulo() +
                                " Data lançamento: " + e.getDataLancamento().format(formatador)
                ));


        // Optional e mito comun no uso com repositories, pois ele pode ou nao retornar um valor e facilita usando seus metodos.
        System.out.println("Digite um trecho do título do episódio");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodiosClasse.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if(episodioBuscado.isPresent()){
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episódio não encontrado!");
        }


        // agrupando por temporada e calculando a média de avaliação, Map e Collectors uso.

        Map<String, Double> avaliacoesPorTemporada = episodiosClasse.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        // classe que faz um resumo de estatisticas de um stream de dados, como média, maior, menor, quantidade

        DoubleSummaryStatistics est = episodiosClasse.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println(est);
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade: " + est.getCount());

        // exemplo com reduce...

        List<Integer> numeros = Arrays.asList(1, 2, 3, 4, 5);

        int soma = numeros.stream()
                .peek(n -> System.out.println("Elemento: " + n))
                .map(n -> n * 2)
                .peek(n -> System.out.println("Conteúdo depois do map: " + n))
                .reduce(0, (total, numero) -> total + numero);

        System.out.println("A soma dos números é: " + soma);


        /*


        public class ExemploFindAnyParallelStream {
            public static void main(String[] args) {
                List<Integer> numeros = new ArrayList<>();
                for (int i = 1; i <= 100; i++) {
                    numeros.add(i);
                }

                // Utilizando parallelStream para encontrar um elemento qualquer em paralelo
                Optional<Integer> numeroQualquer = numeros.parallelStream()
                        .filter(numero -> numero % 10 == 0) // Filtra os números que são múltiplos de 10
                        .findAny();

                if (numeroQualquer.isPresent()) {
                    System.out.println("Encontrado: " + numeroQualquer.get());
                } else {
                    System.out.println("Nenhum número encontrado.");
                }
            }
    }


    ---- outro bom exemplo
    public class Aluno {
    private String nome;
    private LocalDate nascimento;

    public Aluno(String nome, LocalDate nascimento) {
        this.nome = nome;
        this.nascimento = nascimento;
    }

    public int getIdade() {
        Period periodo = Period.between(nascimento, LocalDate.now());
        return periodo.getYears();
    }

    //getters, setters e toString omitidos

    public class Principal {
    public static void main(String[] args) {
        List<Aluno> alunos = Arrays.asList(
                new Aluno("Alice", LocalDate.of(2002, 10, 20)),
                new Aluno("Bob", LocalDate.of(1980, 8, 9)),
                new Aluno("Carlos", LocalDate.of(2001, 01, 28)),
                new Aluno("David", LocalDate.of(2003, 05, 12)),
                new Aluno("Eva", LocalDate.of(2005, 12, 03))
        );

        IntSummaryStatistics stats = alunos.stream()
                .mapToInt(Aluno::getIdade)
                .summaryStatistics();

        // Exibindo as estatísticas
        System.out.println("Idade média: " + stats.getAverage());
        System.out.println("Mínima idade: " + stats.getMin());
        System.out.println("Máxima idade: " + stats.getMax());
        System.out.println("Total de alunos: " + stats.getCount());
    }
}

}

O Java Streams é uma característica poderosa que oferece a capacidade de realizar operações de
 processamento de dados complexas de forma eficiente e em paralelo, sobre collections, arrays e I/O channels. Quando você começa a entender o Java Streams melhor, você nota que ele pode ser usado em muitas maneiras diferentes para fazer seu código mais limpo e mais eficiente. Vamos olhar mais a fundo para os usos avançados de Java Streams e como você pode começar a integrá-los em seus projetos.

Uso avançado de Java Streams
Quando trabalhamos com Java Streams, muitas vezes vamos além do uso básico e adentramos em conceitos mais avançados e complexos. Vamos ver alguns exemplos:

1 - Streams Infinitos

Streams infinitos, ou “infinite Streams”, são streams que não têm um
tamanho definido. Eles são úteis quando queremos gerar uma sequência de números ou valores. Aqui está um exemplo de como criamos um Stream infinito com o método iterate:

Stream.iterate(0, n -> n + 1)
     .limit(10)
     .forEach(System.out::println);

     No exemplo acima, começamos com o número 0 e adicionamos 1 a cada iteração para gerar
      uma sequência numérica. Utilizamos o método limit() para restringir o Stream infinito a 10 elementos e usamos forEach para imprimi-los.


 2 - FlatMap

O método flatMap é uma operação intermediária que é usada para transformar um Stream de
 coleções em um Stream de elementos. Aqui está um exemplo de como o flatMap é usado:

List<List<String>> list = List.of(
  List.of("a", "b"),
  List.of("c", "d")
);

Stream<String> stream = list.stream()
  .flatMap(Collection::stream);

stream.forEach(System.out::println);
Neste exemplo, transformamos um Stream de List para um Stream de Strings.


 3 - Redução de Streams

Stream.reduce() é uma operação terminal
que é utilizada para reduzir o conteúdo de um Stream para um único valor.

List<Integer> numbers = List.of(1, 2, 3, 4, 5);
Optional<Integer> result = numbers.stream().reduce(Integer::sum);
result.ifPresent(System.out::println); //prints 15

No exemplo acima, somamos todos os números da lista usando o método reduce().

Lembre-se que o Java Streams é uma ferramenta poderosa que pode tornar seu código mais elegante e eficiente. Continue praticando e explorando todas as diferentes operações e métodos disponíveis para você com Java Streams para se
tornar mais hábil em lidar com dados em suas aplicações.

         */

    }


}
