package br.com.arlei.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// aqui iremos desserializar o JSON que a API nos retorna o jsoproperty tbm serve para renomear o atributo, porepm no alis podemusar array
// para renomear mais de um atributo
// Anotação para ignorar propriedades desconhecidas
@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosSerie(@JsonAlias("Title") String tituloSerie,
                         @JsonAlias("totalSeasons") Integer totalTemporadas,
                         @JsonAlias("imdbRating") String avaliacao) {
}
