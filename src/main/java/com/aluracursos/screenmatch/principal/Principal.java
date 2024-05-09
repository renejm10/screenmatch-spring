package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.modelo.DatosEpisodio;
import com.aluracursos.screenmatch.modelo.DatosSerie;
import com.aluracursos.screenmatch.modelo.DatosTemporadas;
import com.aluracursos.screenmatch.modelo.Episodio;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner scanner = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE ="https://www.omdbapi.com/?t=";
    private final String API_KEY ="&apikey=c79e28ed";
    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraMenu(){
        //busca datos generales de la serie por nombre
        System.out.println("Ingrese nombre de serie: ");
        var nombreSerie=scanner.nextLine();
        var json = consumoApi.obtenerDatos(
                URL_BASE + nombreSerie.replace(" ","+") + API_KEY);
        var datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos);

        //busca datos de las temporadas
        List<DatosTemporadas> temporadas = new ArrayList<>();
        for (int i = 1; i < datos.totalDeTemporada(); i++) {
            json=consumoApi.obtenerDatos(
                    URL_BASE + nombreSerie.replace(" ","+") + API_KEY + "&season="+i);
            var datosTemporadas = conversor.obtenerDatos(json,DatosTemporadas.class);
            temporadas.add(datosTemporadas);
        }
//        temporadas.forEach(System.out::println);

        //mostrar solo el titulo de los episodios para las temporadas
//        try{
//            for (int i = 0; i < datos.totalDeTemporada() ; i++) {
//                List<DatosEpisodio>episodiosTemporada = temporadas.get(i).episodios();
//                for (int j = 0; j < episodiosTemporada.size(); j++) {
//                    System.out.println(episodiosTemporada.get(j).titulo());
//                }
//            }
//        }catch (IndexOutOfBoundsException e){
//            System.out.println(e.getMessage());
//        }
        //modo simplificado del codigo comentado previamente (esto es una funcion lambda)
//        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));


        //convertir toda la informacion en una lista de tipo datos episodio
        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t ->t.episodios().stream()).collect(Collectors.toList());//crea un collection mapeando solo episodios
        //Top 5 episodios
        datosEpisodios.stream()
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())//ordena de manera descendente
//                .peek(e -> System.out.println("primer filtro ordenar" + e))
                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))//no muestra evaluaciones N/A
//                .peek(e -> System.out.println("Segundo filtro (N/A)" + e))//ojeadita a ver si hace el primer paso
                .limit(5)//top 5
//                .peek(e -> System.out.println("tercer filtro selecciona solo 5" + e))
                .forEach(System.out::println);//imprime los valores

//        convirtiendo datos a una lista tipo episodio
        List < Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(),d)))
                .collect(Collectors.toList());
        episodios.forEach(System.out::println);

//        //busqueda de episodios apartir de una fecha
//        System.out.println("Ingrese apartir de que año desea ver los episodios: ");
//        var fecha = scanner.nextInt();
//        scanner.nextLine();
//
//        LocalDate fechaBusqueda= LocalDate.of(fecha,1,1);
//
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e ->e.getFechaLanzamiento() != null && e.getFechaLanzamiento().isAfter(fechaBusqueda))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " Episodio: " + e.getTitulo() +
//                                " Fecha de Lanzamiento: " + e.getFechaLanzamiento().format(dtf)
//                ));



        //Busqueda de episodios por pedazo del titulo

//        System.out.println("Ingrese parte del titulo: ");
//        var pedazoTitulo = scanner.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
//                .findFirst();
//        if (episodioBuscado.isPresent()){
//            System.out.println("Episodio encontrado");
//            System.out.println("Los datos son: " + episodioBuscado.get());
//        }else {
//            System.out.println("episodio no encontrado");
//        }

        //crear agrupamiento por temporada con su average de evaluacion
        Map<Integer,Double> evaluacionesTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion()>0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(evaluacionesTemporada);

        //muestra estadisticas como ser la suma, min, max, count y average
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e ->e.getEvaluacion()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        System.out.println(est); //muestra_todo
        System.out.println("Media: " + est.getAverage());
        System.out.println("Mejor evaluado: " + est.getMax());
    }
}
