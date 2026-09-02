package com.example.epopeyaap.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estadisticas embebidas dentro de Usuario.
 * Se actualizan automáticamente desde los distintos servicios
 * (DisponibilidadService, ConvocatoriaService, PartidoService...)
 */
@Embeddable // JPA: Permite que este objeto se guarde dentro de otra entidad. No tiene tabla ni Id
@Data //Lombok: Crea los Getters, Setters, toString, equals, hasCode...
@NoArgsConstructor //Lombok: Requerido por JPA/Hibernate. Genera contructor vacio, requisito para JPA/Hibernate
@AllArgsConstructor //Lombok: Para instanciar facilmente. Genera constructor con parametros por cada atributo.
public class EstadisticaJugador {
    private int vecesDisponible = 0;
    private int vecesConvocado = 0;
    private int partidosGanados = 0;
    private int partidosPerdidos = 0;
    private int setGanados = 0;
    private int juegosGanados = 0;
}
