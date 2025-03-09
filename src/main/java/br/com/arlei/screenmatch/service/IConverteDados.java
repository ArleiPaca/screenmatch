package br.com.arlei.screenmatch.service;

// Interface para converter dados que recebe uma classe generica e um json e retorna um objeto da classe generica
public interface IConverteDados {
    <T> T  obterDados(String json, Class<T> classe);
}
