package br.com.arlei.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDate;

public class Episodio {

    private String temporada;
    private String titulo;
    private String numeroEpisodio;
    private Double avaliacao;
    private LocalDate dataLancamento;

    public Episodio(@JsonAlias("Episode") String numero, DadosEpisodio d) {

        this.temporada = numero;
        this.titulo = d.titulo();
        this.numeroEpisodio = d.numero();

        if (d.avaliacao().equals("N/A"))
            this.avaliacao = 0.0;
        else
            this.avaliacao =
                Double.parseDouble(d.avaliacao());


        this.dataLancamento = LocalDate.parse(d.dataLancamento());

    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNumeroEpisodio() {
        return numeroEpisodio;
    }

    public void setNumeroEpisodio(String numeroEpisodio) {
        this.numeroEpisodio = numeroEpisodio;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }
}
