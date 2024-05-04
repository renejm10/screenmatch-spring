package com.aluracursos.screenmatch.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase); //<T> T es para indicar que es datos tipo generico
}
