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
         */

    }


}
